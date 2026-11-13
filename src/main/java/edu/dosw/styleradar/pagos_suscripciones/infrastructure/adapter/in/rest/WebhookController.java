package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.in.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dosw.styleradar.pagos_suscripciones.application.dto.request.MercadoPagoWebhookRequest;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.in.WebhookPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Locale;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Endpoints públicos para recibir notificaciones de pasarelas de pago")
public class WebhookController {

    private final WebhookPort webhookPort;
    private final ObjectMapper objectMapper;   // FIX: inyectado (antes new por request)

    @Value("${mercadopago.webhook-secret}")
    private String webhookSecret;

    @Operation(summary = "Recibir notificación de Mercado Pago")
    @PostMapping("/mercadopago")
    public ResponseEntity<Void> recibirWebhook(
            @RequestHeader(value = "x-signature", required = false) String xSignature,
            @RequestHeader(value = "x-request-id", required = false) String xRequestId,
            @RequestBody MercadoPagoWebhookRequest payload) {

        String idExterno = payload.getData() != null ? payload.getData().getId() : null;

        if (!validarFirma(xSignature, xRequestId, idExterno)) {
            log.warn("Webhook con firma inválida: x-signature={}, x-request-id={}, data.id={}",
                    xSignature, xRequestId, idExterno);
            return ResponseEntity.status(401).build();
        }

        log.info("Webhook Recibido -> Tipo: {}, Acción: {}, ID: {}",
                payload.getType(), payload.getAction(), idExterno);

        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            webhookPort.procesarNotificacionPago(idExterno, payload.getAction(), payload.getType(), payloadJson);
        } catch (Exception e) {
            log.error("ERROR PROCESANDO WEBHOOK para ID {}: {}", idExterno, e.getMessage(), e);
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Manifest MP: `id:{data.id};request-id:{x-request-id};ts:{ts};`
     * - data.id en minúsculas (spec MP para IDs alfanuméricos).
     * - x-request-id ausente → "" (el simulador de MP a veces lo omite).
     * - Parseo por clave (no por posición) → tolera reordenamientos.
     * - Comparación con MessageDigest.isEqual → constant-time (mitiga timing attacks).
     */
    private boolean validarFirma(String xSignature, String xRequestId, String dataId) {
        if (xSignature == null || xSignature.isBlank() || dataId == null || dataId.isBlank()) {
            return false;
        }
        try {
            String ts = null;
            String hashRecibido = null;
            for (String part : xSignature.split(",")) {
                int idx = part.indexOf('=');
                if (idx <= 0) continue;
                String k = part.substring(0, idx).trim();
                String v = part.substring(idx + 1).trim();
                if ("ts".equals(k)) ts = v;
                else if ("v1".equals(k)) hashRecibido = v;
            }
            if (ts == null || hashRecibido == null) {
                log.warn("Firma malformada: falta ts o v1 en '{}'", xSignature);
                return false;
            }

            String idManifest = dataId.toLowerCase(Locale.ROOT);
            String reqIdManifest = xRequestId != null ? xRequestId : "";
            String manifest = "id:" + idManifest + ";request-id:" + reqIdManifest + ";ts:" + ts + ";";

            Mac sha256HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256HMAC.init(secretKey);
            String hashCalculado = Base64.getEncoder()
                    .encodeToString(sha256HMAC.doFinal(manifest.getBytes(StandardCharsets.UTF_8)));

            return MessageDigest.isEqual(
                    hashCalculado.getBytes(StandardCharsets.UTF_8),
                    hashRecibido.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Error al validar la firma HMAC", e);
            return false;
        }
    }
}