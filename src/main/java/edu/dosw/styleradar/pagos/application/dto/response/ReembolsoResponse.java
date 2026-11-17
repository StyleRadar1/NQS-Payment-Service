package edu.dosw.styleradar.pagos.application.dto.response;

import edu.dosw.styleradar.pagos.domain.model.EstadoPago;

/**
 * SR-RF-98: resultado del reembolso, saldo restante y estado de la
 * renovación (RN-05: el reembolso cancela el siguiente cobro).
 */
public record ReembolsoResponse(
        Long ordenId,
        EstadoPago estado,
        MontoResponse montoReembolsado,
        MontoResponse saldoReembolsable,
        boolean reembolsoParcial,
        String motivoReembolso,
        boolean renovacionCancelada,
        String mensaje) {
}
