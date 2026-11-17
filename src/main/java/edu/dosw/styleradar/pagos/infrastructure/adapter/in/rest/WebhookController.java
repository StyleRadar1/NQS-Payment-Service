package edu.dosw.styleradar.pagos.infrastructure.adapter.in.rest;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dosw.styleradar.pagos.application.dto.request.NotificacionPago;
import edu.dosw.styleradar.pagos.application.port.in.WebhookPort;
import edu.dosw.styleradar.pagos.domain.exception.SolicitudInvalidaException;
import edu.dosw.styleradar.pagos.infrastructure.adapter.in.rest.security.MercadoPagoSignatureValidator;
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

/**
 * Webhook de Mercado Pago.
 *
 * <p>SR-RF-94: recibe la notificación, valida la firma HMAC (SR-RNF-13),
 * consulta el estado real y actualiza pago/suscripción. Responde 200 a
 * Mercado Pago para detener reintentos; los duplicados se resuelven por
 * idempotencia (SR-RNF-14).</p>
 */
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Notificaciones de Mercado Pago")
public class WebhookController {

    private final WebhookPort webhookPort;
    private final MercadoPagoSignatureValidator signatureValidator;
    private final ObjectMapper objectMapper;

    @PostMapping("/mercadopago")
    @Operation(summary = "Recibe y procesa una notificación de Mercado Pago")
    public ResponseEntity<Map<String, String>> recibir(
            @RequestHeader(value = "x-signature", required = false) String xSignature,
            @RequestHeader(value = "x-request-id", required = false) String requestId,
            @RequestParam(value = "type", required = false) String typeQuery,
            @RequestParam(value = "topic", required = false) String topicQuery,
            @RequestParam(value = "data.id", required = false) String dataIdQuery,
            @RequestBody(required = false) JsonNode body) {

        String tipo = primeroNoVacio(typeQuery, topicQuery, texto(body, "type"), texto(body, "topic"));
        String dataId = primeroNoVacio(dataIdQuery, texto(body, "data", "id"));

        if (!signatureValidator.validar(xSignature, requestId, dataId)) {
            throw new SolicitudInvalidaException("La firma del webhook es inválida.");
        }

        NotificacionPago notificacion = new NotificacionPago(dataId, tipo, requestId, payload(body));
        webhookPort.procesar(notificacion, requestId);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    private Map<String, Object> payload(JsonNode body) {
        if (body == null || body.isNull()) {
            return Map.of();
        }
        return objectMapper.convertValue(body, new TypeReference<Map<String, Object>>() {
        });
    }

    private static String texto(JsonNode body, String... ruta) {
        if (body == null || body.isNull()) {
            return null;
        }
        JsonNode actual = body;
        for (String campo : ruta) {
            if (actual == null || !actual.has(campo)) {
                return null;
            }
            actual = actual.get(campo);
        }
        return actual == null || actual.isNull() ? null : actual.asText();
    }

    private static String primeroNoVacio(String... valores) {
        for (String valor : valores) {
            if (valor != null && !valor.isBlank()) {
                return valor.trim();
            }
        }
        return null;
    }
}
