package edu.dosw.styleradar.pagos_suscripciones.application.usecase;

import edu.dosw.styleradar.pagos_suscripciones.application.service.SuscripcionActivacionService;
import edu.dosw.styleradar.pagos_suscripciones.domain.exception.IdempotencyException;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.EstadoPago;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.in.WebhookPort;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.IdempotencyPort;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.MercadoPagoPort;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.OrdenPagoRepositoryPort;
import edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.entity.WebhookLogEntity;
import edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.repository.WebhookLogJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcesarWebhookUseCaseImpl implements WebhookPort {

    private final IdempotencyPort idempotencyPort;
    private final OrdenPagoRepositoryPort ordenPagoRepository;
    private final MercadoPagoPort mercadoPagoPort;
    private final WebhookLogJpaRepository webhookLogRepository;
    private final SuscripcionActivacionService suscripcionActivacionService;

    @Override
    @Transactional
    public void procesarNotificacionPago(String idExterno, String accion, String tipoEvento, String payloadJson) {
        log.info("Procesando Webhook ID={} acción={} tipo={}", idExterno, accion, tipoEvento);

        if (idExterno == null) {
            log.warn("Webhook sin data.id, ignorado.");
            return;
        }

        String lockKey = "lock:pago_" + idExterno;
        if (!idempotencyPort.adquirirLock(lockKey)) {
            throw new IdempotencyException("El pago ya está siendo procesado por otra instancia.");
        }

        try {
            // Registro de auditoría (solo si no existe)
            if (!webhookLogRepository.existsByIdPagoExterno(idExterno)) {
                WebhookLogEntity entry = new WebhookLogEntity();
                entry.setIdPagoExterno(idExterno);
                entry.setEstadoProcesamiento(accion != null ? accion : "RECIBIDO");
                entry.setFechaRecepcion(LocalDateTime.now());
                entry.setPayloadRecibido(payloadJson);
                webhookLogRepository.save(entry);
            }

            if (tipoEvento == null) {
                log.warn("Webhook sin tipo, ignorado.");
                return;
            }

            if (tipoEvento.equalsIgnoreCase("payment")) {
                procesarEventoPago(idExterno);
            } else if (tipoEvento.equalsIgnoreCase("subscription_authorized_payment")) {
                procesarEventoPagoAutorizado(idExterno);
            } else if (tipoEvento.toLowerCase().contains("preapproval")) {
                log.info("Evento preapproval recibido para {} (informativo).", idExterno);
            } else {
                log.warn("Tipo de evento no soportado: {}", tipoEvento);
            }
        } finally {
            idempotencyPort.liberarLock(lockKey);
        }
    }

    private void procesarEventoPago(String idPagoExterno) {
        // Consulta obligatoria a MP. Si falla, se lanza excepción y el Controller devuelve 200
        // para evitar reintentos, pero la orden NO se aprueba.
        Map<String, Object> pagoData = mercadoPagoPort.consultarPago(idPagoExterno);
        if (pagoData == null) {
            throw new RuntimeException("No se pudo obtener información del pago " + idPagoExterno + " desde Mercado Pago");
        }

        String estadoReal = pagoData.get("status") != null ? pagoData.get("status").toString() : null;
        String externalRef = pagoData.get("external_reference") != null ? pagoData.get("external_reference").toString() : null;

        procesarEstado(estadoReal, externalRef, idPagoExterno);
    }

    private void procesarEventoPagoAutorizado(String idPagoAutorizado) {
        Map<String, Object> pagoData = mercadoPagoPort.consultarPagoAutorizado(idPagoAutorizado);
        if (pagoData == null) {
            throw new RuntimeException("No se pudo obtener información del pago autorizado " + idPagoAutorizado);
        }

        String estadoReal = null;
        if (pagoData.get("payment") instanceof Map<?, ?> paymentObj) {
            Object status = paymentObj.get("status");
            if (status != null) estadoReal = status.toString();
        }
        if (estadoReal == null && pagoData.get("status") != null) {
            estadoReal = pagoData.get("status").toString();
        }
                
        String externalRef = pagoData.get("external_reference") != null ? pagoData.get("external_reference").toString() : null;

        procesarEstado(estadoReal, externalRef, idPagoAutorizado);
    }

    private void procesarEstado(String estadoReal, String externalRef, String idExternoMp) {
        if (estadoReal == null) {
            log.warn("No se encontró estado para el evento {}", idExternoMp);
            return;
        }
        if (externalRef == null || externalRef.isEmpty()) {
            log.warn("El evento {} no tiene external_reference", idExternoMp);
            return;
        }

        Long ordenId;
        try {
            ordenId = Long.valueOf(externalRef);
        } catch (NumberFormatException e) {
            log.warn("external_reference inválido: {}", externalRef);
            return;
        }

        EstadoPago estado = parseEstado(estadoReal);
        if (estado == null) {
            log.warn("Estado desconocido '{}' para evento {}. Ignorado.", estadoReal, idExternoMp);
            return;
        }

        OrdenPago orden = ordenPagoRepository.buscarPorId(ordenId)
                .orElseThrow(() -> new RuntimeException("No se encontró orden local para el external_reference: " + ordenId));

        // Idempotencia a nivel de estado
        if (orden.getEstado() == estado) {
            log.info("Orden {} ya en estado {}. Skip idempotente.", orden.getId(), estado);
            return;
        }

        orden.actualizarEstado(estado.name());
        orden.actualizarIdPagoExterno(idExternoMp);
        ordenPagoRepository.guardar(orden);

        if (estado == EstadoPago.APROBADO) {
            log.info("Orden {} aprobada. Activando suscripción...", orden.getId());
            suscripcionActivacionService.activarYExtenderSuscripcion(orden);
        } else {
            log.warn("Pago {} en estado {}. No se activan beneficios.", idExternoMp, estado);
        }
    }

    private EstadoPago parseEstado(String raw) {
        if (raw == null) return null;
        return switch (raw.toLowerCase()) {
            case "approved" -> EstadoPago.APROBADO;
            case "rejected", "cancelled" -> EstadoPago.RECHAZADO;
            case "pending", "in_process", "authorized", "in_mediation" -> EstadoPago.PENDIENTE;
            case "refunded", "charged_back" -> EstadoPago.REEMBOLSADO;
            default -> null;
        };
    }
}