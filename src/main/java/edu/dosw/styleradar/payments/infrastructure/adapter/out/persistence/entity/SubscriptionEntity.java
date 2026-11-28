package edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.entity;

import edu.dosw.styleradar.payments.domain.model.SubscriptionStatus;
import edu.dosw.styleradar.payments.domain.model.SubscriberType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Column(name = "scheduled_plan_id")
    private Long scheduledPlanId;

    @Column(name = "subscriber_id", nullable = false, length = 50)
    private String subscriberId;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscriber_type", nullable = false, length = 20)
    private SubscriberType subscriberType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SubscriptionStatus status;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "auto_renewal", nullable = false)
    private boolean autoRenewal;

    @Column(name = "mercado_pago_preapproval_id", length = 100)
    private String mercadoPagoPreapprovalId;

    @Column(name = "tax_id", length = 30)
    private String taxId;

    @Column(name = "store_id")
    private java.util.UUID storeId;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
