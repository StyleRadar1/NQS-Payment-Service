package edu.dosw.styleradar.pagos.application.dto.response;

import edu.dosw.styleradar.pagos.domain.model.Monto;

import java.math.BigDecimal;

/** Representación de dinero usada en las respuestas HTTP. */
public record MontoResponse(BigDecimal valor, String moneda) {

    public static MontoResponse desde(Monto monto) {
        return new MontoResponse(monto.valor(), monto.moneda());
    }
}
