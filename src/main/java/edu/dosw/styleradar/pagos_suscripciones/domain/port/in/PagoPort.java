package edu.dosw.styleradar.pagos_suscripciones.domain.port.in;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.OrdenPago;
import java.util.List;

public interface PagoPort {
    void reembolsarPago(Long ordenPagoId, String monto, String motivo);
    List<OrdenPago> consultarHistorialPagos(String suscriptorId);
}