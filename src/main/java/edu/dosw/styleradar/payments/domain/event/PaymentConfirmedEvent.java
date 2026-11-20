package edu.dosw.styleradar.payments.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record PaymentConfirmedEvent(
        UUID eventId,
        String eventType,
        int version,
        Instant timestamp,
        String producer,
        String correlationId,
        Data data) {

    public static final String TYPE = "styleradar.pagos.pago.confirmado";
    public static final String PRODUCER_NAME = "ms-pagos-suscripciones";
    public static final int SCHEMA_VERSION = 1;

    public record Data(
            @JsonProperty("ordenId") Long orderId,
            @JsonProperty("suscripcionId") Long subscriptionId,
            @JsonProperty("suscriptorId") String subscriberId,
            @JsonProperty("usuarioId") Long userId,
            @JsonProperty("tipoSuscriptor") String subscriberType,
            @JsonProperty("planId") Long planId,
            @JsonProperty("nit") String nit,
            @JsonProperty("almacenId") java.util.UUID storeId,
            @JsonProperty("estado") String status) {
    }

    public static Long userIdFrom(String subscriberId) {
        if (subscriberId == null || subscriberId.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(subscriberId.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public static PaymentConfirmedEvent create(Data data, String correlationId) {
        return new PaymentConfirmedEvent(
                UUID.randomUUID(),
                TYPE,
                SCHEMA_VERSION,
                Instant.now(),
                PRODUCER_NAME,
                correlationId == null || correlationId.isBlank() ? UUID.randomUUID().toString() : correlationId,
                data);
    }
}
