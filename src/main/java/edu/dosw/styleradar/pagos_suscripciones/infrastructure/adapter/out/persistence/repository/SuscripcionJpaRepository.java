package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.repository;

import edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.entity.SuscripcionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SuscripcionJpaRepository extends JpaRepository<SuscripcionEntity, Long> {
    Optional<SuscripcionEntity> findBySuscriptorId(String suscriptorId);
}