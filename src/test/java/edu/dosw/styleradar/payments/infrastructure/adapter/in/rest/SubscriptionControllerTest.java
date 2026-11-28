package edu.dosw.styleradar.payments.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.payments.TestFixtures;
import edu.dosw.styleradar.payments.application.dto.response.CancellationResponse;
import edu.dosw.styleradar.payments.application.dto.response.StartSubscriptionResponse;
import edu.dosw.styleradar.payments.application.dto.response.PlanResponse;
import edu.dosw.styleradar.payments.application.dto.response.SubscriptionResponse;
import edu.dosw.styleradar.payments.application.port.in.SubscriptionPort;
import edu.dosw.styleradar.payments.application.port.out.StoreAccountPort;
import edu.dosw.styleradar.payments.domain.exception.InvalidRequestException;
import edu.dosw.styleradar.payments.domain.model.SubscriptionStatus;
import edu.dosw.styleradar.payments.domain.model.SubscriberType;
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

@WebMvcTest(controllers = SubscriptionController.class)
@AutoConfigureMockMvc(addFilters = false)
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private SubscriptionPort subscriptionPort;
    @MockBean
    private StoreAccountPort storeAccountPort;

    @Test
    void iniciaSubscription() throws Exception {
        when(subscriptionPort.start(eq("USER-1"), any()))
                .thenReturn(new StartSubscriptionResponse(100L, 200L,
                        SubscriptionStatus.PENDING, "https://mp/init", "Continúa"));

        mockMvc.perform(post("/api/v1/subscriptions/start")
                        .header("X-User-Id", "USER-1")
                        .header("X-User-Role", "BUYER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subscriberType":"USER","payerEmail":"a@b.com","planId":2}
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.subscriptionId").value(100))
                .andExpect(jsonPath("$.orderId").value(200))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.initPoint").value("https://mp/init"));
    }

    @Test
    void validaEmailObligatorio() throws Exception {
        mockMvc.perform(post("/api/v1/subscriptions/start")
                        .header("X-User-Id", "USER-1")
                        .header("X-User-Role", "BUYER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subscriberType":"USER","payerEmail":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.payerEmail").exists());
    }

    @Test
    void exigeCabeceraDeUser() throws Exception {
        mockMvc.perform(post("/api/v1/subscriptions/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subscriberType":"USER","payerEmail":"a@b.com"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void traduceErrorDeNegocio() throws Exception {
        when(subscriptionPort.start(anyString(), any()))
                .thenThrow(new InvalidRequestException("Email inválido"));

        mockMvc.perform(post("/api/v1/subscriptions/start")
                        .header("X-User-Id", "USER-1")
                        .header("X-User-Role", "BUYER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subscriberType":"USER","payerEmail":"a@b.com"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.detail").value("Email inválido"));
    }

    @Test
    void iniciaSubscriptionDeTiendaConNitPropio() throws Exception {
        when(storeAccountPort.belongsToStore("STORE-1", "900123456", "Bearer test-token"))
                .thenReturn(true);
        when(subscriptionPort.start(eq("STORE-1"), any()))
                .thenReturn(new StartSubscriptionResponse(100L, 200L,
                        SubscriptionStatus.PENDING, "https://mp/init", "Continúa"));

        mockMvc.perform(post("/api/v1/subscriptions/start")
                        .header("X-User-Id", "STORE-1")
                        .header("X-User-Role", "STORE_ADMIN")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subscriberType":"STORE","payerEmail":"store@test.com","planId":3,"taxId":"900123456"}
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.orderId").value(200));
    }

    @Test
    void rechazaNitQueNoPerteneceALaTienda() throws Exception {
        when(storeAccountPort.belongsToStore("STORE-1", "900123456", "Bearer test-token"))
                .thenReturn(false);

        mockMvc.perform(post("/api/v1/subscriptions/start")
                        .header("X-User-Id", "STORE-1")
                        .header("X-User-Role", "STORE_ADMIN")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subscriberType":"STORE","payerEmail":"store@test.com","planId":3,"taxId":"900123456"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void rechazaTiendaSinRolDeTienda() throws Exception {
        mockMvc.perform(post("/api/v1/subscriptions/start")
                        .header("X-User-Id", "STORE-1")
                        .header("X-User-Role", "BUYER")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subscriberType":"STORE","payerEmail":"store@test.com","planId":3,"taxId":"900123456"}
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void rechazaStoreSinAuthorizationParaValidarNit() throws Exception {
        mockMvc.perform(post("/api/v1/subscriptions/start")
                        .header("X-User-Id", "STORE-1")
                        .header("X-User-Role", "STORE_ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subscriberType":"STORE","payerEmail":"store@test.com","planId":3,"taxId":"900123456"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void consultaSubscription() throws Exception {
        PlanResponse plan = PlanResponse.from(TestFixtures.planRadarPlus());
        SubscriptionResponse response = new SubscriptionResponse(100L, "USER-1",
                SubscriberType.USER, plan, SubscriptionStatus.ACTIVE,
                TestFixtures.HOY.minusDays(10), TestFixtures.HOY.plusDays(20),
                true, true, null, null, null);
        when(subscriptionPort.get("USER-1", "USER-1", "USER")).thenReturn(response);

        mockMvc.perform(get("/api/v1/subscriptions/USER-1")
                        .header("X-User-Id", "USER-1")
                        .header("X-User-Role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.access").value(true))
                .andExpect(jsonPath("$.plan.name").value("Radar+"));
    }

    @Test
    void cancelaRenovacion() throws Exception {
        when(subscriptionPort.cancel(eq("USER-1"), anyBoolean(), eq("USER-1"), eq("USER")))
                .thenReturn(new CancellationResponse(100L, false, SubscriptionStatus.ACTIVE,
                        TestFixtures.HOY.plusDays(20), true, "Cancelada"));

        mockMvc.perform(post("/api/v1/subscriptions/USER-1/cancel")
                        .header("X-User-Id", "USER-1")
                        .header("X-User-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"confirmed":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autoRenewal").value(false))
                .andExpect(jsonPath("$.cancellationConfirmed").value(true));

        verify(subscriptionPort).cancel("USER-1", true, "USER-1", "USER");
    }
}

