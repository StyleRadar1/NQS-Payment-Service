package edu.dosw.styleradar.payments.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void exigeUserEnHistory() throws Exception {
        mockMvc.perform(get("/api/v1/payments/history"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));

        mockMvc.perform(get("/api/v1/payments/history").header("X-User-Id", "USER-1"))
                .andExpect(status().isOk());
    }

    @Test
    void exigeRolAdminEnListadoGlobal() throws Exception {
        mockMvc.perform(get("/api/v1/admin/payments").header("X-User-Id", "USER-1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));

        mockMvc.perform(get("/api/v1/admin/payments")
                        .header("X-User-Id", "ADMIN-1")
                        .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/admin/payments")
                        .header("X-User-Id", "ADMIN-2")
                        .header("X-User-Role", "[ADMIN_STYLERADAR]"))
                .andExpect(status().isOk());
    }

    @Test
    void healthEsPublicoYWebhookAceptaDummyEnTest() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(post("/api/v1/webhooks/mercadopago")
                        .header("x-signature", "dummy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"payment","data":{"id":"12345"}}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ok"));
    }
}
