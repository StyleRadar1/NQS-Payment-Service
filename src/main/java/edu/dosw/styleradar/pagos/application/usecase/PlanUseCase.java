package edu.dosw.styleradar.pagos.application.usecase;

import edu.dosw.styleradar.pagos.application.dto.response.PlanResponse;
import edu.dosw.styleradar.pagos.application.port.in.PlanPort;
import edu.dosw.styleradar.pagos.application.port.out.PlanRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * SR-RF-100 y SR-RF-102: catálogo de planes con precio y límites.
 *
 * <p>Se expone un endpoint de solo lectura para que el frontend muestre
 * Radar+ antes del pago y para que los módulos de beneficios conozcan los
 * límites del plan. No permite comparar ni seleccionar planes de pago:
 * el almacén tiene uno solo y el usuario solo puede contratar Radar+.</p>
 */
@Service
public class PlanUseCase implements PlanPort {

    private final PlanRepositoryPort planRepository;

    public PlanUseCase(PlanRepositoryPort planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanResponse> listar() {
        return planRepository.listarActivos().stream()
                .map(PlanResponse::desde)
                .toList();
    }
}
