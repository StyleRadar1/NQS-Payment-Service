package edu.dosw.styleradar.payments.infrastructure.adapter.in.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Health", description = "Microservice status")
public class HealthController {

    @GetMapping
    @Operation(summary = "Check that the microservice is up")
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "service", "ms-payments-subscriptions",
                "timestamp", Instant.now().toString());
    }
}
