package edu.dosw.styleradar.pagos.infrastructure.adapter.out.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/** Respuesta de Mercado Pago para reembolsos. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record RefundResponse(
        @JsonProperty("id") Long id,
        @JsonProperty("payment_id") Long paymentId,
        @JsonProperty("amount") BigDecimal amount,
        @JsonProperty("status") String status) {
}
