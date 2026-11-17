package edu.dosw.styleradar.pagos.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.pagos.application.dto.request.NotificacionPago;
import edu.dosw.styleradar.pagos.application.port.in.WebhookPort;
import edu.dosw.styleradar.pagos.infrastructure.adapter.in.rest.security.MercadoPagoSignatureValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = WebhookController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("WebhookController (SR-RF-94)")
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private WebhookPort webhookPort;
    @MockBean
    private MercadoPagoSignatureValidator signatureValidator;

    @Test
    void procesaNotificacionValida() throws Exception {
        when(signatureValidator.validar("dummy", "req-1", "12345")).thenReturn(true);

        mockMvc.perform(post("/api/v1/webhooks/mercadopago")
                        .header("x-signature", "dummy")
                        .header("x-request-id", "req-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"payment","action":"payment.created","data":{"id":"12345"}}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));

        ArgumentCaptor<NotificacionPago> notificacion =
                ArgumentCaptor.forClass(NotificacionPago.class);
        verify(webhookPort).procesar(notificacion.capture(), eq("req-1"));
        assertThat(notificacion.getValue().idPago()).isEqualTo("12345");
        assertThat(notificacion.getValue().tipo()).isEqualTo("payment");
        assertThat(notificacion.getValue().payload()).containsEntry("type", "payment");
    }

    @Test
    void rechazaFirmaInvalida() throws Exception {
        when(signatureValidator.validar(any(), any(), any())).thenReturn(false);

        mockMvc.perform(post("/api/v1/webhooks/mercadopago")
                        .header("x-signature", "firma")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"payment","data":{"id":"12345"}}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("La firma del webhook es inválida."));
    }

    @Test
    void aceptaParametrosDeConsultaSinCuerpo() throws Exception {
        when(signatureValidator.validar("dummy", "req-2", "999")).thenReturn(true);

        mockMvc.perform(post("/api/v1/webhooks/mercadopago")
                        .header("x-signature", "dummy")
                        .header("x-request-id", "req-2")
                        .param("type", "payment")
                        .param("data.id", "999"))
                .andExpect(status().isOk());

        ArgumentCaptor<NotificacionPago> notificacion =
                ArgumentCaptor.forClass(NotificacionPago.class);
        verify(webhookPort).procesar(notificacion.capture(), eq("req-2"));
        assertThat(notificacion.getValue().idPago()).isEqualTo("999");
    }

    @Test
    void usaTopicComoAlternativaAType() throws Exception {
        when(signatureValidator.validar("dummy", "req-3", "777")).thenReturn(true);

        mockMvc.perform(post("/api/v1/webhooks/mercadopago")
                        .header("x-signature", "dummy")
                        .header("x-request-id", "req-3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"topic":"payment","data":{"id":"777"}}
                                """))
                .andExpect(status().isOk());

        ArgumentCaptor<NotificacionPago> notificacion =
                ArgumentCaptor.forClass(NotificacionPago.class);
        verify(webhookPort).procesar(notificacion.capture(), eq("req-3"));
        assertThat(notificacion.getValue().tipo()).isEqualTo("payment");
    }
}
