package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.Suscripcion;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.SuscripcionRepositoryPort;
import edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.entity.SuscripcionEntity;
import edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.repository.SuscripcionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class SuscripcionPersistenceAdapter implements SuscripcionRepositoryPort {

    private final SuscripcionJpaRepository repository;

    @Override
    public Suscripcion guardar(Suscripcion suscripcion) {
        SuscripcionEntity entity = toEntity(suscripcion);
        SuscripcionEntity saved = repository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Suscripcion> buscarPorId(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Suscripcion> buscarPorSuscriptorId(String suscriptorId) {
        return repository.findBySuscriptorId(suscriptorId).map(this::toDomain);
    }

    private SuscripcionEntity toEntity(Suscripcion domain) {
        SuscripcionEntity entity = new SuscripcionEntity();
        entity.setId(domain.getId());
        entity.setSuscriptorId(domain.getSuscriptorId());
        entity.setTipoSuscriptor(domain.getTipoSuscriptor());
        entity.setPlanId(domain.getPlanId());
        entity.setPlanProgramadoId(domain.getPlanProgramadoId());
        entity.setEstado(domain.getEstado());
        entity.setFechaInicio(domain.getFechaInicio());
        entity.setFechaFin(domain.getFechaFin());
        entity.setAutoRenovacion(domain.isAutoRenovacion());
        entity.setPreapprovalIdMp(domain.getPreapprovalIdMp());
        return entity;
    }

    private Suscripcion toDomain(SuscripcionEntity entity) {
        Suscripcion domain = new Suscripcion(
                entity.getId(),
                entity.getSuscriptorId(),
                entity.getTipoSuscriptor(),
                entity.getPlanId()
        );
        if (entity.getFechaFin() != null) {
            domain.activar(entity.getFechaFin());
        } else {
            domain.setEstado(entity.getEstado());
        }
        if (!entity.isAutoRenovacion()) {
            domain.cancelarRenovacion();
        }
        domain.setPreapprovalIdMp(entity.getPreapprovalIdMp());
        return domain;
    }
    @Override
    public List<Suscripcion> buscarVencidas(LocalDate fecha) {
        return repository.findVencidas(fecha).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }
}