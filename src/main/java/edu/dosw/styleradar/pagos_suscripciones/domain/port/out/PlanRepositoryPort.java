package edu.dosw.styleradar.pagos_suscripciones.domain.port.out;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.Plan;
import java.util.Optional;
import java.util.List;

public interface PlanRepositoryPort {
    Optional<Plan> buscarPorId(Long id);
    List<Plan> listarActivos();
}