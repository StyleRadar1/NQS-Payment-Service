package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.*;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.OrdenPagoRepositoryPort;
import edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.entity.OrdenPagoEntity;
import edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.repository.OrdenPagoJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrdenPagoPersistenceAdapter implements OrdenPagoRepositoryPort {

    private final OrdenPagoJpaRepository repository;

    @Override
    public OrdenPago guardar(OrdenPago orden) {
        return toDomain(repository.save(toEntity(orden)));
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
        return repository.findBySuscripcionId(suscripcionId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<OrdenPago> buscarPorSuscriptorId(String suscriptorId) {
        return repository.findBySuscriptorId(suscriptorId).stream()
                .map(this::toDomain).toList();
    }

    @Override
    public List<OrdenPago> buscarTodos() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<OrdenPago> buscarPorEstado(EstadoPago estado) {
        return repository.findByEstado(estado).stream().map(this::toDomain).toList();
    }

    @Override
    public List<OrdenPago> buscarPendientesAntesDe(LocalDateTime fechaLimite) {
        return repository.findPendientesAntesDe(fechaLimite).stream().map(this::toDomain).toList();
    }

    private OrdenPagoEntity toEntity(OrdenPago d) {
        OrdenPagoEntity e = new OrdenPagoEntity();
        e.setId(d.getId());
        e.setSuscripcionId(d.getSuscripcionId());
        e.setMonto(d.getMonto().valor());
        e.setMoneda(d.getMonto().moneda());
        e.setEstado(d.getEstado());
        e.setIdPagoExterno(d.getIdPagoExterno());
        e.setMontoReembolsado(d.getMontoReembolsado().valor());
        e.setMotivoReembolso(d.getMotivoReembolso());
        e.setFechaCreacion(d.getFechaCreacion());
        return e;
    }

    private OrdenPago toDomain(OrdenPagoEntity e) {
        Monto monto = new Monto(e.getMonto(), e.getMoneda());
        OrdenPago d = new OrdenPago(e.getId(), e.getSuscripcionId(),
                                     monto, e.getIdPagoExterno());
        BigDecimal reemb = e.getMontoReembolsado() != null ? e.getMontoReembolsado() : BigDecimal.ZERO;
        d.rehidratar(e.getEstado(), new Monto(reemb, e.getMoneda()), e.getMotivoReembolso());
        return d;
    }
}