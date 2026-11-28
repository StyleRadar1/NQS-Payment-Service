package edu.dosw.styleradar.payments;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.dosw.styleradar.payments.application.port.out.MercadoPagoPort;
import edu.dosw.styleradar.payments.domain.model.SubscriberType;
import edu.dosw.styleradar.payments.domain.model.SubscriptionStatus;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.entity.SubscriptionEntity;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.repository.SubscriptionJpaRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/** Con los filtros de seguridad activos: la consulta del mapa no exige token y solo trae tiendas al dia. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class StoresUpToDateIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SubscriptionJpaRepository subscriptions;

    @MockBean
    private MercadoPagoPort mercadoPago;

    @Test
    void listaSoloLasTiendasConSuscripcionActivaYVigente() throws Exception {
        LocalDate hoy = LocalDate.now();
        guardar("s-1", SubscriberType.STORE, SubscriptionStatus.ACTIVE, hoy.plusDays(10), "900000001-1");
        guardar("s-2", SubscriberType.STORE, SubscriptionStatus.ACTIVE, hoy.minusDays(5), "900000002-2");
        guardar("s-3", SubscriberType.STORE, SubscriptionStatus.PENDING, hoy.plusDays(10), "900000003-3");
        guardar("s-4", SubscriberType.STORE, SubscriptionStatus.SUSPENDED, hoy.plusDays(10), "900000004-4");
        guardar("u-1", SubscriberType.USER, SubscriptionStatus.ACTIVE, hoy.plusDays(10), null);

        mockMvc.perform(get("/api/v1/subscriptions/stores/up-to-date"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.taxIds.length()").value(1))
                .andExpect(jsonPath("$.taxIds[0]").value("900000001-1"));
    }

    @Test
    void elRestoDeSuscripcionesSigueExigiendoSesion() throws Exception {
        int estado = mockMvc.perform(get("/api/v1/subscriptions/u-1"))
                .andReturn().getResponse().getStatus();

        assertThat(estado).isIn(400, 401, 403);
    }

    private void guardar(String suscriptor, SubscriberType tipo, SubscriptionStatus estado, LocalDate fin, String nit) {
        subscriptions.save(SubscriptionEntity.builder()
                .planId(1L)
                .subscriberId(suscriptor)
                .subscriberType(tipo)
                .status(estado)
                .startDate(fin.minusMonths(1))
                .endDate(fin)
                .autoRenewal(true)
                .taxId(nit)
                .updatedAt(LocalDateTime.now())
                .build());
    }
}
