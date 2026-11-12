package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.Suscripcion;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.SuscripcionRepositoryPort;
import edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.entity.SuscripcionEntity;
import edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.repository.SuscripcionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    @Override
    public List<Suscripcion> buscarVencidas(LocalDate fecha) {
        return repository.findVencidas(fecha).stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<Suscripcion> buscarPorPreapprovalId(String preapprovalId) {
        return repository.findByPreapprovalIdMp(preapprovalId).map(this::toDomain);
    }

    private SuscripcionEntity toEntity(Suscripcion d) {
        SuscripcionEntity e = new SuscripcionEntity();
        e.setId(d.getId());
        e.setSuscriptorId(d.getSuscriptorId());
        e.setTipoSuscriptor(d.getTipoSuscriptor());
        e.setPlanId(d.getPlanId());
        e.setPlanProgramadoId(d.getPlanProgramadoId());
        e.setEstado(d.getEstado());
        e.setFechaInicio(d.getFechaInicio());
        e.setFechaFin(d.getFechaFin());
        e.setAutoRenovacion(d.isAutoRenovacion());
        e.setPreapprovalIdMp(d.getPreapprovalIdMp());
        return e;
    }

    private Suscripcion toDomain(SuscripcionEntity e) {
        Suscripcion d = new Suscripcion(e.getId(), e.getSuscriptorId(),
                                         e.getTipoSuscriptor(), e.getPlanId());
        d.rehidratar(
            e.getEstado(),
            e.getFechaInicio(),
            e.getFechaFin(),
            e.isAutoRenovacion(),
            e.getPreapprovalIdMp(),
            e.getPlanProgramadoId()
        );
        return d;
    }
}