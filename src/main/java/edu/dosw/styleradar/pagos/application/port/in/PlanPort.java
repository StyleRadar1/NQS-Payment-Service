package edu.dosw.styleradar.pagos.application.port.in;

import edu.dosw.styleradar.pagos.application.dto.response.PlanResponse;

import java.util.List;

/** SR-RF-100/102: catálogo de planes y beneficios. */
public interface PlanPort {

    List<PlanResponse> listar();
}
