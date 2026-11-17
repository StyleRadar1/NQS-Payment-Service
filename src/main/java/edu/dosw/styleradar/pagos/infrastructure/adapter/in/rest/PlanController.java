package edu.dosw.styleradar.pagos.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.pagos.application.dto.response.PlanResponse;
import edu.dosw.styleradar.pagos.application.port.in.PlanPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * SR-RF-100/102: catálogo de planes con precio y límites.
 *
 * <p>Se justifica porque el usuario necesita conocer Radar+ antes de pagar y
 * los módulos de beneficios necesitan los límites del plan. No es un
 * comparador ni permite contratar planes distintos: el almacén tiene uno solo
 * y el usuario solo puede activar Radar+.</p>
 */
@RestController
@RequestMapping("/api/v1/planes")
@RequiredArgsConstructor
@Tag(name = "Planes", description = "Catálogo de planes y beneficios")
public class PlanController {

    private final PlanPort planPort;

    @GetMapping
    @Operation(summary = "Lista los planes activos con su precio y límites")
    public List<PlanResponse> listar() {
        return planPort.listar();
    }
}
