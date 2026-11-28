package edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.payments.domain.model.PaymentStatus;
import edu.dosw.styleradar.payments.domain.model.Amount;
import edu.dosw.styleradar.payments.domain.model.PaymentOrder;
import edu.dosw.styleradar.payments.domain.model.Plan;
import edu.dosw.styleradar.payments.domain.model.Subscription;
import edu.dosw.styleradar.payments.domain.model.SubscriberType;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.entity.PaymentOrderEntity;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.entity.PlanEntity;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.entity.SubscriptionEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
class PersistenceMapperTest {

    @Test
    void mapeaPlan() {
        PlanEntity entity = PlanEntity.builder()
                .id(2L)
                .name("Radar+")
                .subscriberType(SubscriberType.USER)
                .price(new BigDecimal("14900.00"))
                .currency("COP")
                .limits(Map.of("max_wishlist", -1))
                .active(true)
                .build();

        Plan plan = PersistenceMapper.toDomain(entity);

        assertThat(plan.getId()).isEqualTo(2L);
        assertThat(plan.getName()).isEqualTo("Radar+");
        assertThat(plan.getSubscriberType()).isEqualTo(SubscriberType.USER);
        assertThat(plan.getPrice()).isEqualTo(new Amount(new BigDecimal("14900.00"), "COP"));
        assertThat(plan.getLimits()).containsEntry("max_wishlist", -1);
        assertThat(plan.isActive()).isTrue();
    }

    @Test
    void mapeaSubscriptionEnAmbasDirecciones() {
        LocalDateTime ahora = LocalDateTime.of(2026, 11, 14, 10, 0);
        SubscriptionEntity entity = SubscriptionEntity.builder()
                .id(10L)
                .planId(3L)
                .scheduledPlanId(2L)
                .subscriberId("STORE-1")
                .subscriberType(SubscriberType.STORE)
                .status(edu.dosw.styleradar.payments.domain.model.SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 11, 1))
                .endDate(LocalDate.of(2026, 12, 1))
                .autoRenewal(true)
                .mercadoPagoPreapprovalId("PRE-1")
                .taxId("900123456")
                .updatedAt(ahora)
                .build();

        Subscription dominio = PersistenceMapper.toDomain(entity);
        SubscriptionEntity vuelta = PersistenceMapper.toEntity(dominio);

        assertThat(dominio.getId()).isEqualTo(10L);
        assertThat(dominio.getTaxId()).isEqualTo("900123456");
        assertThat(dominio.getMercadoPagoPreapprovalId()).isEqualTo("PRE-1");
        assertThat(vuelta.getSubscriberId()).isEqualTo("STORE-1");
        assertThat(vuelta.getPlanId()).isEqualTo(3L);
        assertThat(vuelta.getScheduledPlanId()).isEqualTo(2L);
        assertThat(vuelta.getTaxId()).isEqualTo("900123456");
        assertThat(vuelta.getStartDate()).isEqualTo(LocalDate.of(2026, 11, 1));
    }

    @Test
    void mapeaOrder() {
        LocalDateTime ahora = LocalDateTime.of(2026, 11, 14, 10, 0);
        PaymentOrderEntity entity = PaymentOrderEntity.builder()
                .id(1L)
                .subscriptionId(10L)
                .amount(new BigDecimal("100.00"))
                .currency("COP")
                .status(PaymentStatus.APPROVED)
                .externalPaymentId("MP-1")
                .createdAt(ahora)
                .build();

        PaymentOrder dominio = PersistenceMapper.toDomain(entity);
        PaymentOrderEntity vuelta = PersistenceMapper.toEntity(dominio);

        assertThat(dominio.getAmount().value()).isEqualByComparingTo("100.00");
        assertThat(dominio.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(vuelta.getSubscriptionId()).isEqualTo(10L);
        assertThat(vuelta.getAmount()).isEqualByComparingTo("100.00");
        assertThat(vuelta.getCurrency()).isEqualTo("COP");
        assertThat(vuelta.getExternalPaymentId()).isEqualTo("MP-1");
    }
}
