package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.pagos_suscripciones.domain.port.in.PagoPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PagoController.class)
class PagoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PagoPort pagoPort;

    @Test
    void debeReembolsarPago() throws Exception {
        mockMvc.perform(post("/api/v1/pagos/1/reembolsar")
                .param("monto", "50")
                .param("motivo", "Cliente insatisfecho"))
                .andExpect(status().isOk());

        verify(pagoPort).reembolsarPago(1L, "50", "Cliente insatisfecho");
    }

    @Test
    void debeConsultarHistorial() throws Exception {
        mockMvc.perform(get("/api/v1/pagos/historial/USER-1"))
                .andExpect(status().isOk());
    }
}