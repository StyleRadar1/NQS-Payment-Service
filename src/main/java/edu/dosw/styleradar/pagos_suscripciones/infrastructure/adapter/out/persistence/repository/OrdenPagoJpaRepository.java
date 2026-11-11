package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.repository;

import edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.entity.OrdenPagoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrdenPagoJpaRepository extends JpaRepository<OrdenPagoEntity, Long> {
    Optional<OrdenPagoEntity> findByIdPagoExterno(String idPagoExterno);
    boolean existsByIdPagoExterno(String idPagoExterno);
    @Query("SELECT o FROM OrdenPagoEntity o JOIN SuscripcionEntity s ON o.suscripcionId = s.id WHERE s.suscriptorId = :suscriptorId ORDER BY o.fechaCreacion DESC")
    List<OrdenPagoEntity> findBySuscriptorId(@Param("suscriptorId") String suscriptorId);
}