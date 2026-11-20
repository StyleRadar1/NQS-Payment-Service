package edu.dosw.styleradar.payments.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.payments.application.dto.request.NotificationPayment;
import edu.dosw.styleradar.payments.application.port.in.WebhookPort;
import edu.dosw.styleradar.payments.infrastructure.adapter.in.rest.security.MercadoPagoSignatureValidator;
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
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private WebhookPort webhookPort;
    @MockBean
    private MercadoPagoSignatureValidator signatureValidator;

    @Test
    void procesaNotificationValida() throws Exception {
        when(signatureValidator.validate("dummy", "req-1", "12345")).thenReturn(true);

        mockMvc.perform(post("/api/v1/webhooks/mercadopago")
                        .header("x-signature", "dummy")
                        .header("x-request-id", "req-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"payment","action":"payment.created","data":{"id":"12345"}}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));

        ArgumentCaptor<NotificationPayment> notification =
                ArgumentCaptor.forClass(NotificationPayment.class);
        verify(webhookPort).process(notification.capture(), eq("req-1"));
        assertThat(notification.getValue().paymentId()).isEqualTo("12345");
        assertThat(notification.getValue().type()).isEqualTo("payment");
        assertThat(notification.getValue().payload()).containsEntry("type", "payment");
    }

    @Test
    void rechazaFirmaInvalida() throws Exception {
        when(signatureValidator.validate(any(), any(), any())).thenReturn(false);

        mockMvc.perform(post("/api/v1/webhooks/mercadopago")
                        .header("x-signature", "firma")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"payment","data":{"id":"12345"}}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.detail").value("La firma del webhook es inválida."));
    }

    @Test
    void aceptaParametrosDeConsultaSinCuerpo() throws Exception {
        when(signatureValidator.validate("dummy", "req-2", "999")).thenReturn(true);

        mockMvc.perform(post("/api/v1/webhooks/mercadopago")
                        .header("x-signature", "dummy")
                        .header("x-request-id", "req-2")
                        .param("type", "payment")
                        .param("data.id", "999"))
                .andExpect(status().isOk());

        ArgumentCaptor<NotificationPayment> notification =
                ArgumentCaptor.forClass(NotificationPayment.class);
        verify(webhookPort).process(notification.capture(), eq("req-2"));
        assertThat(notification.getValue().paymentId()).isEqualTo("999");
    }

    @Test
    void usaTopicComoAlternativaAType() throws Exception {
        when(signatureValidator.validate("dummy", "req-3", "777")).thenReturn(true);

        mockMvc.perform(post("/api/v1/webhooks/mercadopago")
                        .header("x-signature", "dummy")
                        .header("x-request-id", "req-3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"topic":"payment","data":{"id":"777"}}
                                """))
                .andExpect(status().isOk());

        ArgumentCaptor<NotificationPayment> notification =
                ArgumentCaptor.forClass(NotificationPayment.class);
        verify(webhookPort).process(notification.capture(), eq("req-3"));
        assertThat(notification.getValue().type()).isEqualTo("payment");
    }
}


