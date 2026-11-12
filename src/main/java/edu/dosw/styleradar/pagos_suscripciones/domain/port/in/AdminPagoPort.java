package edu.dosw.styleradar.pagos_suscripciones.domain.port.in;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.EstadoPago;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.OrdenPago;
import java.util.List;

public interface AdminPagoPort {
    List<OrdenPago> consultarPagos(EstadoPago estadoFiltro);
}