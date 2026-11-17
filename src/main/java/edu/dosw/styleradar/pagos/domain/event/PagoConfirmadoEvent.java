package edu.dosw.styleradar.pagos.domain.event;

import edu.dosw.styleradar.pagos.domain.model.TipoSuscriptor;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento de integración publicado cuando Mercado Pago confirma un pago aprobado.
 *
 * <p>SR-RF-94: solo un pago APROBADO activa o extiende la suscripción.
 * SR-RF-102: los consumidores aplican los beneficios del plan con estos datos.
 * Convención del broker: routing key {@code styleradar.pagos.pago.confirmado}
 * en {@code styleradar.exchange}.</p>
 */
public record PagoConfirmadoEvent(
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
            Long ordenId,
            Long suscripcionId,
            String suscriptorId,
            TipoSuscriptor tipoSuscriptor,
            Long planId,
            String nit,
            java.util.UUID almacenId,
            String estado) {
    }

    public static PagoConfirmadoEvent crear(Data data, String correlationId) {
        return new PagoConfirmadoEvent(
                UUID.randomUUID(),
                TYPE,
                SCHEMA_VERSION,
                Instant.now(),
                PRODUCER_NAME,
                correlationId == null || correlationId.isBlank() ? UUID.randomUUID().toString() : correlationId,
                data);
    }
}
