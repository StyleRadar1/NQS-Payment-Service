package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.repository;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.EstadoPago;
import edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.entity.OrdenPagoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrdenPagoJpaRepository extends JpaRepository<OrdenPagoEntity, Long> {
    Optional<OrdenPagoEntity> findByIdPagoExterno(String idPagoExterno);
    boolean existsByIdPagoExterno(String idPagoExterno);
    List<OrdenPagoEntity> findBySuscripcionId(Long suscripcionId);
    List<OrdenPagoEntity> findByEstado(EstadoPago estado);

    @Query("SELECT o FROM OrdenPagoEntity o JOIN SuscripcionEntity s ON o.suscripcionId = s.id " +
           "WHERE s.suscriptorId = :suscriptorId ORDER BY o.fechaCreacion DESC")
    List<OrdenPagoEntity> findBySuscriptorId(@Param("suscriptorId") String suscriptorId);

    @Query("SELECT o FROM OrdenPagoEntity o WHERE o.estado = 'PENDIENTE' " +
           "AND o.fechaCreacion < :fechaLimite")
    List<OrdenPagoEntity> findPendientesAntesDe(@Param("fechaLimite") LocalDateTime fechaLimite);
}