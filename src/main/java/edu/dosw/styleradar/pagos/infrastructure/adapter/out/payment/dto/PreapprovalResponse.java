package edu.dosw.styleradar.pagos.infrastructure.adapter.out.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/** Respuesta de Mercado Pago para preapproval. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PreapprovalResponse(
        @JsonProperty("id") String id,
        @JsonProperty("init_point") String initPoint,
        @JsonProperty("sandbox_init_point") String sandboxInitPoint,
        @JsonProperty("status") String status,
        @JsonProperty("external_reference") String externalReference) {
}
