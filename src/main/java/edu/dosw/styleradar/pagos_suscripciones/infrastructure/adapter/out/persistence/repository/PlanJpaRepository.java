package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.repository;

import edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.entity.PlanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanJpaRepository extends JpaRepository<PlanEntity, Long> {}