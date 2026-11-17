package edu.dosw.styleradar.pagos.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.pagos.application.dto.response.MontoResponse;
import edu.dosw.styleradar.pagos.application.dto.response.PagoResponse;
import edu.dosw.styleradar.pagos.application.port.in.AdminPagoPort;
import edu.dosw.styleradar.pagos.domain.model.EstadoPago;
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

@WebMvcTest(controllers = AdminPagoController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AdminPagoController (SR-RF-75)")
class AdminPagoControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AdminPagoPort adminPagoPort;

    private PagoResponse pago() {
        return new PagoResponse(1L, 100L, new MontoResponse(new BigDecimal("14900.00"), "COP"),
                EstadoPago.APROBADO, "MP-1", new MontoResponse(BigDecimal.ZERO, "COP"),
                null, LocalDateTime.of(2026, 11, 11, 10, 0), false,
                new MontoResponse(new BigDecimal("14900.00"), "COP"));
    }

    @Test
    void listaTodosLosPagos() throws Exception {
        when(adminPagoPort.listar(isNull())).thenReturn(List.of(pago()));

        mockMvc.perform(get("/api/v1/admin/pagos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].suscripcionId").value(100));

        verify(adminPagoPort).listar(null);
    }

    @Test
    void filtraPorEstado() throws Exception {
        when(adminPagoPort.listar(EstadoPago.REEMBOLSADO)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/admin/pagos").param("estado", "REEMBOLSADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void rechazaEstadoInvalido() throws Exception {
        mockMvc.perform(get("/api/v1/admin/pagos").param("estado", "OTRO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }
}
