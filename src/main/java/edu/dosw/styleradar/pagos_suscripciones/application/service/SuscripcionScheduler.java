package edu.dosw.styleradar.pagos_suscripciones.application.service;

import edu.dosw.styleradar.pagos_suscripciones.application.dto.event.SuscripcionSuspendidaEvent;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.*;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.EventPublisherPort;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.SuscripcionRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuscripcionScheduler {

    private static final Long PLAN_GRATUITO_ID = 1L;

    private final SuscripcionRepositoryPort suscripcionRepository;
    private final EventPublisherPort eventPublisher;

    /**
     * SR-RF-99 (ALMACEN → SUSPENDIDA) y SR-RF-103 (USUARIO → PLAN GRATUITO).
     *
     * IMPORTANTE (v29/10/2026): el requerimiento ELIMINÓ el "periodo de gracia".
     * La suspensión/degradación es INMEDIATA al vencer el periodo pagado.
     * El aviso "antes y después" (RN-02) se delega a ms-notificaciones vía
     * el evento RabbitMQ publicado al final del método.
     *
     * ADR 09 (Contexto.txt) confirma: "No hay periodo de gracia. Si no paga,
     * pasa a VENCIDA o SUSPENDIDA inmediatamente."
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void revisarSuscripcionesVencidas() {
        log.info("Revisión diaria de suscripciones vencidas...");
        List<Suscripcion> vencidas = suscripcionRepository.buscarVencidas(LocalDate.now());

        for (Suscripcion s : vencidas) {
            log.warn("Suscripción vencida: suscriptor={}, planAnterior={}", s.getSuscriptorId(), s.getPlanId());
            Long planAnterior = s.getPlanId();

            s.marcarVencida();
            suscripcionRepository.guardar(s);

            if (s.getTipoSuscriptor() == TipoSuscriptor.USUARIO) {
                s.revertirAPlanGratuito(PLAN_GRATUITO_ID);   // SR-RF-103
            } else {
                s.suspenderPorFaltaDePago();                  // SR-RF-99
            }
            suscripcionRepository.guardar(s);

            SuscripcionSuspendidaEvent evt = new SuscripcionSuspendidaEvent(
                UUID.randomUUID().toString(),
                SuscripcionSuspendidaEvent.TYPE,
                SuscripcionSuspendidaEvent.VERSION,
                LocalDateTime.now(),
                s.getId(),
                s.getSuscriptorId(),
                s.getTipoSuscriptor(),
                planAnterior,
                s.getPlanId(),
                "Suscripción vencida sin pago aprobado"
            );
            eventPublisher.publicarSuscripcionSuspendida(evt);
        }
    }
}