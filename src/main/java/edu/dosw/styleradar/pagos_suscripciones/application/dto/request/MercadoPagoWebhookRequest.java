package edu.dosw.styleradar.pagos_suscripciones.application.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Payload enviado por Mercado Pago cuando ocurre un evento")
public class MercadoPagoWebhookRequest {

    @Schema(description = "Datos del evento", example = "{\"id\": \"462bbfe0-a0db-4676-941e-9f42090fc511\"}")
    private DataDto data;

    @Schema(description = "Acción realizada", example = "APPROVED")
    private String action;

    @Schema(description = "Tipo de evento", example = "payment")
    private String type;

    @Data
    @Schema(description = "Objeto interno con el ID del pago")
    public static class DataDto {
        @Schema(description = "ID externo del pago en Mercado Pago", example = "462bbfe0-a0db-4676-941e-9f42090fc511")
        private String id;
    }
}