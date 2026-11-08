package edu.dosw.styleradar.pagos_suscripciones.domain.port.in;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.Suscripcion;

public interface SuscripcionPort {
    Suscripcion crearSuscripcion(String suscriptorId, String tipoSuscriptor, Long planId);
    void cancelarRenovacion(Long suscripcionId);
    void cambiarPlan(Long suscripcionId, Long nuevoPlanId);
    Suscripcion consultarSuscripcion(Long suscripcionId);
}