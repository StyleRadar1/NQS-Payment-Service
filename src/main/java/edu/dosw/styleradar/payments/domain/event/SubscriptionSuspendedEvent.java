package edu.dosw.styleradar.payments.domain.event;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Integration event published when a subscription expires without approval.
 *
 * <p>The external RabbitMQ contract is fixed: type
 * {@code styleradar.pagos.suscripcion.suspendida}, producer
 * {@code ms-pagos-suscripciones} and Spanish payload field names.</p>
 */
public record SubscriptionSuspendedEvent(
        UUID eventId,
        String eventType,
        int version,
        Instant timestamp,
        String producer,
        String correlationId,
        Data data) {

    public static final String TYPE = "styleradar.pagos.suscripcion.suspendida";
    public static final String PRODUCER_NAME = "ms-pagos-suscripciones";
    public static final int SCHEMA_VERSION = 1;

    public record Data(
            @JsonProperty("suscripcionId") Long subscriptionId,
            @JsonProperty("suscriptorId") String subscriberId,
            @JsonProperty("tipoSuscriptor") String subscriberType,
            @JsonProperty("planId") Long planId,
            @JsonProperty("nit") String nit,
            @JsonProperty("almacenId") java.util.UUID storeId,
            @JsonProperty("fechaFin") LocalDate endDate,
            @JsonProperty("motivo") String reason) {
    }

    public static SubscriptionSuspendedEvent create(Data data, String correlationId) {
        return new SubscriptionSuspendedEvent(
                UUID.randomUUID(),
                TYPE,
                SCHEMA_VERSION,
                Instant.now(),
                PRODUCER_NAME,
                correlationId == null || correlationId.isBlank() ? UUID.randomUUID().toString() : correlationId,
                data);
    }
}
