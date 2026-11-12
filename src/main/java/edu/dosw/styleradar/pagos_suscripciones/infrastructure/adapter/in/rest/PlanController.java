package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.Plan;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.PlanRepositoryPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/planes")
@RequiredArgsConstructor
@Tag(name = "Planes", description = "Catálogo de planes disponibles")
public class PlanController {

    private final PlanRepositoryPort planRepository;

    @Operation(summary = "Listar planes activos con sus beneficios (SR-RF-100)")
    @GetMapping
    public ResponseEntity<List<Plan>> listar() {
        return ResponseEntity.ok(planRepository.listarActivos());
    }
}