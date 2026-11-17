package edu.dosw.styleradar.pagos.application.service;

import edu.dosw.styleradar.pagos.application.port.out.SuscripcionRepositoryPort;
import edu.dosw.styleradar.pagos.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos.domain.model.Suscripcion;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Punto único de activación y renovación.
 *
 * <p>SR-RF-94 y SR-RF-95: tanto el webhook como la conciliación diaria deben
 * aplicar exactamente la misma regla de vigencia. Centralizarla evita
 * activaciones duplicadas o periodos inconsistentes (SR-RNF-14).</p>
 */
@Service
public class SuscripcionActivacionService {

    private final SuscripcionRepositoryPort suscripcionRepository;

    public SuscripcionActivacionService(SuscripcionRepositoryPort suscripcionRepository) {
        this.suscripcionRepository = Objects.requireNonNull(suscripcionRepository);
    }

    public Suscripcion activar(Suscripcion suscripcion, OrdenPago orden, LocalDate fechaPago) {
        Objects.requireNonNull(suscripcion, "La suscripción es obligatoria.");
        Objects.requireNonNull(orden, "La orden es obligatoria.");
        if (!orden.estaAprobada()) {
            throw new IllegalStateException("Solo un pago aprobado activa o extiende una suscripción.");
        }
        if (suscripcion.getId() != null && !suscripcion.getId().equals(orden.getSuscripcionId())) {
            throw new IllegalStateException("La orden no pertenece a la suscripción.");
        }
        suscripcion.activar(fechaPago);
        return suscripcionRepository.guardar(suscripcion);
    }
}
