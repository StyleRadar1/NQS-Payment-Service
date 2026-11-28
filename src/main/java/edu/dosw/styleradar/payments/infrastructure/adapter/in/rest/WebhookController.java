package edu.dosw.styleradar.payments.infrastructure.adapter.in.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dosw.styleradar.payments.application.dto.request.NotificationPayment;
import edu.dosw.styleradar.payments.application.port.in.WebhookPort;
import edu.dosw.styleradar.payments.domain.exception.UnauthorizedException;
import edu.dosw.styleradar.payments.infrastructure.adapter.in.rest.security.MercadoPagoSignatureValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Mercado Pago notifications")
public class WebhookController {

    private final WebhookPort webhookPort;
    private final MercadoPagoSignatureValidator signatureValidator;
    private final ObjectMapper objectMapper;

    @PostMapping("/mercadopago")
    @Operation(summary = "Receive and process a Mercado Pago notification")
    public ResponseEntity<Map<String, String>> receive(
            @RequestHeader(value = "x-signature", required = false) String xSignature,
            @RequestHeader(value = "x-request-id", required = false) String requestId,
            @RequestParam(value = "type", required = false) String typeQuery,
            @RequestParam(value = "topic", required = false) String topicQuery,
            @RequestParam(value = "data.id", required = false) String dataIdQuery,
            @RequestBody(required = false) JsonNode body) {

        String type = firstNonBlank(typeQuery, topicQuery, text(body, "type"), text(body, "topic"));
        String dataId = firstNonBlank(dataIdQuery, text(body, "data", "id"));

        if (!signatureValidator.validate(xSignature, requestId, dataId)) {
            throw new UnauthorizedException("La firma del webhook es inválida.");
        }

        NotificationPayment notification = new NotificationPayment(dataId, type, requestId, payload(body));
        webhookPort.process(notification, requestId);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    private Map<String, Object> payload(JsonNode body) {
        if (body == null || body.isNull()) {
            return Map.of();
        }
        return objectMapper.convertValue(body, new TypeReference<Map<String, Object>>() {
        });
    }

    private static String text(JsonNode body, String... path) {
        if (body == null || body.isNull()) {
            return null;
        }
        JsonNode current = body;
        for (String field : path) {
            if (current == null || !current.has(field)) {
                return null;
            }
            current = current.get(field);
        }
        return current == null || current.isNull() ? null : current.asText();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}

