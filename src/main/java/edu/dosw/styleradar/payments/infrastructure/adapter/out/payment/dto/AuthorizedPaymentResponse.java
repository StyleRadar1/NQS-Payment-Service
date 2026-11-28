package edu.dosw.styleradar.payments.infrastructure.adapter.out.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AuthorizedPaymentResponse(
        @JsonProperty("id") Long id,
        @JsonProperty("status") String status,
        @JsonProperty("external_reference") String externalReference,
        @JsonProperty("preapproval_id") String preapprovalId,
        @JsonProperty("transaction_amount") BigDecimal transactionAmount,
        @JsonProperty("currency_id") String currencyId,
        @JsonProperty("payment") Payment payment) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Payment(
            @JsonProperty("id") Long id,
            @JsonProperty("status") String status) {
    }
}
