package edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.pagos.application.port.out.OrdenPagoRepositoryPort;
import edu.dosw.styleradar.pagos.domain.model.EstadoPago;
import edu.dosw.styleradar.pagos.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence.repository.OrdenPagoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Adaptador JPA de órdenes de pago. */
@Component
@RequiredArgsConstructor
public class OrdenPagoPersistenceAdapter implements OrdenPagoRepositoryPort {

    private final OrdenPagoJpaRepository ordenPagoJpaRepository;

    @Override
    public OrdenPago guardar(OrdenPago ordenPago) {
        return PersistenceMapper.toDomain(
                ordenPagoJpaRepository.save(PersistenceMapper.toEntity(ordenPago)));
    }

    @Override
    public Optional<OrdenPago> buscarPorId(Long id) {
        return ordenPagoJpaRepository.findById(id).map(PersistenceMapper::toDomain);
    }

    @Override
    public Optional<OrdenPago> buscarPorIdPagoExterno(String idPagoExterno) {
        return ordenPagoJpaRepository.findByIdPagoExterno(idPagoExterno)
                .map(PersistenceMapper::toDomain);
    }

    @Override
    public Optional<OrdenPago> buscarPendientePorSuscripcion(Long suscripcionId) {
        return ordenPagoJpaRepository
                .findFirstBySuscripcionIdAndEstadoOrderByFechaCreacionDesc(suscripcionId, EstadoPago.PENDIENTE)
                .map(PersistenceMapper::toDomain);
    }

    @Override
    public List<OrdenPago> buscarPorSuscripcion(Long suscripcionId) {
        return ordenPagoJpaRepository.findBySuscripcionIdOrderByFechaCreacionDesc(suscripcionId)
                .stream().map(PersistenceMapper::toDomain).toList();
    }

    @Override
    public List<OrdenPago> buscarPorSuscriptor(String suscriptorId) {
        return ordenPagoJpaRepository
                .findBySuscripcion_SuscriptorIdOrderByFechaCreacionDesc(suscriptorId)
                .stream().map(PersistenceMapper::toDomain).toList();
    }

    @Override
    public List<OrdenPago> listar(EstadoPago estado) {
        List<edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence.entity.OrdenPagoEntity> entities =
                estado == null
                        ? ordenPagoJpaRepository.findAllByOrderByFechaCreacionDesc()
                        : ordenPagoJpaRepository.findByEstadoOrderByFechaCreacionDesc(estado);
        return entities.stream().map(PersistenceMapper::toDomain).toList();
    }

    @Override
    public List<OrdenPago> buscarPendientesAntesDe(LocalDateTime limite) {
        return ordenPagoJpaRepository
                .findByEstadoAndFechaCreacionBefore(EstadoPago.PENDIENTE, limite)
                .stream().map(PersistenceMapper::toDomain).toList();
    }
}
