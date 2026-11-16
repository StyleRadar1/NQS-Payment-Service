package edu.dosw.styleradar.pagos.application.dto.response;

import edu.dosw.styleradar.pagos.domain.model.EstadoSuscripcion;
import edu.dosw.styleradar.pagos.domain.model.Suscripcion;
import edu.dosw.styleradar.pagos.domain.model.TipoSuscriptor;

import java.time.LocalDate;

/**
 * Estado y vigencia de una suscripción.
 *
 * <p>SR-RF-67: incluye el acceso al almacén. SR-RF-68/101: incluye la
 * renovación y la fecha de fin pagada. SR-RF-102/103: incluye el plan
 * vigente y sus límites.</p>
 */
public record SuscripcionResponse(
        Long id,
        String suscriptorId,
        TipoSuscriptor tipoSuscriptor,
        PlanResponse plan,
        EstadoSuscripcion estado,
        LocalDate fechaInicio,
        LocalDate fechaFin,
        boolean autoRenovacion,
        boolean acceso,
        String nit,
        java.util.UUID almacenId,
        String mensaje) {

    public static SuscripcionResponse desde(Suscripcion suscripcion,
                                            PlanResponse plan,
                                            boolean acceso,
                                            String mensaje) {
        return new SuscripcionResponse(
                suscripcion.getId(),
                suscripcion.getSuscriptorId(),
                suscripcion.getTipoSuscriptor(),
                plan,
                suscripcion.getEstado(),
                suscripcion.getFechaInicio(),
                suscripcion.getFechaFin(),
                suscripcion.isAutoRenovacion(),
                acceso,
                suscripcion.getNit(),
                suscripcion.getAlmacenId(),
                mensaje);
    }
}
