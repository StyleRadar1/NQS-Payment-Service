package edu.dosw.styleradar.pagos.application.usecase;

import edu.dosw.styleradar.pagos.application.dto.request.NotificacionPago;
import edu.dosw.styleradar.pagos.application.port.in.WebhookPort;
import edu.dosw.styleradar.pagos.application.port.out.EventPublisherPort;
import edu.dosw.styleradar.pagos.application.port.out.IdempotencyPort;
import edu.dosw.styleradar.pagos.application.port.out.MercadoPagoPort;
import edu.dosw.styleradar.pagos.application.port.out.OrdenPagoRepositoryPort;
import edu.dosw.styleradar.pagos.application.port.out.SuscripcionRepositoryPort;
import edu.dosw.styleradar.pagos.application.port.out.WebhookLogPort;
import edu.dosw.styleradar.pagos.application.service.EstadoPagoMapper;
import edu.dosw.styleradar.pagos.application.service.SuscripcionActivacionService;
import edu.dosw.styleradar.pagos.domain.event.PagoConfirmadoEvent;
import edu.dosw.styleradar.pagos.domain.exception.IdempotencyException;
import edu.dosw.styleradar.pagos.domain.exception.PaymentProcessingException;
import edu.dosw.styleradar.pagos.domain.exception.SolicitudInvalidaException;
import edu.dosw.styleradar.pagos.domain.exception.SuscripcionNoEncontradaException;
import edu.dosw.styleradar.pagos.domain.model.EstadoPago;
import edu.dosw.styleradar.pagos.domain.model.Monto;
import edu.dosw.styleradar.pagos.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos.domain.model.Suscripcion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Procesa las notificaciones de Mercado Pago.
 *
 * <p>SR-RF-94: valida la identidad del pago, consulta el estado real en la
 * pasarela y solo activa o extiende la suscripción con un pago APROBADO.
 * SR-RNF-14: idempotencia por Redis + {@code webhooks_log} único + verificación
 * de transición de estado. Los avisos de preapproval no modifican pagos
 * (SR-RF-94 RN-04).</p>
 */
@Slf4j
@Service
public class WebhookUseCase implements WebhookPort {

    private static final Duration TTL_LOCK = Duration.ofSeconds(30);
    private static final String PREFIJO_REFERENCIA = "SUS-";

    private final OrdenPagoRepositoryPort ordenPagoRepository;
    private final SuscripcionRepositoryPort suscripcionRepository;
    private final MercadoPagoPort mercadoPago;
    private final IdempotencyPort idempotencyPort;
    private final WebhookLogPort webhookLogPort;
    private final SuscripcionActivacionService activacionService;
    private final EventPublisherPort eventPublisher;
    private final Clock clock;

    public WebhookUseCase(OrdenPagoRepositoryPort ordenPagoRepository,
                          SuscripcionRepositoryPort suscripcionRepository,
                          MercadoPagoPort mercadoPago,
                          IdempotencyPort idempotencyPort,
                          WebhookLogPort webhookLogPort,
                          SuscripcionActivacionService activacionService,
                          EventPublisherPort eventPublisher,
                          Clock clock) {
        this.ordenPagoRepository = ordenPagoRepository;
        this.suscripcionRepository = suscripcionRepository;
        this.mercadoPago = mercadoPago;
        this.idempotencyPort = idempotencyPort;
        this.webhookLogPort = webhookLogPort;
        this.activacionService = activacionService;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void procesar(NotificacionPago notificacion, String correlationId) {
        if (notificacion == null || notificacion.idPago() == null || notificacion.idPago().isBlank()) {
            throw new SolicitudInvalidaException("La notificación no contiene el identificador del pago.");
        }
        if (notificacion.tipo() != null && !esNotificacionDePago(notificacion.tipo())) {
            // SR-RF-94 RN-04: avisos de preapproval no modifican el estado de un pago.
            log.info("Notificación ignorada por tipo {} (pago {})", notificacion.tipo(), notificacion.idPago());
            return;
        }

        String clave = "lock:pago_" + notificacion.idPago();
        if (!idempotencyPort.intentarBloquear(clave, TTL_LOCK)) {
            throw new IdempotencyException();
        }
        try {
            procesarConBloqueo(notificacion, correlationId);
        } finally {
            idempotencyPort.liberar(clave);
        }
    }

    private void procesarConBloqueo(NotificacionPago notificacion, String correlationId) {
        if (webhookLogPort.existe(notificacion.idPago())) {
            log.info("Webhook duplicado para el pago {}; se omite", notificacion.idPago());
            return;
        }

        MercadoPagoPort.PagoMp pago = consultarPago(notificacion.idPago());
        OrdenPago orden = localizarOrden(notificacion.idPago(), pago);
        if (orden == null) {
            log.warn("No se pudo asociar el pago {} a una suscripción de StyleRadar", notificacion.idPago());
            return;
        }

        EstadoPago nuevoEstado = EstadoPagoMapper.desdeMercadoPago(pago.estado())
                .orElse(null);
        if (nuevoEstado == null) {
            log.warn("Estado de Mercado Pago no reconocido: {} para el pago {}",
                    pago.estado(), notificacion.idPago());
            return;
        }

        boolean transicion = orden.getEstado() != nuevoEstado;
        if (transicion) {
            orden.actualizarEstado(nuevoEstado, notificacion.idPago());
            orden = ordenPagoRepository.guardar(orden);
        }

        if (nuevoEstado == EstadoPago.APROBADO && transicion) {
            activarSuscripcion(orden, correlationId);
        }

        Map<String, Object> payload = notificacion.payload() == null ? Map.of() : notificacion.payload();
        webhookLogPort.registrar(notificacion.idPago(), payload, "PROCESADO");
    }

    private MercadoPagoPort.PagoMp consultarPago(String idPago) {
        try {
            MercadoPagoPort.PagoMp pago = mercadoPago.consultarPago(idPago);
            if (pago == null || pago.id() == null || pago.id().isBlank()) {
                throw new PaymentProcessingException(
                        "No fue posible consultar el estado del pago en Mercado Pago.");
            }
            return pago;
        } catch (PaymentProcessingException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new PaymentProcessingException(
                    "No fue posible consultar el estado del pago en Mercado Pago.", ex);
        }
    }

    private OrdenPago localizarOrden(String idPago, MercadoPagoPort.PagoMp pago) {
        Optional<OrdenPago> porExterno = ordenPagoRepository.buscarPorIdPagoExterno(
                pago.id() == null ? idPago : pago.id());
        if (porExterno.isPresent()) {
            return porExterno.get();
        }

        Suscripcion suscripcion = buscarSuscripcion(pago);
        if (suscripcion == null || suscripcion.getId() == null) {
            return null;
        }

        Optional<OrdenPago> pendiente = ordenPagoRepository.buscarPendientePorSuscripcion(suscripcion.getId());
        if (pendiente.isPresent()) {
            return pendiente.get();
        }

        // SR-RF-95: el cobro recurrente llega sin orden local; se registra
        // en el historial con el monto confirmado por la pasarela.
        Monto monto = montoDelPago(pago, suscripcion.getId());
        return ordenPagoRepository.guardar(OrdenPago.pendiente(
                suscripcion.getId(), monto, LocalDateTime.now(clock)));
    }

    private Suscripcion buscarSuscripcion(MercadoPagoPort.PagoMp pago) {
        Long idSuscripcion = parsearSuscripcion(pago.referenciaExterna());
        if (idSuscripcion != null) {
            Optional<Suscripcion> porReferencia = suscripcionRepository.buscarPorId(idSuscripcion);
            if (porReferencia.isPresent()) {
                return porReferencia.get();
            }
        }
        if (pago.preapprovalId() != null && !pago.preapprovalId().isBlank()) {
            return suscripcionRepository.buscarPorPreapprovalId(pago.preapprovalId()).orElse(null);
        }
        return null;
    }

    private Monto montoDelPago(MercadoPagoPort.PagoMp pago, Long suscripcionId) {
        if (pago.monto() != null) {
            String moneda = pago.moneda() == null || pago.moneda().isBlank() ? "COP" : pago.moneda();
            return new Monto(pago.monto(), moneda);
        }
        return ordenPagoRepository.buscarPorSuscripcion(suscripcionId).stream()
                .findFirst()
                .map(OrdenPago::getMonto)
                .orElseThrow(() -> new PaymentProcessingException(
                        "No fue posible determinar el monto del pago recurrente."));
    }

    private Long parsearSuscripcion(String referenciaExterna) {
        if (referenciaExterna == null || !referenciaExterna.startsWith(PREFIJO_REFERENCIA)) {
            return null;
        }
        String[] partes = referenciaExterna.split("-");
        if (partes.length < 2) {
            return null;
        }
        try {
            return Long.valueOf(partes[1]);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private void activarSuscripcion(OrdenPago orden, String correlationId) {
        Suscripcion suscripcion = suscripcionRepository.buscarPorId(orden.getSuscripcionId())
                .orElseThrow(SuscripcionNoEncontradaException::new);
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
                correlationId));
    }

    private boolean esNotificacionDePago(String tipo) {
        return "payment".equalsIgnoreCase(tipo);
    }
}
