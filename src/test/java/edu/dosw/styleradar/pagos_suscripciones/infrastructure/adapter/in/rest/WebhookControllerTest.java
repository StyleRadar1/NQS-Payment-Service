package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.pagos_suscripciones.domain.port.in.WebhookPort;
import edu.dosw.styleradar.pagos_suscripciones.infrastructure.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = WebhookController.class)
@ActiveProfiles("test")
@Import(GlobalExceptionHandler.class)
class WebhookControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean  private WebhookPort webhookPort;

    @Test
    void debeRecibirWebhookDeMercadoPago() throws Exception {
        String payload = "{\"data\": {\"id\": \"MP-123\"}, \"action\": \"APPROVED\", \"type\": \"payment\"}";

        mockMvc.perform(post("/api/v1/webhooks/mercadopago")
                        .header("x-signature", "dummy")
                        .header("x-request-id", "dummy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        // Opción 1: port recibe DTO → verify con any(MercadoPagoWebhookRequest.class)
        // Opción 2: port recibe String → verify con anyString()
        verify(webhookPort).procesarNotificacionPago(
                eq("MP-123"), eq("APPROVED"), eq("payment"), anyString());
    }

    @Test
    void debeRechazarWebhookConFirmaInvalida() throws Exception {
        String payload = "{\"data\": {\"id\": \"MP-123\"}, \"action\": \"APPROVED\", \"type\": \"payment\"}";
        String firmaFalsa = "ts=123,v1=hash_invalido";

        mockMvc.perform(post("/api/v1/webhooks/mercadopago")
                        .header("x-signature", firmaFalsa)
                        .header("x-request-id", "request-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(webhookPort);
    }
}