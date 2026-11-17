package edu.dosw.styleradar.pagos.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.pagos.TestFixtures;
import edu.dosw.styleradar.pagos.application.dto.response.CancelacionResponse;
import edu.dosw.styleradar.pagos.application.dto.response.IniciarSuscripcionResponse;
import edu.dosw.styleradar.pagos.application.dto.response.PlanResponse;
import edu.dosw.styleradar.pagos.application.dto.response.SuscripcionResponse;
import edu.dosw.styleradar.pagos.application.port.in.SuscripcionPort;
import edu.dosw.styleradar.pagos.domain.exception.SolicitudInvalidaException;
import edu.dosw.styleradar.pagos.domain.model.EstadoSuscripcion;
import edu.dosw.styleradar.pagos.domain.model.TipoSuscriptor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SuscripcionController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("SuscripcionController")
class SuscripcionControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private SuscripcionPort suscripcionPort;

    @Test
    void iniciaSuscripcion() throws Exception {
        when(suscripcionPort.iniciar(eq("USER-1"), any()))
                .thenReturn(new IniciarSuscripcionResponse(100L, 200L,
                        EstadoSuscripcion.PENDIENTE, "https://mp/init", "Continúa"));

        mockMvc.perform(post("/api/v1/suscripciones/iniciar")
                        .header("X-User-Id", "USER-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipoSuscriptor":"USUARIO","emailPayer":"a@b.com","planId":2}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.suscripcionId").value(100))
                .andExpect(jsonPath("$.ordenId").value(200))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.initPoint").value("https://mp/init"));
    }

    @Test
    void validaCorreoObligatorio() throws Exception {
        mockMvc.perform(post("/api/v1/suscripciones/iniciar")
                        .header("X-User-Id", "USER-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipoSuscriptor":"USUARIO","emailPayer":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors.emailPayer").exists());
    }

    @Test
    void exigeCabeceraDeUsuario() throws Exception {
        mockMvc.perform(post("/api/v1/suscripciones/iniciar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipoSuscriptor":"USUARIO","emailPayer":"a@b.com"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void traduceErrorDeNegocio() throws Exception {
        when(suscripcionPort.iniciar(anyString(), any()))
                .thenThrow(new SolicitudInvalidaException("Correo inválido"));

        mockMvc.perform(post("/api/v1/suscripciones/iniciar")
                        .header("X-User-Id", "USER-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipoSuscriptor":"USUARIO","emailPayer":"a@b.com"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Correo inválido"));
    }

    @Test
    void consultaSuscripcion() throws Exception {
        PlanResponse plan = PlanResponse.desde(TestFixtures.planRadarPlus());
        SuscripcionResponse response = new SuscripcionResponse(100L, "USER-1",
                TipoSuscriptor.USUARIO, plan, EstadoSuscripcion.ACTIVA,
                TestFixtures.HOY.minusDays(10), TestFixtures.HOY.plusDays(20),
                true, true, null, null, null);
        when(suscripcionPort.consultar("USER-1", "USER-1", "USUARIO")).thenReturn(response);

        mockMvc.perform(get("/api/v1/suscripciones/USER-1")
                        .header("X-User-Id", "USER-1")
                        .header("X-User-Role", "USUARIO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACTIVA"))
                .andExpect(jsonPath("$.acceso").value(true))
                .andExpect(jsonPath("$.plan.nombre").value("Radar+"));
    }

    @Test
    void cancelaRenovacion() throws Exception {
        when(suscripcionPort.cancelar(eq("USER-1"), anyBoolean(), eq("USER-1"), eq("USUARIO")))
                .thenReturn(new CancelacionResponse(100L, false, EstadoSuscripcion.ACTIVA,
                        TestFixtures.HOY.plusDays(20), true, "Cancelada"));

        mockMvc.perform(post("/api/v1/suscripciones/USER-1/cancelar")
                        .header("X-User-Id", "USER-1")
                        .header("X-User-Role", "USUARIO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"confirmacion":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autoRenovacion").value(false))
                .andExpect(jsonPath("$.cancelacionConfirmada").value(true));

        verify(suscripcionPort).cancelar("USER-1", true, "USER-1", "USUARIO");
    }
}
