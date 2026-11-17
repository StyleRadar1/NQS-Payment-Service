package edu.dosw.styleradar.pagos.infrastructure.adapter.out.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/** Cuerpo para cancelar una preapproval. */
public record CancelPreapprovalRequest(@JsonProperty("status") String status) {
}
