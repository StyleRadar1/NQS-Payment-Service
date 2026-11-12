package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.pagos_suscripciones.domain.port.in.PagoPort;
import edu.dosw.styleradar.pagos_suscripciones.infrastructure.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PagoController.class)
@ActiveProfiles("test")
@Import(GlobalExceptionHandler.class)   // <-- clave: sin esto, los 400/403 devuelven 500
class PagoControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean  private PagoPort pagoPort;

    // ---------- REEMBOLSO ----------

    @Test
    void debeReembolsarPagoComoAdmin() throws Exception {
        mockMvc.perform(post("/api/v1/pagos/1/reembolsar")
                        .header("X-User-Role", "ADMIN")
                        .param("monto", "50")
                        .param("motivo", "Cliente insatisfecho"))
                .andExpect(status().isOk());

        verify(pagoPort).reembolsarPago(1L, "50", "Cliente insatisfecho");
    }

    @Test
    void debeRetornar403SiNoEsAdmin() throws Exception {
        mockMvc.perform(post("/api/v1/pagos/1/reembolsar")
                        .header("X-User-Role", "USER")
                        .param("monto", "50")
                        .param("motivo", "test"))
                .andExpect(status().isForbidden());

        verify(pagoPort, never()).reembolsarPago(any(), anyString(), anyString());
    }

    @Test
    void debeRetornar403SinHeaderDeRol() throws Exception {
        mockMvc.perform(post("/api/v1/pagos/1/reembolsar")
                        .param("monto", "50")
                        .param("motivo", "test"))
                .andExpect(status().isForbidden());
    }

    // ---------- HISTORIAL ----------

    @Test
    void debeConsultarHistorialUsandoHeaderXUserId() throws Exception {
        mockMvc.perform(get("/api/v1/pagos/historial")
                        .header("X-User-Id", "USER-1"))
                .andExpect(status().isOk());

        verify(pagoPort).consultarHistorialPagos("USER-1");
    }

    @Test
    void debeFallarHistorialSinHeaderXUserId() throws Exception {
        mockMvc.perform(get("/api/v1/pagos/historial"))
                .andExpect(status().isBadRequest());
    }

    // ---------- REINTENTO ----------

    @Test
    void debeReintentarPago() throws Exception {
        when(pagoPort.reintentarPago(eq(1L), anyString(), anyString()))
                .thenReturn("https://mp.com/retry");

        mockMvc.perform(post("/api/v1/pagos/1/reintentar")
                        .header("X-User-Id", "USER-1")
                        .param("emailPayer", "test@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.init_point").value("https://mp.com/retry"));

        verify(pagoPort).reintentarPago(1L, "USER-1", "test@test.com");
    }

    @Test
    void debeRetornar400SiReintentoFalla() throws Exception {
        when(pagoPort.reintentarPago(any(), anyString(), anyString()))
                .thenThrow(new IllegalStateException("La suscripción ya está activa."));

        mockMvc.perform(post("/api/v1/pagos/1/reintentar")
                        .header("X-User-Id", "USER-1")
                        .param("emailPayer", "test@test.com"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("INVALID_STATE"))
                .andExpect(jsonPath("$.message").value("La suscripción ya está activa."))
                .andExpect(jsonPath("$.path").value("/api/v1/pagos/1/reintentar"));
    }
}