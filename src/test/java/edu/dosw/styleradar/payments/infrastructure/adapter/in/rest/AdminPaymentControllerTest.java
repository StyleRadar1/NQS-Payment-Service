package edu.dosw.styleradar.payments.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.payments.application.dto.response.AmountResponse;
import edu.dosw.styleradar.payments.application.dto.response.PaymentResponse;
import edu.dosw.styleradar.payments.application.port.in.AdminPaymentPort;
import edu.dosw.styleradar.payments.domain.model.PaymentStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdminPaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminPaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AdminPaymentPort adminPaymentPort;

    private PaymentResponse payment() {
        return new PaymentResponse(1L, 100L, new AmountResponse(new BigDecimal("14900.00"), "COP"),
                PaymentStatus.APPROVED, "MP-1", LocalDateTime.of(2026, 11, 11, 10, 0));
    }

    @Test
    void listaAllLosPayments() throws Exception {
        when(adminPaymentPort.list(isNull())).thenReturn(List.of(payment()));

        mockMvc.perform(get("/api/v1/admin/payments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].subscriptionId").value(100));

        verify(adminPaymentPort).list(null);
    }

    @Test
    void filtraPorStatus() throws Exception {
        when(adminPaymentPort.list(PaymentStatus.REJECTED)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/admin/payments").param("status", "REJECTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void rechazaStatusInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/admin/payments").param("status", "OTRO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }
}
