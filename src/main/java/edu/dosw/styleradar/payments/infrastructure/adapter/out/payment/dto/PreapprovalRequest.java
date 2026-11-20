package edu.dosw.styleradar.payments.infrastructure.adapter.out.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record PreapprovalRequest(
        @JsonProperty("reason") String reason,
        @JsonProperty("external_reference") String externalReference,
        @JsonProperty("payer_email") String payerEmail,
        @JsonProperty("back_url") String backUrl,
        @JsonProperty("auto_recurring") AutoRecurring autoRecurring) {

    public record AutoRecurring(
            @JsonProperty("transaction_amount") BigDecimal transactionAmount,
            @JsonProperty("currency_id") String currencyId,
            @JsonProperty("frequency") int frequency,
            @JsonProperty("frequency_type") String frequencyType) {
    }
}
