package edu.dosw.styleradar.pagos.application.service;

import edu.dosw.styleradar.pagos.application.port.out.EventPublisherPort;
import edu.dosw.styleradar.pagos.application.port.out.SuscripcionRepositoryPort;
import edu.dosw.styleradar.pagos.domain.event.SuscripcionSuspendidaEvent;
import edu.dosw.styleradar.pagos.domain.model.Suscripcion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/**
 * Suspensión diaria de suscripciones vencidas sin pago aprobado.
 *
 * <p>SR-RF-99 y SR-RF-103: no existe periodo de gracia; el mismo job pasa
 * el almacén a SUSPENDIDA y devuelve al usuario al plan Radar. Se publica
 * {@code styleradar.pagos.suscripcion.suspendida} para que catálogo, tiendas,
 * inventario y notificaciones actúen (SR-RF-99 RN-02).</p>
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "styleradar.scheduling", name = "enabled",
        havingValue = "true", matchIfMissing = true)
public class SuscripcionScheduler {

    private final SuscripcionRepositoryPort suscripcionRepository;
    private final EventPublisherPort eventPublisher;
    private final Clock clock;

    public SuscripcionScheduler(SuscripcionRepositoryPort suscripcionRepository,
                                EventPublisherPort eventPublisher,
                                Clock clock) {
        this.suscripcionRepository = suscripcionRepository;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Scheduled(cron = "${styleradar.scheduling.suspension-cron:0 10 0 * * *}")
    public void revisarSuscripcionesVencidas() {
        LocalDate hoy = LocalDate.now(clock);
        List<Suscripcion> vencidas = suscripcionRepository.buscarActivasVencidas(hoy);
        int suspendidas = 0;
        for (Suscripcion suscripcion : vencidas) {
            try {
                suscripcion.suspenderPorFaltaDePago(hoy);
                Suscripcion guardada = suscripcionRepository.guardar(suscripcion);
                eventPublisher.publicarSuscripcionSuspendida(SuscripcionSuspendidaEvent.crear(
                        new SuscripcionSuspendidaEvent.Data(
                                guardada.getId(),
                                guardada.getSuscriptorId(),
                                guardada.getTipoSuscriptor(),
                                guardada.getPlanId(),
                                guardada.getNit(),
                                guardada.getAlmacenId(),
                                guardada.getFechaFin(),
                                "FALTA_DE_PAGO"),
                        null));
                suspendidas++;
            } catch (RuntimeException ex) {
                log.error("No fue posible suspender la suscripción {}: {}",
                        suscripcion.getId(), ex.getMessage());
            }
        }
        log.info("Suspensión diaria: {} suscripciones vencidas procesadas, {} suspendidas",
                vencidas.size(), suspendidas);
    }
}
