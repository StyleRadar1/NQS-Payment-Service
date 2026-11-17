package edu.dosw.styleradar.pagos.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.pagos.TestFixtures;
import edu.dosw.styleradar.pagos.application.dto.response.MontoResponse;
import edu.dosw.styleradar.pagos.application.dto.response.PagoResponse;
import edu.dosw.styleradar.pagos.application.dto.response.ReembolsoResponse;
import edu.dosw.styleradar.pagos.application.dto.response.ReintentoResponse;
import edu.dosw.styleradar.pagos.application.port.in.PagoPort;
import edu.dosw.styleradar.pagos.domain.model.EstadoPago;
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

@WebMvcTest(controllers = PagoController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("PagoController")
class PagoControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private PagoPort pagoPort;

    private PagoResponse pago() {
        return new PagoResponse(1L, 100L, new MontoResponse(new BigDecimal("14900.00"), "COP"),
                EstadoPago.APROBADO, "MP-1", new MontoResponse(BigDecimal.ZERO, "COP"),
                null, TestFixtures.AHORA, false, new MontoResponse(new BigDecimal("14900.00"), "COP"));
    }

    @Test
    void consultaHistorial() throws Exception {
        when(pagoPort.historial("USER-1")).thenReturn(List.of(pago()));

        mockMvc.perform(get("/api/v1/pagos/historial").header("X-User-Id", "USER-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].estado").value("APROBADO"))
                .andExpect(jsonPath("$[0].monto.valor").value(14900.00));
    }

    @Test
    void historialVacioDevuelve200() throws Exception {
        when(pagoPort.historial("USER-1")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/pagos/historial").header("X-User-Id", "USER-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void reintentaPago() throws Exception {
        when(pagoPort.reintentar(eq(10L), anyString(), eq("USER-1")))
                .thenReturn(new ReintentoResponse(11L, EstadoPago.PENDIENTE,
                        "https://mp/new", "Nuevo intento"));

        mockMvc.perform(post("/api/v1/pagos/10/reintentar")
                        .header("X-User-Id", "USER-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"emailPayer":"a@b.com"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordenId").value(11))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    void reembolsaPago() throws Exception {
        when(pagoPort.reembolsar(eq(10L), any(), anyString(), eq("ADMIN")))
                .thenReturn(new ReembolsoResponse(10L, EstadoPago.REEMBOLSADO,
                        new MontoResponse(new BigDecimal("100.00"), "COP"),
                        new MontoResponse(BigDecimal.ZERO, "COP"), false,
                        "Motivo", true, "Reembolso total"));

        mockMvc.perform(post("/api/v1/pagos/10/reembolsar")
                        .header("X-User-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"monto":100.00,"motivo":"Motivo"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("REEMBOLSADO"))
                .andExpect(jsonPath("$.renovacionCancelada").value(true));
    }
}
