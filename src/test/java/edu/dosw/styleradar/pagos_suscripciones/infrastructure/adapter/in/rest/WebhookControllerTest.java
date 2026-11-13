package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.pagos_suscripciones.domain.port.in.WebhookPort;
import edu.dosw.styleradar.pagos_suscripciones.infrastructure.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = WebhookController.class)
@ActiveProfiles("test")
@Import(GlobalExceptionHandler.class)
class WebhookControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private WebhookController webhookController;
    @MockBean  private WebhookPort webhookPort;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(webhookController, "webhookSecret", "secret-key-12345");
    }

    @Test
    void debeRecibirWebhookDeMercadoPago() throws Exception {
        String payload = "{\"data\": {\"id\": \"123456\"}, \"action\": \"APPROVED\", \"type\": \"payment\"}";
        String ts = "123";
        String requestId = "dummy";
        String hash = calcularFirma("123456", requestId, ts, "secret-key-12345");
        String firmaValida = "ts=" + ts + ",v1=" + hash;

        mockMvc.perform(post("/api/v1/webhooks/mercadopago")
                        .header("x-signature", firmaValida)
                        .header("x-request-id", requestId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        verify(webhookPort).procesarNotificacionPago(
                eq("123456"), eq("APPROVED"), eq("payment"), anyString());
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

    private String calcularFirma(String id, String requestId, String ts, String secret) throws Exception {
        String manifest = "id:" + id + ";request-id:" + requestId + ";ts:" + ts + ";";
        Mac sha256HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256HMAC.init(secretKey);
        return Base64.getEncoder().encodeToString(sha256HMAC.doFinal(manifest.getBytes(StandardCharsets.UTF_8)));
    }
}