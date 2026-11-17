package edu.dosw.styleradar.pagos.application.port.out;

import edu.dosw.styleradar.pagos.domain.model.Plan;
import edu.dosw.styleradar.pagos.domain.model.TipoSuscriptor;

import java.util.List;
import java.util.Optional;

/** Puerto de salida para el catálogo de planes. */
public interface PlanRepositoryPort {

    Optional<Plan> buscarPorId(Long id);

    List<Plan> listarActivos();

    /**
     * Plan de pago vigente para el tipo de suscriptor.
     * SR-RF-67: único plan de almacén. SR-RF-100: único plan de pago de usuario.
     */
    Optional<Plan> buscarPlanDePagoPorTipo(TipoSuscriptor tipoSuscriptor);
}
