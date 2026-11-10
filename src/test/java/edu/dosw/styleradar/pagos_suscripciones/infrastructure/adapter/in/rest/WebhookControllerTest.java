package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.pagos_suscripciones.domain.port.in.WebhookPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WebhookController.class)
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WebhookPort webhookPort;

    @Test
    void debeRecibirWebhookDeMercadoPago() throws Exception {
        String payload = "{\"data.id\": \"MP-123\", \"action\": \"APPROVED\", \"type\": \"payment\"}";

        mockMvc.perform(post("/api/v1/webhooks/mercadopago")
                .header("x-signature", "dummy-signature")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk());

        verify(webhookPort).procesarNotificacionPago("MP-123", "APPROVED", "payment");
    }
}