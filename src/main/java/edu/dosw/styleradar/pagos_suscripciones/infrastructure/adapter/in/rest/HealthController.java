package edu.dosw.styleradar.pagos_suscripciones.infrastructure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health Check", description = "Endpoints para verificar el estado del microservicio")
public class HealthController {

    @GetMapping
    @Operation(summary = "Verificar estado", description = "Retorna OK si el servicio está funcionando correctamente")
    public ResponseEntity<Map<String, Object>> checkHealth() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "ms-pagos-suscripciones",
            "timestamp", LocalDateTime.now().toString(),
            "version", "1.0.0"
        ));
    }
}