package edu.dosw.styleradar.pagos_suscripciones.domain.port.out;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.Suscripcion;
import java.util.Optional;

public interface SuscripcionRepositoryPort {
    Suscripcion guardar(Suscripcion suscripcion);
    Optional<Suscripcion> buscarPorId(Long id);
}