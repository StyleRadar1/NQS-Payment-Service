package edu.dosw.styleradar.pagos_suscripciones.application.usecase;

import edu.dosw.styleradar.pagos_suscripciones.domain.exception.PlanNotFoundException;
import edu.dosw.styleradar.pagos_suscripciones.domain.exception.SuscripcionNoEncontradaException;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.*;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.in.SuscripcionPort;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuscribirAlmacenUseCaseImpl implements SuscripcionPort {

    private static final String MP_INIT_POINT = "init_point";
    private static final String MP_PREAPPROVAL_ID = "preapproval_id";

    private final PlanRepositoryPort planRepository;
    private final SuscripcionRepositoryPort suscripcionRepository;
    private final OrdenPagoRepositoryPort ordenPagoRepository;
    private final MercadoPagoPort mercadoPagoPort;

    @Override
@Transactional
public String iniciarSuscripcion(String idSuscriptor, Long idPlan,
                                 TipoSuscriptor tipoSuscriptor, String emailPayer) {
    log.info("Iniciando suscripción {} → plan {}", idSuscriptor, idPlan);

    Plan plan = planRepository.buscarPorId(idPlan)
            .orElseThrow(() -> new PlanNotFoundException("El plan " + idPlan + " no existe."));
    if (!plan.isActivo()) {
        throw new IllegalArgumentException("El plan seleccionado no está disponible.");
    }
    if (plan.getTipoSuscriptor() != tipoSuscriptor) {
        throw new IllegalArgumentException(
            "El plan '" + plan.getNombre() + "' es de tipo " + plan.getTipoSuscriptor()
            + " y el suscriptor es de tipo " + tipoSuscriptor + ".");
    }

    Suscripcion suscripcion;
    Optional<Suscripcion> existente = suscripcionRepository.buscarPorSuscriptorId(idSuscriptor);

    if (existente.isPresent()) {
        Suscripcion ex = existente.get();
        if (ex.estaActiva() || ex.estaPendiente()) {
            throw new IllegalStateException(
                "El suscriptor ya tiene una suscripción " + ex.getEstado()
                + ". Use /suscripciones/{id}/cambiar-plan para modificarla.");
        }
        if (ex.getTipoSuscriptor() != tipoSuscriptor) {
            throw new IllegalArgumentException(
                "No puede cambiar de tipo de suscriptor (" + ex.getTipoSuscriptor()
                + " → " + tipoSuscriptor + "). Contacte a soporte.");
        }
        // FIX: reutilizar la fila existente (UNIQUE por suscriptor).
        suscripcion = ex;
        suscripcion.setPlanId(idPlan);
        suscripcion.setEstado(EstadoSuscripcion.PENDIENTE);
        suscripcion.setPreapprovalIdMp(null);
    } else {
        suscripcion = new Suscripcion(null, idSuscriptor, tipoSuscriptor, idPlan);
        suscripcion.setEstado(EstadoSuscripcion.PENDIENTE);
    }

    suscripcion = suscripcionRepository.guardar(suscripcion);

    OrdenPago orden = new OrdenPago(null, suscripcion.getId(), plan.getPrecio(), null);
    orden = ordenPagoRepository.guardar(orden);

    Map<String, String> mp = mercadoPagoPort.crearPreferenciaPago(orden, suscripcion, emailPayer);

    suscripcion.setPreapprovalIdMp(mp.get(MP_PREAPPROVAL_ID));
    suscripcionRepository.guardar(suscripcion);

    log.info("Preferencia creada para {}, init_point={}", idSuscriptor, mp.get(MP_INIT_POINT));
    return mp.get(MP_INIT_POINT);
}

    @Override
    @Transactional
    public void cancelarRenovacion(String idSuscriptor) {
        Suscripcion s = suscripcionRepository.buscarPorSuscriptorId(idSuscriptor)
                .orElseThrow(() -> new SuscripcionNoEncontradaException(
                    "No se encontró suscripción para: " + idSuscriptor));

        if (!s.isAutoRenovacion()) {
            log.info("Renovación ya cancelada previamente para {}", idSuscriptor);
            return;
        }

        s.cancelarRenovacion();
        suscripcionRepository.guardar(s);

        if (s.getPreapprovalIdMp() != null) {
            try {
                mercadoPagoPort.cancelarPreapproval(s.getPreapprovalIdMp());
            } catch (Exception e) {
                log.warn("No se pudo cancelar preapproval en MP: {}", e.getMessage());
            }
        }
    }

    @Override
    public Suscripcion consultarSuscripcion(String idSuscriptor) {
        return suscripcionRepository.buscarPorSuscriptorId(idSuscriptor)
                .orElseThrow(() -> new SuscripcionNoEncontradaException(
                    "No se encontró suscripción para: " + idSuscriptor));
    }

    @Override
    @Transactional
    public String programarCambioPlan(String idSuscriptor, Long nuevoPlanId, String emailPayer) {
        log.info("Cambio de plan: {} → {}", idSuscriptor, nuevoPlanId);

        Suscripcion s = suscripcionRepository.buscarPorSuscriptorId(idSuscriptor)
                .orElseThrow(() -> new SuscripcionNoEncontradaException("Suscripción no encontrada"));

        if (!s.estaActiva()) {
            throw new IllegalStateException("Solo se puede cambiar de plan si la suscripción está ACTIVA.");
        }

        Plan planActual = planRepository.buscarPorId(s.getPlanId())
                .orElseThrow(() -> new PlanNotFoundException("Plan actual no encontrado."));
        Plan nuevoPlan = planRepository.buscarPorId(nuevoPlanId)
                .orElseThrow(() -> new PlanNotFoundException("El nuevo plan no existe."));

        if (!nuevoPlan.isActivo()) {
            throw new IllegalArgumentException("El plan seleccionado no está disponible.");
        }

        if (nuevoPlan.getTipoSuscriptor() != s.getTipoSuscriptor()) {
            throw new IllegalArgumentException(
                "No se puede cambiar a un plan de tipo " + nuevoPlan.getTipoSuscriptor()
                + ". Su suscripción actual es de tipo " + s.getTipoSuscriptor() + ".");
        }

        if (nuevoPlanId.equals(s.getPlanId())) {
            throw new IllegalArgumentException("Ya tiene el plan '" + nuevoPlan.getNombre() + "'.");
        }

        int cmp = nuevoPlan.getPrecio().valor().compareTo(planActual.getPrecio().valor());

        if (cmp > 0) {
            Monto diferencia = new Monto(
                nuevoPlan.getPrecio().valor().subtract(planActual.getPrecio().valor()),
                nuevoPlan.getPrecio().moneda());

            OrdenPago ordenUpgrade = new OrdenPago(null, s.getId(), diferencia, null);
            ordenUpgrade = ordenPagoRepository.guardar(ordenUpgrade);

            s.programarCambioPlan(nuevoPlanId);
            suscripcionRepository.guardar(s);

            Map<String, String> mp = mercadoPagoPort.crearPreferenciaPago(ordenUpgrade, s, emailPayer);
            return mp.get(MP_INIT_POINT);
        } else {
            s.programarCambioPlan(nuevoPlanId);
            suscripcionRepository.guardar(s);
            return null;
        }
    }
}