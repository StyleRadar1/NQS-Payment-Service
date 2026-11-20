package edu.dosw.styleradar.payments.application.usecase;

import edu.dosw.styleradar.payments.application.dto.response.PlanResponse;
import edu.dosw.styleradar.payments.application.port.in.PlanPort;
import edu.dosw.styleradar.payments.application.port.out.PlanRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PlanUseCase implements PlanPort {

    private final PlanRepositoryPort planRepository;

    public PlanUseCase(PlanRepositoryPort planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanResponse> list() {
        return planRepository.listActive().stream()
                .map(PlanResponse::from)
                .toList();
    }
}
