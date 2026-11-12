package edu.dosw.styleradar.pagos_suscripciones.application.service;

import edu.dosw.styleradar.pagos_suscripciones.application.dto.event.PagoConfirmadoEvent;
import edu.dosw.styleradar.pagos_suscripciones.domain.exception.PaymentProcessingException;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.*;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * SR-RF-94 / SR-RF-95 / SR-RNF-14.
 * Servicio único de activación/renovación, invocado tanto por el Webhook
 * como por el Scheduler de conciliación. Garantiza consistencia entre ambos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SuscripcionActivacionService {

    private final SuscripcionRepositoryPort suscripcionRepository;
    private final PlanRepositoryPort planRepository;
    private final EventPublisherPort eventPublisher;

    @Transactional
    public void activarYExtenderSuscripcion(OrdenPago orden) {
        Suscripcion sub = suscripcionRepository.buscarPorId(orden.getSuscripcionId())
                .orElseThrow(() -> new PaymentProcessingException(
                        "Suscripción no encontrada para la orden: " + orden.getId()));

        if (sub.estaActiva()) {
            sub.renovar();
        } else {
            sub.activar(LocalDate.now().plusMonths(1));
        }
        suscripcionRepository.guardar(sub);
        publicarPagoConfirmado(sub, orden);
        log.info("Suscripción {} activada/renovada por orden {}", sub.getId(), orden.getId());
    }

    private void publicarPagoConfirmado(Suscripcion sub, OrdenPago orden) {
        Plan plan = planRepository.buscarPorId(sub.getPlanId()).orElse(null);
        PagoConfirmadoEvent event = new PagoConfirmadoEvent(
                UUID.randomUUID().toString(),
                PagoConfirmadoEvent.TYPE,
                PagoConfirmadoEvent.VERSION,
                LocalDateTime.now(),
                sub.getId(),
                sub.getSuscriptorId(),
                sub.getTipoSuscriptor(),
                sub.getPlanId(),
                plan != null ? plan.getNombre() : null,
                plan != null ? plan.getLimites() : null,
                sub.getFechaInicio(),
                sub.getFechaFin(),
                orden.getMonto().valor(),
                orden.getMonto().moneda()
        );
        eventPublisher.publicarPagoConfirmado(event);
    }
}