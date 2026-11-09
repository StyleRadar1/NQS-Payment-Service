package edu.dosw.styleradar.pagos_suscripciones.application.usecase;

import edu.dosw.styleradar.pagos_suscripciones.domain.exception.IdempotencyException;
import edu.dosw.styleradar.pagos_suscripciones.domain.exception.PaymentProcessingException;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.EstadoPago;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.Suscripcion;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.in.WebhookPort;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.EventPublisherPort;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.IdempotencyPort;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.OrdenPagoRepositoryPort;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.SuscripcionRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcesarWebhookUseCaseImpl implements WebhookPort {

    private final IdempotencyPort idempotencyPort;
    private final OrdenPagoRepositoryPort ordenPagoRepository;
    private final SuscripcionRepositoryPort suscripcionRepository;
    private final EventPublisherPort eventPublisher;

    @Override
    @Transactional
    public void procesarNotificacionPago(String idPagoExterno, String estadoPago, String tipoEvento) {
        log.info("Iniciando procesamiento de webhook para pago: {} con estado: {}", idPagoExterno, estadoPago);
        String lockKey = "lock:pago_" + idPagoExterno;

        if (!idempotencyPort.adquirirLock(lockKey)) {
            log.warn("Webhook duplicado detectado concurrentemente para pago: {}", idPagoExterno);
            throw new IdempotencyException("El pago ya está siendo procesado por otra instancia.");
        }

        try {
            if (ordenPagoRepository.existePorIdPagoExterno(idPagoExterno)) {
                log.info("Webhook ya procesado previamente (DB) para pago: {}", idPagoExterno);
                return;
            }

            OrdenPago ordenPago = ordenPagoRepository.buscarPorIdPagoExterno(idPagoExterno)
                    .orElseThrow(() -> new PaymentProcessingException("No se encontró la orden de pago para el ID externo: " + idPagoExterno));

            ordenPago.actualizarEstado(estadoPago);
            ordenPagoRepository.guardar(ordenPago);

            if (EstadoPago.APROBADO.name().equalsIgnoreCase(estadoPago)) {
                procesarPagoAprobado(ordenPago);
            } else {
                log.warn("Pago {} en estado {}. No se activan beneficios.", idPagoExterno, estadoPago);
            }
        } finally {
            idempotencyPort.liberarLock(lockKey);
        }
    }

    private void procesarPagoAprobado(OrdenPago ordenPago) {
        log.info("Pago aprobado. Activando suscripción ID: {}", ordenPago.getSuscripcionId());
        Suscripcion suscripcion = suscripcionRepository.buscarPorId(ordenPago.getSuscripcionId())
                .orElseThrow(() -> new PaymentProcessingException("Suscripción no encontrada para la orden de pago ID: " + ordenPago.getId()));

        suscripcion.activar(LocalDate.now().plusMonths(1));
        suscripcionRepository.guardar(suscripcion);

        eventPublisher.publicarPagoConfirmado(suscripcion);
        log.info("Evento PagoConfirmado publicado para suscriptor: {}", suscripcion.getSuscriptorId());
    }
}