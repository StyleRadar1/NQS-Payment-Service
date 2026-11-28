package edu.dosw.styleradar.payments.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.payments.application.dto.response.PlanResponse;
import edu.dosw.styleradar.payments.application.port.in.PlanPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
@Tag(name = "Planes", description = "Plan catalog and benefits")
public class PlanController {

    private final PlanPort planPort;

    @GetMapping
    @Operation(summary = "List active plans with their price and limits")
    public List<PlanResponse> list() {
        return planPort.list();
    }
}

