package edu.dosw.styleradar.pagos_suscripciones.domain.port.in;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.Suscripcion;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.TipoSuscriptor;

public interface SuscripcionPort {
    /**
     * Inicia el proceso de suscripción, creando una orden de pago y devolviendo el link de pago.
     * @return El init_point de Mercado Pago.
     */
    String iniciarSuscripcion(String idSuscriptor, Long idPlan, TipoSuscriptor tipoSuscriptor, String emailPayer);

    /**
     * Cancela la renovación automática de una suscripción.
     */
    void cancelarRenovacion(String idSuscriptor);

    /**
     * Consulta una suscripción por el ID del suscriptor.
     */
    Suscripcion consultarSuscripcion(String idSuscriptor);
    
    /**
     * @return init_point si es upgrade (cobro de diferencia); null si es downgrade.
     */
    String programarCambioPlan(String idSuscriptor, Long nuevoPlanId, String emailPayer);
}