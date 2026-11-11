package edu.dosw.styleradar.pagos_suscripciones.application.dto.request;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MercadoPagoWebhookRequestTest {

    @Test
    void debeMapearPropiedadesDelPayload() {
        MercadoPagoWebhookRequest request = new MercadoPagoWebhookRequest();
        MercadoPagoWebhookRequest.DataDto data = new MercadoPagoWebhookRequest.DataDto();
        data.setId("MP-123");
        
        request.setData(data);
        request.setAction("APPROVED");
        request.setType("payment");

        assertEquals("MP-123", request.getData().getId());
        assertEquals("APPROVED", request.getAction());
        assertEquals("payment", request.getType());
    }
}