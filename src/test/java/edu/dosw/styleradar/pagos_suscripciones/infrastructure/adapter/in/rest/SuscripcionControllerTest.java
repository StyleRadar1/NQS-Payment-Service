package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.Suscripcion;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.TipoSuscriptor;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.in.SuscripcionPort;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SuscripcionController.class) // Solo levanta este controlador
class SuscripcionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SuscripcionPort suscripcionPort; // Simulamos el caso de uso

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void debeIniciarSuscripcionYRetornarLink() throws Exception {
        SuscripcionController.CrearSuscripcionRequest request = new SuscripcionController.CrearSuscripcionRequest(
                "USER-1", 2L, "USUARIO", "test@test.com"
        );

        when(suscripcionPort.iniciarSuscripcion(eq("USER-1"), eq(2L), eq(TipoSuscriptor.USUARIO), eq("test@test.com")))
                .thenReturn("https://mercadopago.com/checkout");

        mockMvc.perform(post("/api/v1/suscripciones/iniciar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("https://mercadopago.com/checkout"));
    }

    @Test
    void debeConsultarSuscripcion() throws Exception {
        Suscripcion suscripcion = new Suscripcion(1L, "USER-1", TipoSuscriptor.USUARIO, 2L);
        when(suscripcionPort.consultarSuscripcion("USER-1")).thenReturn(suscripcion);

        mockMvc.perform(get("/api/v1/suscripciones/USER-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suscriptorId").value("USER-1"))
                .andExpect(jsonPath("$.planId").value(2));
    }

    @Test
    void debeCancelarRenovacion() throws Exception {
        mockMvc.perform(post("/api/v1/suscripciones/USER-1/cancelar"))
                .andExpect(status().isOk());
    }
}