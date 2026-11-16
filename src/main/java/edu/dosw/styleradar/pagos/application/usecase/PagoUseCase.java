package edu.dosw.styleradar.pagos.application.usecase;

import edu.dosw.styleradar.pagos.application.dto.response.MontoResponse;
import edu.dosw.styleradar.pagos.application.dto.response.PagoResponse;
import edu.dosw.styleradar.pagos.application.dto.response.ReembolsoResponse;
import edu.dosw.styleradar.pagos.application.dto.response.ReintentoResponse;
import edu.dosw.styleradar.pagos.application.port.in.PagoPort;
import edu.dosw.styleradar.pagos.application.port.out.MercadoPagoPort;
import edu.dosw.styleradar.pagos.application.port.out.OrdenPagoRepositoryPort;
import edu.dosw.styleradar.pagos.application.port.out.SuscripcionRepositoryPort;
import edu.dosw.styleradar.pagos.application.service.Roles;
import edu.dosw.styleradar.pagos.application.service.ValidadorEmail;
import edu.dosw.styleradar.pagos.domain.exception.Mensajes;
import edu.dosw.styleradar.pagos.domain.exception.NoAutorizadoException;
import edu.dosw.styleradar.pagos.domain.exception.OperacionNoPermitidaException;
import edu.dosw.styleradar.pagos.domain.exception.OrdenPagoNoEncontradaException;
import edu.dosw.styleradar.pagos.domain.exception.PaymentProcessingException;
import edu.dosw.styleradar.pagos.domain.exception.SolicitudInvalidaException;
import edu.dosw.styleradar.pagos.domain.exception.SuscripcionNoEncontradaException;
import edu.dosw.styleradar.pagos.domain.model.EstadoPago;
import edu.dosw.styleradar.pagos.domain.model.Monto;
import edu.dosw.styleradar.pagos.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos.domain.model.Suscripcion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Casos de uso de pagos: historial propio (SR-RF-97), reintento (SR-RF-96)
 * y reembolso total o parcial (SR-RF-98).
 */
@Slf4j
@Service
public class PagoUseCase implements PagoPort {

    private final OrdenPagoRepositoryPort ordenPagoRepository;
    private final SuscripcionRepositoryPort suscripcionRepository;
    private final MercadoPagoPort mercadoPago;
    private final Clock clock;
    private final String backUrl;

    public PagoUseCase(OrdenPagoRepositoryPort ordenPagoRepository,
                       SuscripcionRepositoryPort suscripcionRepository,
                       MercadoPagoPort mercadoPago,
                       Clock clock,
                       @Value("${mercadopago.back-url}") String backUrl) {
        this.ordenPagoRepository = ordenPagoRepository;
        this.suscripcionRepository = suscripcionRepository;
        this.mercadoPago = mercadoPago;
        this.clock = clock;
        this.backUrl = backUrl;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> historial(String suscriptorId) {
        if (suscriptorId == null || suscriptorId.isBlank()) {
            throw new NoAutorizadoException("Debe iniciar sesión para continuar.");
        }
        return ordenPagoRepository.buscarPorSuscriptor(suscriptorId).stream()
                .map(PagoResponse::desde)
                .toList();
    }

    @Override
    @Transactional
    public ReintentoResponse reintentar(Long ordenId, String emailPayer, String solicitanteId) {
        OrdenPago original = ordenPagoRepository.buscarPorId(ordenId)
                .orElseThrow(OrdenPagoNoEncontradaException::new);
        Suscripcion suscripcion = suscripcionRepository.buscarPorId(original.getSuscripcionId())
                .orElseThrow(SuscripcionNoEncontradaException::new);

        if (solicitanteId == null || solicitanteId.isBlank()) {
            throw new NoAutorizadoException("Debe iniciar sesión para continuar.");
        }
        if (!Roles.esTitular(suscripcion.getSuscriptorId(), solicitanteId)) {
            throw new OperacionNoPermitidaException(Mensajes.SOLO_TITULAR);
        }
        if (suscripcion.esActiva()) {
            throw new SolicitudInvalidaException(Mensajes.SUSCRIPCION_ACTIVA_NO_REINTENTO);
        }
        if (!original.estaRechazada()) {
            throw new SolicitudInvalidaException(Mensajes.ORDEN_NO_RECHAZADA);
        }
        if (!ValidadorEmail.esValido(emailPayer)) {
            throw new SolicitudInvalidaException(Mensajes.CORREO_INVALIDO);
        }

        OrdenPago nuevo = ordenPagoRepository.guardar(OrdenPago.pendiente(
                suscripcion.getId(), original.getMonto(), LocalDateTime.now(clock)));

        MercadoPagoPort.PreapprovalCreado preapproval;
        try {
            preapproval = mercadoPago.crearPreapproval(new MercadoPagoPort.PreapprovalSolicitud(
                    "Reintento de pago - StyleRadar",
                    "SUS-" + suscripcion.getId(),
                    emailPayer.trim(),
                    original.getMonto(),
                    backUrl));
        } catch (RuntimeException ex) {
            throw new PaymentProcessingException(Mensajes.NO_REINTENTO, ex);
        }
        if (preapproval == null || preapproval.initPoint() == null || preapproval.initPoint().isBlank()) {
            throw new PaymentProcessingException(Mensajes.NO_REINTENTO);
        }

        String preapprovalAnterior = suscripcion.getPreapprovalIdMp();
        suscripcion.asignarPreapproval(preapproval.id());
        suscripcionRepository.guardar(suscripcion);
        cancelarPreapprovalAnterior(preapprovalAnterior, preapproval.id());

        return new ReintentoResponse(
                nuevo.getId(),
                EstadoPago.PENDIENTE,
                preapproval.initPoint(),
                "Nuevo intento de pago generado. Continúa en Mercado Pago.");
    }

    @Override
    @Transactional
    public ReembolsoResponse reembolsar(Long ordenId, BigDecimal monto, String motivo, String rol) {
        if (!Roles.esAdmin(rol)) {
            throw new OperacionNoPermitidaException(Mensajes.SOLO_ADMIN);
        }
        OrdenPago orden = ordenPagoRepository.buscarPorId(ordenId)
                .orElseThrow(OrdenPagoNoEncontradaException::new);
        if (orden.getIdPagoExterno() == null || orden.getIdPagoExterno().isBlank()) {
            throw new PaymentProcessingException(Mensajes.NO_REEMBOLSO);
        }

        Monto montoReembolso = new Monto(monto, orden.getMonto().moneda());
        orden.reembolsar(montoReembolso, motivo);

        try {
            mercadoPago.reembolsar(orden.getIdPagoExterno(), montoReembolso.valor());
        } catch (RuntimeException ex) {
            throw new PaymentProcessingException(Mensajes.NO_REEMBOLSO, ex);
        }

        OrdenPago guardada = ordenPagoRepository.guardar(orden);
        boolean renovacionCancelada = cancelarRenovacionDeLaSuscripcion(guardada);

        String mensaje = guardada.esReembolsoParcial()
                ? "Reembolso parcial registrado. Saldo pendiente de reembolso: "
                    + guardada.saldoReembolsable().valor() + " " + guardada.saldoReembolsable().moneda() + "."
                : "Reembolso total registrado.";
        return new ReembolsoResponse(
                guardada.getId(),
                guardada.getEstado(),
                MontoResponse.desde(guardada.getMontoReembolsado()),
                MontoResponse.desde(guardada.saldoReembolsable()),
                guardada.esReembolsoParcial(),
                guardada.getMotivoReembolso(),
                renovacionCancelada,
                mensaje);
    }

    private boolean cancelarRenovacionDeLaSuscripcion(OrdenPago orden) {
        Suscripcion suscripcion = suscripcionRepository.buscarPorId(orden.getSuscripcionId())
                .orElseThrow(SuscripcionNoEncontradaException::new);
        if (!suscripcion.isAutoRenovacion()) {
            return true;
        }
        boolean canceladaEnMp = true;
        try {
            if (suscripcion.getPreapprovalIdMp() != null && !suscripcion.getPreapprovalIdMp().isBlank()) {
                mercadoPago.cancelarPreapproval(suscripcion.getPreapprovalIdMp());
            }
        } catch (RuntimeException ex) {
            canceladaEnMp = false;
            log.warn("No fue posible cancelar la preapproval {} tras el reembolso: {}",
                    suscripcion.getPreapprovalIdMp(), ex.getMessage());
        }
        if (suscripcion.esActiva()) {
            suscripcion.cancelarRenovacion();
            suscripcionRepository.guardar(suscripcion);
        }
        return canceladaEnMp;
    }

    private void cancelarPreapprovalAnterior(String anterior, String nueva) {
        if (anterior == null || anterior.isBlank() || anterior.equals(nueva)) {
            return;
        }
        try {
            mercadoPago.cancelarPreapproval(anterior);
        } catch (RuntimeException ex) {
            log.warn("No fue posible cancelar la preapproval anterior {}: {}", anterior, ex.getMessage());
        }
    }
}
