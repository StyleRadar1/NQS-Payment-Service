package edu.dosw.styleradar.pagos_suscripciones.domain.port.out;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.Plan;
import java.util.Optional;

public interface PlanRepositoryPort {
    Optional<Plan> buscarPorId(Long id);
}