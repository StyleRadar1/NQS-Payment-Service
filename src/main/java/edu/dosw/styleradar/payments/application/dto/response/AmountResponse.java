package edu.dosw.styleradar.payments.application.dto.response;

import edu.dosw.styleradar.payments.domain.model.Amount;

import java.math.BigDecimal;

public record AmountResponse(BigDecimal value, String currency) {

    public static AmountResponse from(Amount amount) {
        return new AmountResponse(amount.value(), amount.currency());
    }
}
