package edu.dosw.styleradar.pagos_suscripciones.application.usecase;

import edu.dosw.styleradar.pagos_suscripciones.domain.exception.IdempotencyException;
import edu.dosw.styleradar.pagos_suscripciones.domain.exception.PaymentProcessingException;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.EstadoPago;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.Suscripcion;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.in.WebhookPort;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.*;
import edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.entity.WebhookLogEntity;
import edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.repository.WebhookLogJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcesarWebhookUseCaseImpl implements WebhookPort {

    private final IdempotencyPort idempotencyPort;
    private final OrdenPagoRepositoryPort ordenPagoRepository;
    private final SuscripcionRepositoryPort suscripcionRepository;
    private final EventPublisherPort eventPublisher;
    private final WebhookLogJpaRepository webhookLogRepository; // <--- NUEVO

    @Override
    @Transactional
    public void procesarNotificacionPago(String idExterno, String estadoPago, String tipoEvento) {
        log.info("Iniciando procesamiento de webhook para ID: {} con estado: {}", idExterno, estadoPago);
        
        // 1. Guardar el log del webhook (para la tabla de auditoría)
        if (!webhookLogRepository.existsByIdPagoExterno(idExterno))  {
            WebhookLogEntity logEntity = new WebhookLogEntity();
            logEntity.setIdPagoExterno(idExterno);
            logEntity.setEstadoProcesamiento(estadoPago != null ? estadoPago : "RECIBIDO");
            logEntity.setFechaRecepcion(LocalDateTime.now());
            webhookLogRepository.save(logEntity);
        }

        String lockKey = "lock:pago_" + idExterno;
        if (!idempotencyPort.adquirirLock(lockKey)) {
            log.warn("Webhook duplicado detectado concurrentemente para: {}", idExterno);
            throw new IdempotencyException("El pago ya está siendo procesado por otra instancia.");
        }

        try {
            // 2. Buscar la suscripción por preapproval_id
            Suscripcion suscripcion = suscripcionRepository.buscarPorPreapprovalId(idExterno)
                    .orElse(null);

            if (suscripcion != null) {
                log.info("Suscripción encontrada por preapproval_id: {}. Activando...", idExterno);
                
                if (suscripcion.getEstado().name().equals("ACTIVA")) {
                    log.info("Webhook ya procesado previamente (Suscripción ya activa) para: {}", idExterno);
                    return;
                }

                suscripcion.activar(LocalDate.now().plusMonths(1));
                suscripcionRepository.guardar(suscripcion);
                
                //Actualizar la orden de pago a APROBADO
                ordenPagoRepository.buscarPorSuscripcionId(suscripcion.getId()).forEach(orden -> {
                    if (orden.getEstado() == EstadoPago.PENDIENTE) {
                        orden.aprobar();
                        ordenPagoRepository.guardar(orden);
                        log.info("Orden de pago actualizada a APROBADO: {}", orden.getId());
                    }
                });

                eventPublisher.publicarPagoConfirmado(suscripcion);
                return;
            }

            // 3. Si no es suscripción, buscar por OrdenPago
            OrdenPago ordenPago = ordenPagoRepository.buscarPorIdPagoExterno(idExterno)
                    .orElseThrow(() -> new PaymentProcessingException("No se encontró orden ni suscripción para el ID: " + idExterno));

            ordenPago.actualizarEstado(estadoPago);
            ordenPagoRepository.guardar(ordenPago);

            if (EstadoPago.APROBADO.name().equalsIgnoreCase(estadoPago)) {
                procesarPagoAprobado(ordenPago);
            } else {
                log.warn("Pago {} en estado {}. No se activan beneficios.", idExterno, estadoPago);
            }
        } finally {
            idempotencyPort.liberarLock(lockKey);
        }
    }

    private void procesarPagoAprobado(OrdenPago ordenPago) {
        Suscripcion suscripcion = suscripcionRepository.buscarPorId(ordenPago.getSuscripcionId())
                .orElseThrow(() -> new PaymentProcessingException("Suscripción no encontrada para la orden de pago ID: " + ordenPago.getId()));

        suscripcion.activar(LocalDate.now().plusMonths(1));
        suscripcionRepository.guardar(suscripcion);
        eventPublisher.publicarPagoConfirmado(suscripcion);
        log.info("Evento PagoConfirmado publicado para suscriptor: {}", suscripcion.getSuscriptorId());
    }
}