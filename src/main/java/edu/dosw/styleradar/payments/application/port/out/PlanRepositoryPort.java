package edu.dosw.styleradar.payments.application.port.out;

import edu.dosw.styleradar.payments.domain.model.Plan;
import edu.dosw.styleradar.payments.domain.model.SubscriberType;

import java.util.List;
import java.util.Optional;

public interface PlanRepositoryPort {

    Optional<Plan> findById(Long id);

    List<Plan> listActive();

    
    Optional<Plan> findPaidPlanByType(SubscriberType subscriberType);
}
