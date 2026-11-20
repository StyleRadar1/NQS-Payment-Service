package edu.dosw.styleradar.payments.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.payments.TestFixtures;
import edu.dosw.styleradar.payments.application.dto.response.AmountResponse;
import edu.dosw.styleradar.payments.application.dto.response.PaymentResponse;
import edu.dosw.styleradar.payments.application.dto.response.RetryPaymentResponse;
import edu.dosw.styleradar.payments.application.port.in.PaymentPort;
import edu.dosw.styleradar.payments.domain.model.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private PaymentPort paymentPort;

    private PaymentResponse payment() {
        return new PaymentResponse(1L, 100L, new AmountResponse(new BigDecimal("14900.00"), "COP"),
                PaymentStatus.APPROVED, "MP-1", TestFixtures.AHORA);
    }

    @Test
    void consultaHistory() throws Exception {
        when(paymentPort.history("USER-1")).thenReturn(List.of(payment()));

        mockMvc.perform(get("/api/v1/payments/history").header("X-User-Id", "USER-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("APPROVED"))
                .andExpect(jsonPath("$[0].amount.value").value(14900.00));
    }

    @Test
    void historyVacioDevuelve200() throws Exception {
        when(paymentPort.history("USER-1")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/payments/history").header("X-User-Id", "USER-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void reintentaPayment() throws Exception {
        when(paymentPort.retry(eq(10L), anyString(), eq("USER-1")))
                .thenReturn(new RetryPaymentResponse(11L, PaymentStatus.PENDING,
                        "https://mp/new", "Nuevo intento"));

        mockMvc.perform(post("/api/v1/payments/10/retry")
                        .header("X-User-Id", "USER-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"payerEmail":"a@b.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderId").value(11))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }
}
