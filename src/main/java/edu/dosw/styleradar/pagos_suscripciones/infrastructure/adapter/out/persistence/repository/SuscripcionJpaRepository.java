package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.repository;

import edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.entity.SuscripcionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SuscripcionJpaRepository extends JpaRepository<SuscripcionEntity, Long> {
    Optional<SuscripcionEntity> findBySuscriptorId(String suscriptorId);
    @Query("SELECT s FROM SuscripcionEntity s WHERE s.fechaFin < :fecha AND s.estado = 'ACTIVA'")
    List<SuscripcionEntity> findVencidas(@Param("fecha") LocalDate fecha);
    Optional<SuscripcionEntity> findByPreapprovalIdMp(String preapprovalIdMp);
}