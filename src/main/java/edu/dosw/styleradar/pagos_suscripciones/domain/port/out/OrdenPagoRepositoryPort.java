package edu.dosw.styleradar.pagos_suscripciones.domain.port.out;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.OrdenPago;

public interface OrdenPagoRepositoryPort {
    OrdenPago guardar(OrdenPago orden);
    java.util.Optional<OrdenPago> buscarPorId(Long id);
    java.util.Optional<OrdenPago> buscarPorIdPagoExterno(String idPagoExterno);
    java.util.List<OrdenPago> buscarPorSuscripcionId(Long suscripcionId);
}