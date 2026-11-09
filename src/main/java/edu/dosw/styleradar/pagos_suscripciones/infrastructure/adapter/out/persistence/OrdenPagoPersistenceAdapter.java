package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.EstadoPago;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.Monto;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.OrdenPagoRepositoryPort;
import edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.entity.OrdenPagoEntity;
import edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.repository.OrdenPagoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrdenPagoPersistenceAdapter implements OrdenPagoRepositoryPort {

    private final OrdenPagoJpaRepository repository;

    @Override
    public OrdenPago guardar(OrdenPago orden) {
        OrdenPagoEntity entity = toEntity(orden);
        OrdenPagoEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<OrdenPago> buscarPorId(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<OrdenPago> buscarPorIdPagoExterno(String idPagoExterno) {
        return repository.findByIdPagoExterno(idPagoExterno).map(this::toDomain);
    }

    @Override
    public boolean existePorIdPagoExterno(String idPagoExterno) {
        return repository.existsByIdPagoExterno(idPagoExterno);
    }

    @Override
    public List<OrdenPago> buscarPorSuscripcionId(Long suscripcionId) {
        return repository.findAll().stream()
                .filter(o -> o.getSuscripcionId().equals(suscripcionId))
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private OrdenPagoEntity toEntity(OrdenPago domain) {
        OrdenPagoEntity entity = new OrdenPagoEntity();
        entity.setId(domain.getId());
        entity.setSuscripcionId(domain.getSuscripcionId());
        entity.setMonto(domain.getMonto().valor());
        entity.setMoneda(domain.getMonto().moneda());
        entity.setEstado(domain.getEstado());
        entity.setIdPagoExterno(domain.getIdPagoExterno());
        entity.setMontoReembolsado(domain.getMontoReembolsado().valor());
        entity.setMotivoReembolso(domain.getMotivoReembolso());
        entity.setFechaCreacion(domain.getFechaCreacion());
        return entity;
    }

    private OrdenPago toDomain(OrdenPagoEntity entity) {
        OrdenPago domain = new OrdenPago(
                entity.getId(),
                entity.getSuscripcionId(),
                new Monto(entity.getMonto(), entity.getMoneda()),
                entity.getIdPagoExterno()
        );
        domain.actualizarEstado(entity.getEstado().name());
        return domain;
    }
}