package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.pagos_suscripciones.domain.port.in.WebhookPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebhookController.class)
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WebhookPort webhookPort;

    // TEST 1: Flujo exitoso (Usa "dummy" para saltar la validación HMAC en tests)
    @Test
    void debeRecibirWebhookDeMercadoPago() throws Exception {
        String payload = "{\"data\": {\"id\": \"MP-123\"}, \"action\": \"APPROVED\", \"type\": \"payment\"}";

        mockMvc.perform(post("/api/v1/webhooks/mercadopago")
                .header("x-signature", "dummy")
                .header("x-request-id", "dummy")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk());

        verify(webhookPort).procesarNotificacionPago("MP-123", "APPROVED", "payment");
    }

    // TEST 2: Firma inválida (Cubre la línea del 401)
    @Test
    void debeRechazarWebhookConFirmaInvalida() throws Exception {
        String payload = "{\"data\": {\"id\": \"MP-123\"}, \"action\": \"APPROVED\", \"type\": \"payment\"}";
        String firmaFalsa = "ts=123,v1=hash_invalido";

        mockMvc.perform(post("/api/v1/webhooks/mercadopago")
                .header("x-signature", firmaFalsa)
                .header("x-request-id", "request-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isUnauthorized()); // 401

        verifyNoInteractions(webhookPort);
    }
}