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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SuscripcionController.class)
class SuscripcionControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private SuscripcionPort suscripcionPort;
    @Autowired private ObjectMapper objectMapper;

    @Test
    void debeIniciarSuscripcionYRetornarLink() throws Exception {
        SuscripcionController.CrearSuscripcionRequest req =
            new SuscripcionController.CrearSuscripcionRequest(
                "USER-1", 2L, "USUARIO", "test@test.com");

        when(suscripcionPort.iniciarSuscripcion("USER-1", 2L, TipoSuscriptor.USUARIO, "test@test.com"))
                .thenReturn("https://mercadopago.com/checkout");

        mockMvc.perform(post("/api/v1/suscripciones/iniciar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string("https://mercadopago.com/checkout"));
    }

    @Test
    void debeConsultarSuscripcion() throws Exception {
        Suscripcion s = new Suscripcion(1L, "USER-1", TipoSuscriptor.USUARIO, 2L);
        when(suscripcionPort.consultarSuscripcion("USER-1")).thenReturn(s);

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

    @Test
    void cambiarPlanUpgradeDebeRetornarInitPoint() throws Exception {
        when(suscripcionPort.programarCambioPlan("USER-1", 3L, "test@test.com"))
                .thenReturn("https://mp.com/upgrade");

        mockMvc.perform(put("/api/v1/suscripciones/USER-1/cambiar-plan")
                .param("nuevoPlanId", "3")
                .param("emailPayer", "test@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("UPGRADE"))
                .andExpect(jsonPath("$.init_point").value("https://mp.com/upgrade"));
    }

    @Test
    void cambiarPlanDowngradeNoDebeRetornarInitPoint() throws Exception {
        when(suscripcionPort.programarCambioPlan("USER-1", 1L, "test@test.com"))
                .thenReturn(null); // downgrade devuelve null

        mockMvc.perform(put("/api/v1/suscripciones/USER-1/cambiar-plan")
                .param("nuevoPlanId", "1")
                .param("emailPayer", "test@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("DOWNGRADE"))
                .andExpect(jsonPath("$.init_point").doesNotExist())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void cambiarPlanDebeRequerirEmailPayer() throws Exception {
        mockMvc.perform(put("/api/v1/suscripciones/USER-1/cambiar-plan")
                .param("nuevoPlanId", "3"))
                .andExpect(status().isBadRequest());
    }
}