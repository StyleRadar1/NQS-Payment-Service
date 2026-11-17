package edu.dosw.styleradar.pagos.infrastructure.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ExceptionTestController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("GlobalExceptionHandler (ADR-10)")
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void mapea400DeNegocio() throws Exception {
        mockMvc.perform(get("/test/400"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.path").value("/test/400"));
    }

    @Test
    void mapea403() throws Exception {
        mockMvc.perform(get("/test/403"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
        mockMvc.perform(get("/test/denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    void mapea404() throws Exception {
        mockMvc.perform(get("/test/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("SUSCRIPCION_NOT_FOUND"));
    }

    @Test
    void mapea409() throws Exception {
        mockMvc.perform(get("/test/409"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("IDEMPOTENCY"));
        mockMvc.perform(get("/test/integrity"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("CONFLICT"));
    }

    @Test
    void mapea422() throws Exception {
        mockMvc.perform(get("/test/422"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("PAYMENT_ERROR"));
    }

    @Test
    void mapea500SinStacktrace() throws Exception {
        mockMvc.perform(get("/test/500"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("Ocurrió un error inesperado. Inténtalo nuevamente."));
    }

    @Test
    void mapeaEstadoIlegal400() throws Exception {
        mockMvc.perform(get("/test/illegal"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INVALID_STATE"))
                .andExpect(jsonPath("$.message").value("Estado inválido"));
    }

    @Test
    void mapeaCabeceraFaltante401() throws Exception {
        mockMvc.perform(get("/test/header"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void mapeaParametroInvalido400() throws Exception {
        mockMvc.perform(get("/test/type").param("estado", "OTRO"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"));
    }

    @Test
    void mapeaValidacionYcuerpoIlegible() throws Exception {
        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"tipoSuscriptor":"USUARIO","emailPayer":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.validationErrors.emailPayer").exists());

        mockMvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}
