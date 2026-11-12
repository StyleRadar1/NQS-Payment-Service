package edu.dosw.styleradar.pagos_suscripciones.domain.port.out;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.EstadoPago;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.OrdenPago;
import java.util.List;
import java.util.Optional;

public interface OrdenPagoRepositoryPort {
    OrdenPago guardar(OrdenPago orden);
    Optional<OrdenPago> buscarPorId(Long id);
    Optional<OrdenPago> buscarPorIdPagoExterno(String idPagoExterno);
    boolean existePorIdPagoExterno(String idPagoExterno);
    List<OrdenPago> buscarPorSuscripcionId(Long suscripcionId);
    List<OrdenPago> buscarPorSuscriptorId(String suscriptorId);
    List<OrdenPago> buscarTodos();
    List<OrdenPago> buscarPorEstado(EstadoPago estado);
    List<OrdenPago> buscarPendientesAntesDe(java.time.LocalDateTime fechaLimite);
}