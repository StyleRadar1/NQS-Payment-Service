package edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.pagos.application.port.out.SuscripcionRepositoryPort;
import edu.dosw.styleradar.pagos.domain.model.EstadoSuscripcion;
import edu.dosw.styleradar.pagos.domain.model.Suscripcion;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence.repository.SuscripcionJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Adaptador JPA de suscripciones. */
@Component
@RequiredArgsConstructor
public class SuscripcionPersistenceAdapter implements SuscripcionRepositoryPort {

    private static final List<EstadoSuscripcion> ESTADOS_VIGENTES =
            List.of(EstadoSuscripcion.ACTIVA, EstadoSuscripcion.PENDIENTE);

    private final SuscripcionJpaRepository suscripcionJpaRepository;

    @Override
    public Suscripcion guardar(Suscripcion suscripcion) {
        return PersistenceMapper.toDomain(
                suscripcionJpaRepository.save(PersistenceMapper.toEntity(suscripcion)));
    }

    @Override
    public Optional<Suscripcion> buscarPorId(Long id) {
        return suscripcionJpaRepository.findById(id).map(PersistenceMapper::toDomain);
    }

    @Override
    public Optional<Suscripcion> buscarUltimaPorSuscriptor(String suscriptorId) {
        return suscripcionJpaRepository.findFirstBySuscriptorIdOrderByIdDesc(suscriptorId)
                .map(PersistenceMapper::toDomain);
    }

    @Override
    public Optional<Suscripcion> buscarActivaOPendiente(String suscriptorId) {
        return suscripcionJpaRepository
                .findFirstBySuscriptorIdAndEstadoInOrderByIdDesc(suscriptorId, ESTADOS_VIGENTES)
                .map(PersistenceMapper::toDomain);
    }

    @Override
    public Optional<Suscripcion> buscarPorPreapprovalId(String preapprovalId) {
        return suscripcionJpaRepository.findFirstByPreapprovalIdMp(preapprovalId)
                .map(PersistenceMapper::toDomain);
    }

    @Override
    public List<Suscripcion> buscarActivasVencidas(LocalDate hoy) {
        return suscripcionJpaRepository
                .findByEstadoAndFechaFinBefore(EstadoSuscripcion.ACTIVA, hoy).stream()
                .map(PersistenceMapper::toDomain)
                .toList();
    }
}
