package edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence.repository;

import edu.dosw.styleradar.pagos.domain.model.EstadoSuscripcion;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence.entity.SuscripcionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SuscripcionJpaRepository extends JpaRepository<SuscripcionEntity, Long> {

    Optional<SuscripcionEntity> findFirstBySuscriptorIdOrderByIdDesc(String suscriptorId);

    Optional<SuscripcionEntity> findFirstBySuscriptorIdAndEstadoInOrderByIdDesc(
            String suscriptorId, Collection<EstadoSuscripcion> estados);

    Optional<SuscripcionEntity> findFirstByPreapprovalIdMp(String preapprovalIdMp);

    List<SuscripcionEntity> findByEstadoAndFechaFinBefore(EstadoSuscripcion estado, LocalDate fecha);
}
