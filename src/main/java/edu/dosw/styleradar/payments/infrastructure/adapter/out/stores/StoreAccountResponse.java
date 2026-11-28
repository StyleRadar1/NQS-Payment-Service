package edu.dosw.styleradar.payments.infrastructure.adapter.out.stores;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StoreAccountResponse(String nit) {
}
