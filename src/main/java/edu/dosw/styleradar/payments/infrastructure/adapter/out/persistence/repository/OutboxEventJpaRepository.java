package edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.repository;

import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface OutboxEventJpaRepository extends JpaRepository<OutboxEventEntity, Long> {

    List<OutboxEventEntity> findTop50ByStatusInAndNextAttemptAtBeforeOrderByCreatedAtAsc(
            List<String> statuses, LocalDateTime now);

    boolean existsByEventId(UUID eventId);
}
