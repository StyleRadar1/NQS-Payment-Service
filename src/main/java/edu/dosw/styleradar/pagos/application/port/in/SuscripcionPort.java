package edu.dosw.styleradar.pagos.application.port.in;

import edu.dosw.styleradar.pagos.application.dto.request.IniciarSuscripcionRequest;
import edu.dosw.styleradar.pagos.application.dto.response.CancelacionResponse;
import edu.dosw.styleradar.pagos.application.dto.response.IniciarSuscripcionResponse;
import edu.dosw.styleradar.pagos.application.dto.response.SuscripcionResponse;

/** Puerto de entrada para el ciclo de vida de la suscripción. */
public interface SuscripcionPort {

    IniciarSuscripcionResponse iniciar(String suscriptorId, IniciarSuscripcionRequest request);

    SuscripcionResponse consultar(String suscriptorId, String solicitanteId, String rol);

    CancelacionResponse cancelar(String suscriptorId,
                                 boolean confirmacion,
                                 String solicitanteId,
                                 String rol);
}
