package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.repository;

import edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.entity.OrdenPagoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrdenPagoJpaRepository extends JpaRepository<OrdenPagoEntity, Long> {
    Optional<OrdenPagoEntity> findByIdPagoExterno(String idPagoExterno);
    boolean existsByIdPagoExterno(String idPagoExterno);
}