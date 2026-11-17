package edu.dosw.styleradar.pagos.application.dto.response;

import edu.dosw.styleradar.pagos.domain.model.EstadoPago;
import edu.dosw.styleradar.pagos.domain.model.OrdenPago;

import java.time.LocalDateTime;

/**
 * Pago del historial propio (SR-RF-97) o del listado administrativo (SR-RF-75).
 *
 * <p>SR-RF-98: {@code montoReembolsado}, {@code saldoReembolsable} y
 * {@code reembolsoParcial} permiten distinguir un pago aprobado con devolución
 * parcial de uno que conserva la totalidad del valor.</p>
 */
public record PagoResponse(
        Long id,
        Long suscripcionId,
        MontoResponse monto,
        EstadoPago estado,
        String idPagoExterno,
        MontoResponse montoReembolsado,
        String motivoReembolso,
        LocalDateTime fechaCreacion,
        boolean reembolsoParcial,
        MontoResponse saldoReembolsable) {

    public static PagoResponse desde(OrdenPago orden) {
        return new PagoResponse(
                orden.getId(),
                orden.getSuscripcionId(),
                MontoResponse.desde(orden.getMonto()),
                orden.getEstado(),
                orden.getIdPagoExterno(),
                MontoResponse.desde(orden.getMontoReembolsado()),
                orden.getMotivoReembolso(),
                orden.getFechaCreacion(),
                orden.esReembolsoParcial(),
                MontoResponse.desde(orden.saldoReembolsable()));
    }
}
