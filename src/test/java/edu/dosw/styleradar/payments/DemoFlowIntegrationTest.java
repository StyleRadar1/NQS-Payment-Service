package edu.dosw.styleradar.payments;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dosw.styleradar.payments.application.port.out.MercadoPagoPort;
import edu.dosw.styleradar.payments.domain.model.SubscriberType;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.entity.PlanEntity;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.repository.PlanJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end demo over HTTP with H2 and a mocked Mercado Pago gateway.
 * No Docker, PostgreSQL, Redis or RabbitMQ required.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class DemoFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PlanJpaRepository planRepository;

    @MockBean
    private MercadoPagoPort mercadoPago;

    private final AtomicReference<String> externalReference = new AtomicReference<>();

    @BeforeEach
    void seedPlansAndGateway() {
        if (planRepository.count() == 0) {
            planRepository.save(PlanEntity.builder()
                    .id(1L)
                    .name("Radar")
                    .subscriberType(SubscriberType.USER)
                    .price(BigDecimal.ZERO)
                    .currency("COP")
                    .limits(Map.of("max_wishlist", 20))
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .build());
            planRepository.save(PlanEntity.builder()
                    .id(2L)
                    .name("Radar+")
                    .subscriberType(SubscriberType.USER)
                    .price(new BigDecimal("14900.00"))
                    .currency("COP")
                    .limits(Map.of("max_wishlist", -1, "try_on_uses_per_month", 30))
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .build());
            planRepository.save(PlanEntity.builder()
                    .id(3L)
                    .name("Store Basic")
                    .subscriberType(SubscriberType.STORE)
                    .price(new BigDecimal("89000.00"))
                    .currency("COP")
                    .limits(Map.of("max_products", 50))
                    .active(true)
                    .createdAt(LocalDateTime.now())
                    .build());
        }

        when(mercadoPago.createPreapproval(any())).thenAnswer(invocation -> {
            MercadoPagoPort.PreapprovalRequest request = invocation.getArgument(0);
            externalReference.set(request.externalReference());
            return new MercadoPagoPort.PreapprovalCreated(
                    "PRE-1", "https://www.mercadopago.com/checkout/PRE-1", "pending");
        });
        when(mercadoPago.getPayment("MP-1")).thenAnswer(invocation ->
                new MercadoPagoPort.MercadoPagoPayment(
                        "MP-1", "approved", externalReference.get(), "PRE-1",
                        new BigDecimal("14900.00"), "COP"));
    }

    @Test
    void completeSubscriptionPaymentFlow() throws Exception {
        MvcResult start = mockMvc.perform(post("/api/v1/subscriptions/start")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"subscriberType":"USER","payerEmail":"demo@testuser.com","planId":2}
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();

        JsonNode startBody = objectMapper.readTree(start.getResponse().getContentAsString());
        long subscriptionId = startBody.get("subscriptionId").asLong();
        assertThat(subscriptionId).isPositive();
        assertThat(startBody.get("initPoint").asText()).contains("mercadopago");

        mockMvc.perform(post("/api/v1/webhooks/mercadopago")
                        .header("x-signature", "dummy")
                        .header("x-request-id", "demo-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type":"payment","data":{"id":"MP-1"}}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/subscriptions/" + subscriptionId)
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.autoRenewal").value(true));

        mockMvc.perform(get("/api/v1/payments/history").header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("APPROVED"))
                .andExpect(jsonPath("$[0].amount.value").value(14900.00));

        mockMvc.perform(post("/api/v1/subscriptions/" + subscriptionId + "/cancel")
                        .header("X-User-Id", "1")
                        .header("X-User-Role", "USER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"confirmed":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autoRenewal").value(false));
    }
}
