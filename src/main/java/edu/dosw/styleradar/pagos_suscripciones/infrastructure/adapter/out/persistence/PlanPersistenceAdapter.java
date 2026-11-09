package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.Monto;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.Plan;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.PlanRepositoryPort;
import edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.entity.PlanEntity;
import edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.repository.PlanJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PlanPersistenceAdapter implements PlanRepositoryPort {

    private final PlanJpaRepository planJpaRepository;

    @Override
    public Optional<Plan> buscarPorId(Long id) {
        return planJpaRepository.findById(id).map(this::toDomain);
    }

    private Plan toDomain(PlanEntity entity) {
        return new Plan(
                entity.getId(),
                entity.getNombre(),
                entity.getTipoSuscriptor(),
                new Monto(entity.getPrecio(), entity.getMoneda()),
                entity.getLimites()
        );
    }
}