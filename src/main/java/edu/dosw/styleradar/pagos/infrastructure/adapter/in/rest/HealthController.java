package edu.dosw.styleradar.pagos.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/** Health check propio, además de {@code /actuator/health}. */
@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Salud", description = "Estado del microservicio")
public class HealthController {

    @GetMapping
    @Operation(summary = "Verifica que el microservicio esté arriba")
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "service", "ms-pagos-suscripciones",
                "timestamp", Instant.now().toString());
    }
}
