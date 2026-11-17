package edu.dosw.styleradar.pagos.application.usecase;

import edu.dosw.styleradar.pagos.application.dto.request.IniciarSuscripcionRequest;
import edu.dosw.styleradar.pagos.application.dto.response.CancelacionResponse;
import edu.dosw.styleradar.pagos.application.dto.response.IniciarSuscripcionResponse;
import edu.dosw.styleradar.pagos.application.dto.response.PlanResponse;
import edu.dosw.styleradar.pagos.application.dto.response.SuscripcionResponse;
import edu.dosw.styleradar.pagos.application.port.in.SuscripcionPort;
import edu.dosw.styleradar.pagos.application.port.out.MercadoPagoPort;
import edu.dosw.styleradar.pagos.application.port.out.OrdenPagoRepositoryPort;
import edu.dosw.styleradar.pagos.application.port.out.PlanRepositoryPort;
import edu.dosw.styleradar.pagos.application.port.out.SuscripcionRepositoryPort;
import edu.dosw.styleradar.pagos.application.service.Roles;
import edu.dosw.styleradar.pagos.application.service.ValidadorEmail;
import edu.dosw.styleradar.pagos.domain.exception.Mensajes;
import edu.dosw.styleradar.pagos.domain.exception.NoAutorizadoException;
import edu.dosw.styleradar.pagos.domain.exception.OperacionNoPermitidaException;
import edu.dosw.styleradar.pagos.domain.exception.PaymentProcessingException;
import edu.dosw.styleradar.pagos.domain.exception.PlanNotFoundException;
import edu.dosw.styleradar.pagos.domain.exception.SolicitudInvalidaException;
import edu.dosw.styleradar.pagos.domain.exception.SuscripcionNoEncontradaException;
import edu.dosw.styleradar.pagos.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos.domain.model.Plan;
import edu.dosw.styleradar.pagos.domain.model.Suscripcion;
import edu.dosw.styleradar.pagos.domain.model.TipoSuscriptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Casos de uso del ciclo de vida de la suscripción:
 * iniciar pago (SR-RF-67/93/100), consultar (SR-RF-67/68/102)
 * y cancelar la renovación (SR-RF-68/101).
 */
@Service
public class SuscripcionUseCase implements SuscripcionPort {

    private final PlanRepositoryPort planRepository;
    private final SuscripcionRepositoryPort suscripcionRepository;
    private final OrdenPagoRepositoryPort ordenPagoRepository;
    private final MercadoPagoPort mercadoPago;
    private final Clock clock;
    private final String backUrl;

    public SuscripcionUseCase(PlanRepositoryPort planRepository,
                              SuscripcionRepositoryPort suscripcionRepository,
                              OrdenPagoRepositoryPort ordenPagoRepository,
                              MercadoPagoPort mercadoPago,
                              Clock clock,
                              @Value("${mercadopago.back-url}") String backUrl) {
        this.planRepository = planRepository;
        this.suscripcionRepository = suscripcionRepository;
        this.ordenPagoRepository = ordenPagoRepository;
        this.mercadoPago = mercadoPago;
        this.clock = clock;
        this.backUrl = backUrl;
    }

    @Override
    @Transactional
    public IniciarSuscripcionResponse iniciar(String suscriptorId, IniciarSuscripcionRequest request) {
        if (suscriptorId == null || suscriptorId.isBlank()) {
            throw new NoAutorizadoException("Debe iniciar sesión para continuar.");
        }
        if (request == null || request.tipoSuscriptor() == null) {
            throw new SolicitudInvalidaException("El tipo de suscriptor es obligatorio.");
        }
        TipoSuscriptor tipo = request.tipoSuscriptor();
        validarCorreo(request.emailPayer(), tipo);

        Plan plan = resolverPlan(request);
        Optional<Suscripcion> vigente = suscripcionRepository.buscarActivaOPendiente(suscriptorId);
        if (vigente.isPresent()) {
            throw new OperacionNoPermitidaException(tipo == TipoSuscriptor.ALMACEN
                    ? Mensajes.ALMACEN_YA_SUSCRITO
                    : Mensajes.RADAR_PLUS_ACTIVO);
        }

        Suscripcion suscripcion = suscripcionRepository.buscarUltimaPorSuscriptor(suscriptorId)
                .filter(s -> s.esSuspendida()
                        && s.esDeTipo(tipo)
                        && s.getPlanId().equals(plan.getId()))
                .orElseGet(() -> Suscripcion.pendiente(suscriptorId, tipo, plan.getId(), LocalDateTime.now(clock)));

        if (tipo == TipoSuscriptor.ALMACEN) {
            if (request.almacenId() == null) {
                throw new SolicitudInvalidaException(Mensajes.ALMACEN_ID_REQUERIDO);
            }
            suscripcion.asignarAlmacenId(request.almacenId());
            if (request.nit() != null && !request.nit().isBlank()) {
                suscripcion.asignarNit(request.nit());
            }
        }

        suscripcion = suscripcionRepository.guardar(suscripcion);

        MercadoPagoPort.PreapprovalCreado preapproval;
        try {
            preapproval = mercadoPago.crearPreapproval(new MercadoPagoPort.PreapprovalSolicitud(
                    "Suscripción " + plan.getNombre() + " - StyleRadar",
                    "SUS-" + suscripcion.getId(),
                    request.emailPayer().trim(),
                    plan.getPrecio(),
                    backUrl));
        } catch (RuntimeException ex) {
            throw new PaymentProcessingException(Mensajes.NO_INICIAR_PAGO, ex);
        }
        if (preapproval == null || preapproval.id() == null || preapproval.id().isBlank()
                || preapproval.initPoint() == null || preapproval.initPoint().isBlank()) {
            throw new PaymentProcessingException(Mensajes.NO_INICIAR_PAGO);
        }

        suscripcion.asignarPreapproval(preapproval.id());
        suscripcion = suscripcionRepository.guardar(suscripcion);

        OrdenPago orden = ordenPagoRepository.guardar(OrdenPago.pendiente(
                suscripcion.getId(), plan.getPrecio(), LocalDateTime.now(clock)));

        String mensaje = tipo == TipoSuscriptor.ALMACEN
                ? "Continúa a Mercado Pago para activar tu almacén."
                : "Continúa a Mercado Pago para activar Radar+.";
        return new IniciarSuscripcionResponse(
                suscripcion.getId(), orden.getId(), suscripcion.getEstado(), preapproval.initPoint(), mensaje);
    }

    @Override
    @Transactional(readOnly = true)
    public SuscripcionResponse consultar(String suscriptorId, String solicitanteId, String rol) {
        Suscripcion suscripcion = suscripcionRepository.buscarUltimaPorSuscriptor(suscriptorId)
                .orElseThrow(SuscripcionNoEncontradaException::new);
        validarAcceso(suscripcion, solicitanteId, rol);

        Plan plan = planRepository.buscarPorId(suscripcion.getPlanId())
                .orElseThrow(PlanNotFoundException::new);
        LocalDate hoy = LocalDate.now(clock);
        boolean acceso = suscripcion.permiteAcceso(hoy);
        String mensaje = mensajeEstado(suscripcion);
        return SuscripcionResponse.desde(suscripcion, PlanResponse.desde(plan), acceso, mensaje);
    }

    @Override
    @Transactional
    public CancelacionResponse cancelar(String suscriptorId,
                                        boolean confirmacion,
                                        String solicitanteId,
                                        String rol) {
        if (!confirmacion) {
            throw new SolicitudInvalidaException("Debes confirmar la cancelación de la renovación.");
        }
        Suscripcion suscripcion = suscripcionRepository.buscarUltimaPorSuscriptor(suscriptorId)
                .orElseThrow(SuscripcionNoEncontradaException::new);
        validarAcceso(suscripcion, solicitanteId, rol);

        if (!suscripcion.esActiva()) {
            throw new SolicitudInvalidaException(Mensajes.SUSCRIPCION_NO_VIGENTE);
        }
        if (!suscripcion.isAutoRenovacion()) {
            return new CancelacionResponse(
                    suscripcion.getId(), false, suscripcion.getEstado(), suscripcion.getFechaFin(),
                    true, "La renovación automática ya estaba desactivada.");
        }

        try {
            if (suscripcion.getPreapprovalIdMp() != null
                    && !suscripcion.getPreapprovalIdMp().isBlank()) {
                mercadoPago.cancelarPreapproval(suscripcion.getPreapprovalIdMp());
            }
        } catch (RuntimeException ex) {
            String mensaje = suscripcion.getTipoSuscriptor() == TipoSuscriptor.USUARIO
                    ? Mensajes.NO_CANCELAR_RADAR_PLUS
                    : Mensajes.NO_CANCELAR_RENOVACION;
            throw new PaymentProcessingException(mensaje, ex);
        }

        suscripcion.cancelarRenovacion();
        suscripcion = suscripcionRepository.guardar(suscripcion);

        String mensaje = suscripcion.getTipoSuscriptor() == TipoSuscriptor.USUARIO
                ? "La renovación de Radar+ fue cancelada. Mantienes los beneficios hasta "
                    + suscripcion.getFechaFin() + "."
                : "La renovación de tu suscripción fue cancelada. Mantienes el acceso hasta "
                    + suscripcion.getFechaFin() + ".";
        return new CancelacionResponse(
                suscripcion.getId(), false, suscripcion.getEstado(), suscripcion.getFechaFin(), true, mensaje);
    }

    private Plan resolverPlan(IniciarSuscripcionRequest request) {
        Plan plan = planRepository.buscarPlanDePagoPorTipo(request.tipoSuscriptor())
                .orElseThrow(PlanNotFoundException::new);
        if (request.planId() != null && !plan.getId().equals(request.planId())) {
            if (request.tipoSuscriptor() == TipoSuscriptor.USUARIO) {
                throw new PlanNotFoundException();
            }
            // SR-RF-67 RN-01: el almacén no elige plan; se usa el único vigente.
        }
        return plan;
    }

    private void validarCorreo(String correo, TipoSuscriptor tipo) {
        if (!ValidadorEmail.esValido(correo)) {
            throw new SolicitudInvalidaException(tipo == TipoSuscriptor.USUARIO
                    ? Mensajes.CORREO_INVALIDO_RADAR_PLUS
                    : Mensajes.CORREO_INVALIDO);
        }
    }

    private void validarAcceso(Suscripcion suscripcion, String solicitanteId, String rol) {
        if (solicitanteId == null || solicitanteId.isBlank()) {
            throw new NoAutorizadoException("Debe iniciar sesión para continuar.");
        }
        if (!Roles.esAdmin(rol) && !Roles.esTitular(suscripcion.getSuscriptorId(), solicitanteId)) {
            throw new OperacionNoPermitidaException(Mensajes.SOLO_TITULAR);
        }
    }

    private String mensajeEstado(Suscripcion suscripcion) {
        if (suscripcion.esSuspendida() && suscripcion.getTipoSuscriptor() == TipoSuscriptor.USUARIO) {
            return Mensajes.RADAR_PLUS_TERMINO;
        }
        if (suscripcion.esPendiente()) {
            return Mensajes.PAGO_PENDIENTE;
        }
        return null;
    }
}
