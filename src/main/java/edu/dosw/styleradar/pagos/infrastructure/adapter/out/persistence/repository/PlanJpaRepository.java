package edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence.repository;

import edu.dosw.styleradar.pagos.domain.model.TipoSuscriptor;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence.entity.PlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PlanJpaRepository extends JpaRepository<PlanEntity, Long> {

    List<PlanEntity> findByActivoTrueOrderByIdAsc();

    Optional<PlanEntity> findFirstByTipoSuscriptorAndActivoTrueAndPrecioGreaterThanOrderByIdAsc(
            TipoSuscriptor tipoSuscriptor, BigDecimal precio);
}
