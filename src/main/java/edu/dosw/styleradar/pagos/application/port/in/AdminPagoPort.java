package edu.dosw.styleradar.pagos.application.port.in;

import edu.dosw.styleradar.pagos.application.dto.response.PagoResponse;
import edu.dosw.styleradar.pagos.domain.model.EstadoPago;

import java.util.List;

/** SR-RF-75: consulta administrativa de todos los pagos. */
public interface AdminPagoPort {

    List<PagoResponse> listar(EstadoPago estado);
}
