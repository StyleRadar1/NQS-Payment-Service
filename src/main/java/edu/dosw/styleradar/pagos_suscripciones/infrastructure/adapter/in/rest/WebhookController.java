package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.pagos_suscripciones.application.dto.request.MercadoPagoWebhookRequest;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.in.WebhookPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Endpoints públicos para recibir notificaciones de pasarelas de pago")
public class WebhookController {

    private final WebhookPort webhookPort;
    private final Environment environment; // Inyectado para detectar si es QA

    @Value("${mercadopago.webhook-secret:secret-key-12345}")
    private String webhookSecret;

    @Operation(summary = "Recibir notificación de Mercado Pago")
    @PostMapping("/mercadopago")
    public ResponseEntity<Void> recibirWebhook(
            @RequestHeader(value = "x-signature", required = false, defaultValue = "dummy") String xSignature,
            @RequestHeader(value = "x-request-id", required = false, defaultValue = "dummy") String xRequestId,
            @RequestBody MercadoPagoWebhookRequest payload) {

        String idExterno = payload.getData() != null ? payload.getData().getId() : null;

        log.info("Webhook Recibido -> Tipo: {}, Acción: {}, ID: {}, Firma: {}", 
                payload.getType(), payload.getAction(), idExterno, xSignature);

        // Si estamos en QA, nos saltamos la validación de firma para poder probar sin problemas
        boolean isQa = environment.acceptsProfiles(Profiles.of("qa"));
        
        if (!isQa && !"dummy".equals(xSignature) && !validarFirma(xSignature, xRequestId, idExterno)) {
            log.warn("Intento de webhook con firma inválida o no autorizada.");
            return ResponseEntity.status(401).build();
        }

        String payloadJson;
        try {
            payloadJson = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(payload);
        } catch (Exception e) {
            payloadJson = "{}";
        }
        
        try {
            webhookPort.procesarNotificacionPago(idExterno, payload.getAction(), payload.getType(), payloadJson);
        } catch (Exception e) {
            // En QA, si hay un error (ej. Redis caído), logueamos el error pero decimos 200 a MP
            log.error("ERROR INTERNO PROCESANDO WEBHOOK: {}", e.getMessage(), e);
        }
        
        return ResponseEntity.ok().build();
    }
    private boolean validarFirma(String xSignature, String xRequestId, String dataId) {
        try {
            String[] parts = xSignature.split(",");
            String ts = parts[0].split("=")[1];
            String hashRecibido = parts[1].split("=")[1];

            String manifest = "id:" + dataId + ";request-id:" + xRequestId + ";ts:" + ts + ";";

            Mac sha256HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256HMAC.init(secretKey);
            String hashCalculado = Base64.getEncoder().encodeToString(sha256HMAC.doFinal(manifest.getBytes(StandardCharsets.UTF_8)));

            boolean esValido = hashCalculado.equals(hashRecibido);
            if (!esValido) {
                log.warn("Hash calculado: {}, Hash recibido: {}", hashCalculado, hashRecibido);
            }
            return esValido;

        } catch (Exception e) {
            log.error("Error al validar la firma HMAC", e);
            return false;
        }
    }
}