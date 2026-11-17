package edu.dosw.styleradar.pagos.application.service;

import edu.dosw.styleradar.pagos.application.port.out.EventPublisherPort;
import edu.dosw.styleradar.pagos.application.port.out.MercadoPagoPort;
import edu.dosw.styleradar.pagos.application.port.out.OrdenPagoRepositoryPort;
import edu.dosw.styleradar.pagos.application.port.out.SuscripcionRepositoryPort;
import edu.dosw.styleradar.pagos.domain.event.PagoConfirmadoEvent;
import edu.dosw.styleradar.pagos.domain.model.EstadoPago;
import edu.dosw.styleradar.pagos.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos.domain.model.Suscripcion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Conciliación diaria de pagos pendientes con Mercado Pago.
 *
 * <p>SR-RNF-14: una vez al día se comparan los pagos registrados con la
 * pasarela y se corrigen las diferencias de estado. Si un pago aparece
 * APROBADO, se activa la suscripción con el mismo servicio que usa el webhook
 * (SR-RF-94/SR-RF-95).</p>
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "styleradar.scheduling", name = "enabled",
        havingValue = "true", matchIfMissing = true)
public class ConciliacionScheduler {

    private static final long HORAS_ESPERA = 2L;

    private final OrdenPagoRepositoryPort ordenPagoRepository;
    private final SuscripcionRepositoryPort suscripcionRepository;
    private final MercadoPagoPort mercadoPago;
    private final SuscripcionActivacionService activacionService;
    private final EventPublisherPort eventPublisher;
    private final Clock clock;

    public ConciliacionScheduler(OrdenPagoRepositoryPort ordenPagoRepository,
                                 SuscripcionRepositoryPort suscripcionRepository,
                                 MercadoPagoPort mercadoPago,
                                 SuscripcionActivacionService activacionService,
                                 EventPublisherPort eventPublisher,
                                 Clock clock) {
        this.ordenPagoRepository = ordenPagoRepository;
        this.suscripcionRepository = suscripcionRepository;
        this.mercadoPago = mercadoPago;
        this.activacionService = activacionService;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Scheduled(cron = "${styleradar.scheduling.conciliacion-cron:0 0 3 * * *}")
    public void conciliarPagosPendientes() {
        LocalDateTime limite = LocalDateTime.now(clock).minusHours(HORAS_ESPERA);
        List<OrdenPago> pendientes = ordenPagoRepository.buscarPendientesAntesDe(limite);
        int corregidas = 0;
        for (OrdenPago orden : pendientes) {
            if (orden.getIdPagoExterno() == null || orden.getIdPagoExterno().isBlank()) {
                continue;
            }
            try {
                MercadoPagoPort.PagoMp pago = mercadoPago.consultarPago(orden.getIdPagoExterno());
                if (pago == null) {
                    continue;
                }
                EstadoPago estadoReal = EstadoPagoMapper.desdeMercadoPago(pago.estado()).orElse(null);
                if (estadoReal == null || estadoReal == orden.getEstado()) {
                    continue;
                }
                orden.actualizarEstado(estadoReal, pago.id());
                OrdenPago guardada = ordenPagoRepository.guardar(orden);
                if (estadoReal == EstadoPago.APROBADO) {
                    activarYPublicar(guardada);
                }
                corregidas++;
            } catch (RuntimeException ex) {
                log.error("No fue posible conciliar la orden {}: {}", orden.getId(), ex.getMessage());
            }
        }
        log.info("Conciliación diaria: {} órdenes pendientes revisadas, {} corregidas",
                pendientes.size(), corregidas);
    }

    private void activarYPublicar(OrdenPago orden) {
        Suscripcion suscripcion = suscripcionRepository.buscarPorId(orden.getSuscripcionId())
                .orElseThrow(() -> new IllegalStateException(
                        "No se encontró la suscripción de la orden " + orden.getId()));
        Suscripcion activada = activacionService.activar(suscripcion, orden, LocalDate.now(clock));
        eventPublisher.publicarPagoConfirmado(PagoConfirmadoEvent.crear(
                new PagoConfirmadoEvent.Data(
                        orden.getId(),
                        activada.getId(),
                        activada.getSuscriptorId(),
                        activada.getTipoSuscriptor(),
                        activada.getPlanId(),
                        activada.getNit(),
                        activada.getAlmacenId(),
                        activada.getEstado().name()),
                null));
    }
}
