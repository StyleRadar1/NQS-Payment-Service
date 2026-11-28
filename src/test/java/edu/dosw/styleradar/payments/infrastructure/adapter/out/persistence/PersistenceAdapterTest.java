package edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.payments.domain.model.PaymentStatus;
import edu.dosw.styleradar.payments.domain.model.SubscriptionStatus;
import edu.dosw.styleradar.payments.domain.model.Amount;
import edu.dosw.styleradar.payments.domain.model.PaymentOrder;
import edu.dosw.styleradar.payments.domain.model.Plan;
import edu.dosw.styleradar.payments.domain.model.Subscription;
import edu.dosw.styleradar.payments.domain.model.SubscriberType;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.entity.PaymentOrderEntity;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.entity.PlanEntity;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.entity.SubscriptionEntity;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.repository.PaymentOrderJpaRepository;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.repository.PlanJpaRepository;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.repository.SubscriptionJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({
        PlanPersistenceAdapter.class,
        SubscriptionPersistenceAdapter.class,
        PaymentOrderPersistenceAdapter.class,
        WebhookLogPersistenceAdapter.class
})
class PersistenceAdapterTest {

    @Autowired
    private PlanPersistenceAdapter planAdapter;
    @Autowired
    private SubscriptionPersistenceAdapter subscriptionAdapter;
    @Autowired
    private PaymentOrderPersistenceAdapter orderAdapter;
    @Autowired
    private WebhookLogPersistenceAdapter webhookLogAdapter;
    @Autowired
    private PlanJpaRepository planRepository;
    @Autowired
    private SubscriptionJpaRepository subscriptionRepository;
    @Autowired
    private PaymentOrderJpaRepository orderRepository;

    private Long planUserId;
    private Long planStoreId;

    @BeforeEach
    void seedPlanes() {
        planUserId = planRepository.save(PlanEntity.builder()
                .name("Radar+")
                .subscriberType(SubscriberType.USER)
                .price(new BigDecimal("14900.00"))
                .currency("COP")
                .limits(Map.of("max_wishlist", -1))
                .active(true)
                .build()).getId();
        planStoreId = planRepository.save(PlanEntity.builder()
                .name("Almacén Básico")
                .subscriberType(SubscriberType.STORE)
                .price(new BigDecimal("89000.00"))
                .currency("COP")
                .limits(Map.of("max_productos", 50))
                .active(true)
                .build()).getId();
        planRepository.save(PlanEntity.builder()
                .name("Radar")
                .subscriberType(SubscriberType.USER)
                .price(BigDecimal.ZERO)
                .currency("COP")
                .limits(Map.of())
                .active(true)
                .build());
    }

    @Test
    void planAdapterListaYBuscaPlanes() {
        assertThat(planAdapter.listActive()).hasSize(3);
        assertThat(planAdapter.findById(planUserId)).isPresent();
        assertThat(planAdapter.findPaidPlanByType(SubscriberType.USER))
                .get()
                .extracting(Plan::getName)
                .isEqualTo("Radar+");
        assertThat(planAdapter.findPaidPlanByType(SubscriberType.STORE))
                .get()
                .extracting(Plan::getName)
                .isEqualTo("Almacén Básico");
    }

    @Test
    void subscriptionAdapterGuardaYConsulta() {
        Subscription pending = Subscription.pending("USER-1", SubscriberType.USER,
                planUserId, LocalDateTime.now());

        Subscription guardada = subscriptionAdapter.save(pending);

        assertThat(guardada.getId()).isNotNull();
        assertThat(subscriptionAdapter.findById(guardada.getId())).isPresent();
        assertThat(subscriptionAdapter.findLatestBySubscriber("USER-1")).isPresent();
        assertThat(subscriptionAdapter.findActiveOrPending("USER-1")).isPresent();
        assertThat(subscriptionAdapter.findActiveOrPending("OTRO")).isEmpty();
    }

    @Test
    void subscriptionAdapterConsultaActivesExpiredYPreapproval() {
        Subscription active = Subscription.rehydrate(null, "STORE-1", SubscriberType.STORE,
                planStoreId, null, SubscriptionStatus.ACTIVE,
                LocalDate.now().minusMonths(1), LocalDate.now().minusDays(1), true,
                "PRE-1", "900123456", LocalDateTime.now());
        Subscription guardada = subscriptionAdapter.save(active);

        assertThat(subscriptionAdapter.findByPreapprovalId("PRE-1")).isPresent();
        assertThat(subscriptionAdapter.findExpiredActive(LocalDate.now()))
                .extracting(Subscription::getId)
                .contains(guardada.getId());
        assertThat(subscriptionAdapter.findExpiredActive(LocalDate.now().minusMonths(2))).isEmpty();
    }

    @Test
    void orderAdapterGuardaYConsultaPorSubscriber() {
        Subscription subscription = subscriptionAdapter.save(Subscription.pending(
                "USER-1", SubscriberType.USER, planUserId, LocalDateTime.now()));
        PaymentOrder pending = PaymentOrder.pending(subscription.getId(),
                Amount.cop(new BigDecimal("14900.00")), LocalDateTime.now().minusDays(1));

        PaymentOrder guardada = orderAdapter.save(pending);

        assertThat(guardada.getId()).isNotNull();
        assertThat(orderAdapter.findById(guardada.getId())).isPresent();
        assertThat(orderAdapter.findBySubscription(subscription.getId())).hasSize(1);
        assertThat(orderAdapter.findBySubscriber("USER-1")).hasSize(1);
        assertThat(orderAdapter.findPendingBySubscription(subscription.getId())).isPresent();
        assertThat(orderAdapter.findPendingBefore(LocalDateTime.now())).hasSize(1);
        assertThat(orderAdapter.list(null)).hasSize(1);
        assertThat(orderAdapter.list(PaymentStatus.PENDING)).hasSize(1);
        assertThat(orderAdapter.list(PaymentStatus.APPROVED)).isEmpty();
    }

    @Test
    void orderAdapterBuscaPorPaymentExternoYActualizaStatus() {
        Subscription subscription = subscriptionAdapter.save(Subscription.pending(
                "USER-2", SubscriberType.USER, planUserId, LocalDateTime.now()));
        PaymentOrder pending = PaymentOrder.pending(subscription.getId(),
                Amount.cop(new BigDecimal("14900.00")), LocalDateTime.now());
        pending.updateStatus(PaymentStatus.APPROVED, "MP-99");

        PaymentOrder guardada = orderAdapter.save(pending);

        assertThat(orderAdapter.findByExternalPaymentId("MP-99")).isPresent();
        assertThat(guardada.getStatus()).isEqualTo(PaymentStatus.APPROVED);
    }

    @Test
    void webhookLogAdapterRegistraYConsulta() {
        assertThat(webhookLogAdapter.exists("MP-1")).isFalse();

        webhookLogAdapter.register("MP-1", Map.of("type", "payment"), "PROCESSED");

        assertThat(webhookLogAdapter.exists("MP-1")).isTrue();
    }

    @Test
    void orderEntitySeMapeaConSubscription() {
        Subscription subscription = subscriptionAdapter.save(Subscription.pending(
                "USER-3", SubscriberType.USER, planUserId, LocalDateTime.now()));
        PaymentOrderEntity entity = orderRepository.save(PaymentOrderEntity.builder()
                .subscriptionId(subscription.getId())
                .amount(new BigDecimal("14900.00"))
                .currency("COP")
                .status(PaymentStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build());

        assertThat(entity.getId()).isNotNull();
        assertThat(subscriptionRepository.findById(subscription.getId())).isPresent();
    }
}
