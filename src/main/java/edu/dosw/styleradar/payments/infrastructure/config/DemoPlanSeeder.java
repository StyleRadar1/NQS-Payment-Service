package edu.dosw.styleradar.payments.infrastructure.config;

import edu.dosw.styleradar.payments.domain.model.SubscriberType;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.entity.PlanEntity;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.repository.PlanJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Seeds the three official plans for local demos (dev/test only).
 * Production data comes from Flyway.
 */
@Component
@Profile({"dev", "test"})
@RequiredArgsConstructor
public class DemoPlanSeeder implements CommandLineRunner {

    private final PlanJpaRepository planRepository;

    @Override
    public void run(String... args) {
        if (planRepository.count() > 0) {
            return;
        }
        planRepository.save(PlanEntity.builder()
                .id(1L)
                .name("Radar")
                .subscriberType(SubscriberType.USER)
                .price(BigDecimal.ZERO)
                .currency("COP")
                .limits(Map.of("max_wishlist", 20, "try_on_photo", true,
                        "try_on_uses_per_month", 10, "availability_alerts", false))
                .active(true)
                .createdAt(LocalDateTime.now())
                .build());
        planRepository.save(PlanEntity.builder()
                .id(2L)
                .name("Radar+")
                .subscriberType(SubscriberType.USER)
                .price(new BigDecimal("14900.00"))
                .currency("COP")
                .limits(Map.of("max_wishlist", -1, "try_on_photo", true,
                        "try_on_uses_per_month", 50, "availability_alerts", true))
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
}
