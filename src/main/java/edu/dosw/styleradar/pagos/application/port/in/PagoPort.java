package edu.dosw.styleradar.pagos.application.port.in;

import edu.dosw.styleradar.pagos.application.dto.response.PagoResponse;
import edu.dosw.styleradar.pagos.application.dto.response.ReembolsoResponse;
import edu.dosw.styleradar.pagos.application.dto.response.ReintentoResponse;

import java.math.BigDecimal;
import java.util.List;

/** Puerto de entrada para las operaciones de pagos del actor. */
public interface PagoPort {

    List<PagoResponse> historial(String suscriptorId);

    ReintentoResponse reintentar(Long ordenId, String emailPayer, String solicitanteId);

    ReembolsoResponse reembolsar(Long ordenId, BigDecimal monto, String motivo, String rol);
}
