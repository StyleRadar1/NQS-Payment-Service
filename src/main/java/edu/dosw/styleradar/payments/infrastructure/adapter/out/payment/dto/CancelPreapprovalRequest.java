package edu.dosw.styleradar.payments.infrastructure.adapter.out.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CancelPreapprovalRequest(@JsonProperty("status") String status) {
}
