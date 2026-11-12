package edu.dosw.styleradar.pagos_suscripciones.application.usecase;

import edu.dosw.styleradar.pagos_suscripciones.application.service.SuscripcionActivacionService;
import edu.dosw.styleradar.pagos_suscripciones.domain.exception.IdempotencyException;
import edu.dosw.styleradar.pagos_suscripciones.domain.exception.PaymentProcessingException;
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
        log.info("Webhook ID={} acción={} tipo={}", idExterno, accion, tipoEvento);

        if (idExterno == null) {
            log.warn("Webhook sin data.id, ignorado.");
            return;
        }

        String lockKey = "lock:pago_" + idExterno;
        if (!idempotencyPort.adquirirLock(lockKey)) {
            throw new IdempotencyException("El pago ya está siendo procesado por otra instancia.");
        }
        try {
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
        // 1. Consultar a MP por el pago completo para obtener el external_reference
        Map<String, Object> pagoData = mercadoPagoPort.consultarPago(idPagoExterno);
        if (pagoData == null || pagoData.get("status") == null) {
            log.warn("No se pudo obtener información del pago {} en MP", idPagoExterno);
            return;
        }

        String estadoReal = pagoData.get("status").toString();
        String externalRef = pagoData.get("external_reference") != null ? pagoData.get("external_reference").toString() : null;

        if (externalRef == null || externalRef.isEmpty()) {
            log.warn("El pago {} no tiene external_reference", idPagoExterno);
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
            log.warn("Estado desconocido '{}' para pago {}. Ignorado.", estadoReal, idPagoExterno);
            return;
        }

        // 2. Buscar la orden por nuestro ID real
        OrdenPago orden = ordenPagoRepository.buscarPorId(ordenId)
                .orElseThrow(() -> new PaymentProcessingException(
                        "No se encontró orden para el external_reference: " + ordenId));

        // 3. Actualizar el idPagoExterno si no lo tiene
        boolean actualizado = false;
        if (orden.getIdPagoExterno() == null) {
            orden.actualizarIdPagoExterno(idPagoExterno);
            actualizado = true;
        }

        // 4. Idempotencia por estado
        if (orden.getEstado() == estado) {
            log.info("Orden {} ya en estado {}. Skip idempotente.", orden.getId(), estado);
            if (actualizado) {
                ordenPagoRepository.guardar(orden);
            }
            return;
        }

        orden.actualizarEstado(estado.name());
        ordenPagoRepository.guardar(orden);

        if (estado == EstadoPago.APROBADO) {
            suscripcionActivacionService.activarYExtenderSuscripcion(orden);
        } else {
            log.warn("Pago {} en estado {}. No se activan beneficios.", idPagoExterno, estado);
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