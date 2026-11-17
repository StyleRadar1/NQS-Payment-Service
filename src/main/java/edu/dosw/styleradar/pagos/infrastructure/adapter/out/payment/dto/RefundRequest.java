package edu.dosw.styleradar.pagos.infrastructure.adapter.out.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/** Cuerpo del reembolso total o parcial en Mercado Pago. */
public record RefundRequest(@JsonProperty("amount") BigDecimal amount) {
}
