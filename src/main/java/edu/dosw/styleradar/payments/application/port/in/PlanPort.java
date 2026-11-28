package edu.dosw.styleradar.payments.application.port.in;

import edu.dosw.styleradar.payments.application.dto.response.PlanResponse;

import java.util.List;

public interface PlanPort {

    List<PlanResponse> list();
}
