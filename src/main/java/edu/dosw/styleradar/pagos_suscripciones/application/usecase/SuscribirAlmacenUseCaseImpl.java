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
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuscribirAlmacenUseCaseImpl implements SuscripcionPort {

    private final PlanRepositoryPort planRepository;
    private final SuscripcionRepositoryPort suscripcionRepository;
    private final OrdenPagoRepositoryPort ordenPagoRepository;
    private final MercadoPagoPort mercadoPagoPort;

    @Override
    @Transactional
    public String iniciarSuscripcion(String idSuscriptor, Long idPlan, TipoSuscriptor tipoSuscriptor, String emailPayer) {
        log.info("Iniciando suscripción para {} con ID: {} al plan: {}", tipoSuscriptor, idSuscriptor, idPlan);
        Plan plan = planRepository.buscarPorId(idPlan)
                .orElseThrow(() -> new PlanNotFoundException("El plan con ID " + idPlan + " no existe."));

        if (!plan.isActivo()) {
            throw new IllegalArgumentException("El plan seleccionado no está disponible.");
        }

        // 1. Crear la Suscripción en estado PENDIENTE
        Suscripcion suscripcion = new Suscripcion(null, idSuscriptor, tipoSuscriptor, idPlan);
        suscripcion.setEstado(EstadoSuscripcion.PENDIENTE);
        suscripcion = suscripcionRepository.guardar(suscripcion);

        // 2. Crear la Orden de Pago
        String referenciaExterna = UUID.randomUUID().toString();
        OrdenPago nuevaOrden = new OrdenPago(null, suscripcion.getId(), plan.getPrecio(), referenciaExterna);
        ordenPagoRepository.guardar(nuevaOrden);

        // 3. Llamar a Mercado Pago
        Map<String, String> mpResponse = mercadoPagoPort.crearPreferenciaPago(nuevaOrden, suscripcion, emailPayer);
        
        // 4. Guardar el ID de preapproval en la suscripción
        suscripcion.setPreapprovalIdMp(mpResponse.get("preapproval_id"));
        suscripcionRepository.guardar(suscripcion);

        log.info("Preferencia de pago creada. Init point generado: {}", mpResponse.get("init_point"));
        return mpResponse.get("init_point");
    }

    @Override
    @Transactional
    public void cancelarRenovacion(String idSuscriptor) {
        log.info("Solicitud de cancelación de renovación para suscriptor: {}", idSuscriptor);
        Suscripcion suscripcion = suscripcionRepository.buscarPorSuscriptorId(idSuscriptor)
                .orElseThrow(() -> new SuscripcionNoEncontradaException("No se encontró suscripción para: " + idSuscriptor));

        suscripcion.cancelarRenovacion();
        suscripcionRepository.guardar(suscripcion);

        if (suscripcion.getPreapprovalIdMp() != null) {
            try {
                mercadoPagoPort.cancelarPreapproval(suscripcion.getPreapprovalIdMp());
            } catch (Exception e) {
                // Si MP falla, no rompemos la transacción local. Solo logueamos el error.
                log.warn("No se pudo cancelar el Preapproval en Mercado Pago: {}", e.getMessage());
            }
        }
        log.info("Renovación cancelada para suscriptor: {}", idSuscriptor);
    }

    @Override
    public Suscripcion consultarSuscripcion(String idSuscriptor) {
        return suscripcionRepository.buscarPorSuscriptorId(idSuscriptor)
                .orElseThrow(() -> new SuscripcionNoEncontradaException("No se encontró suscripción para: " + idSuscriptor));
    }

    @Override
    @Transactional
    public void programarCambioPlan(String idSuscriptor, Long nuevoPlanId) {
        log.info("Programando cambio de plan para suscriptor: {} al plan: {}", idSuscriptor, nuevoPlanId);
        
        if (planRepository.buscarPorId(nuevoPlanId).isEmpty()) {
            throw new PlanNotFoundException("El nuevo plan no existe");
        }

        Suscripcion suscripcion = suscripcionRepository.buscarPorSuscriptorId(idSuscriptor)
                .orElseThrow(() -> new SuscripcionNoEncontradaException("Suscripción no encontrada"));

        suscripcion.programarCambioPlan(nuevoPlanId);
        suscripcionRepository.guardar(suscripcion);
        log.info("Cambio de plan programado exitosamente.");
    }
}