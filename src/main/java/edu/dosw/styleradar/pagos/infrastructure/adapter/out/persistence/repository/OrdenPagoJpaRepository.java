package edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence.repository;

import edu.dosw.styleradar.pagos.domain.model.EstadoPago;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence.entity.OrdenPagoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrdenPagoJpaRepository extends JpaRepository<OrdenPagoEntity, Long> {

    Optional<OrdenPagoEntity> findByIdPagoExterno(String idPagoExterno);

    Optional<OrdenPagoEntity> findFirstBySuscripcionIdAndEstadoOrderByFechaCreacionDesc(
            Long suscripcionId, EstadoPago estado);

    List<OrdenPagoEntity> findBySuscripcionIdOrderByFechaCreacionDesc(Long suscripcionId);

    List<OrdenPagoEntity> findBySuscripcion_SuscriptorIdOrderByFechaCreacionDesc(String suscriptorId);

    List<OrdenPagoEntity> findAllByOrderByFechaCreacionDesc();

    List<OrdenPagoEntity> findByEstadoOrderByFechaCreacionDesc(EstadoPago estado);

    List<OrdenPagoEntity> findByEstadoAndFechaCreacionBefore(EstadoPago estado, LocalDateTime limite);
}
