package edu.dosw.styleradar.pagos.domain.event;

import edu.dosw.styleradar.pagos.domain.model.TipoSuscriptor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Evento publicado cuando una suscripción vence sin pago aprobado.
 *
 * <p>SR-RF-99 y SR-RF-103: la suspensión es inmediata, sin periodo de gracia.
 * SR-RF-99 RN-02: los datos del almacén se conservan; este servicio solo
 * notifica para que catálogo, tiendas e inventario oculten el contenido.
 * Convención del broker: routing key
 * {@code styleradar.pagos.suscripcion.suspendida} en {@code styleradar.exchange}.</p>
 */
public record SuscripcionSuspendidaEvent(
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
            Long suscripcionId,
            String suscriptorId,
            TipoSuscriptor tipoSuscriptor,
            Long planId,
            String nit,
            java.util.UUID almacenId,
            LocalDate fechaFin,
            String motivo) {
    }

    public static SuscripcionSuspendidaEvent crear(Data data, String correlationId) {
        return new SuscripcionSuspendidaEvent(
                UUID.randomUUID(),
                TYPE,
                SCHEMA_VERSION,
                Instant.now(),
                PRODUCER_NAME,
                correlationId == null || correlationId.isBlank() ? UUID.randomUUID().toString() : correlationId,
                data);
    }
}
