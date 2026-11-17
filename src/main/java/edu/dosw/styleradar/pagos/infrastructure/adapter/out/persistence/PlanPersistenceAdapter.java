package edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.pagos.application.port.out.PlanRepositoryPort;
import edu.dosw.styleradar.pagos.domain.model.Plan;
import edu.dosw.styleradar.pagos.domain.model.TipoSuscriptor;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence.repository.PlanJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/** Adaptador JPA del catálogo de planes. */
@Component
@RequiredArgsConstructor
public class PlanPersistenceAdapter implements PlanRepositoryPort {

    private final PlanJpaRepository planJpaRepository;

    @Override
    public Optional<Plan> buscarPorId(Long id) {
        return planJpaRepository.findById(id).map(PersistenceMapper::toDomain);
    }

    @Override
    public List<Plan> listarActivos() {
        return planJpaRepository.findByActivoTrueOrderByIdAsc().stream()
                .map(PersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Plan> buscarPlanDePagoPorTipo(TipoSuscriptor tipoSuscriptor) {
        return planJpaRepository
                .findFirstByTipoSuscriptorAndActivoTrueAndPrecioGreaterThanOrderByIdAsc(
                        tipoSuscriptor, BigDecimal.ZERO)
                .map(PersistenceMapper::toDomain);
    }
}
