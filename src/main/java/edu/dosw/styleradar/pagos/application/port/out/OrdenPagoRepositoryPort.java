package edu.dosw.styleradar.pagos.application.port.out;

import edu.dosw.styleradar.pagos.domain.model.EstadoPago;
import edu.dosw.styleradar.pagos.domain.model.OrdenPago;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** Puerto de salida para la persistencia de órdenes de pago. */
public interface OrdenPagoRepositoryPort {

    OrdenPago guardar(OrdenPago ordenPago);

    Optional<OrdenPago> buscarPorId(Long id);

    Optional<OrdenPago> buscarPorIdPagoExterno(String idPagoExterno);

    Optional<OrdenPago> buscarPendientePorSuscripcion(Long suscripcionId);

    List<OrdenPago> buscarPorSuscripcion(Long suscripcionId);

    /** SR-RF-97: historial propio, del más reciente al más antiguo. */
    List<OrdenPago> buscarPorSuscriptor(String suscriptorId);

    /** SR-RF-75: listado administrativo, con filtro opcional por estado. */
    List<OrdenPago> listar(EstadoPago estado);

    /** SR-RNF-14: candidatos de la conciliación diaria. */
    List<OrdenPago> buscarPendientesAntesDe(LocalDateTime limite);
}
