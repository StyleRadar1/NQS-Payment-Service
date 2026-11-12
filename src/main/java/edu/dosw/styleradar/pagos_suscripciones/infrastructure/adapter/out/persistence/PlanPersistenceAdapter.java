package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.Monto;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.Plan;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.PlanRepositoryPort;
import edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.entity.PlanEntity;
import edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.repository.PlanJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PlanPersistenceAdapter implements PlanRepositoryPort {

    private final PlanJpaRepository repo;

    @Override
    public Optional<Plan> buscarPorId(Long id) {
        return repo.findById(id).map(this::toDomain);
    }

    private Plan toDomain(PlanEntity e) {
        Plan p = new Plan(e.getId(), e.getNombre(), e.getTipoSuscriptor(),
                          new Monto(e.getPrecio(), e.getMoneda()), e.getLimites());
        if (!e.isActivo()) p.desactivar();
        return p;
    }

    public List<Plan> listarActivos() {
        return repo.findByActivoTrue().stream().map(this::toDomain).toList();
    }
}