package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.in.rest;

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
import java.util.Base64;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Endpoints públicos para recibir notificaciones de pasarelas de pago")
public class WebhookController {

    private final WebhookPort webhookPort;

    @Value("${mercadopago.webhook-secret:secret-key-12345}")
    private String webhookSecret;

    @Operation(summary = "Recibir notificación de Mercado Pago")
    @PostMapping("/mercadopago")
    public ResponseEntity<Void> recibirWebhook(
            @RequestHeader(value = "x-signature", required = false, defaultValue = "dummy") String xSignature,
            @RequestHeader(value = "x-request-id", required = false, defaultValue = "dummy") String xRequestId,
            @RequestBody MercadoPagoWebhookRequest payload) {

        String idExterno = payload.getData() != null ? payload.getData().getId() : null;

        // 1. Validar la firma HMAC (Solo si no es "dummy", para permitir pruebas desde Swagger)
        if (!"dummy".equals(xSignature) && !validarFirma(xSignature, xRequestId, idExterno)) {
            log.warn("Intento de webhook con firma inválida o no autorizada.");
            return ResponseEntity.status(401).build();
        }

        log.info("Webhook recibido. ID: {}, Estado: {}, Tipo: {}", idExterno, payload.getAction(), payload.getType());
        webhookPort.procesarNotificacionPago(idExterno, payload.getAction(), payload.getType());
        
        return ResponseEntity.ok().build();
    }

    /**
     * Valida la firma HMAC enviada por Mercado Pago en el header x-signature.
     */
    private boolean validarFirma(String xSignature, String xRequestId, String dataId) {
        try {
            // El formato de x-signature es: ts=timestamp,v1=hash
            String[] parts = xSignature.split(",");
            String ts = parts[0].split("=")[1];
            String hashRecibido = parts[1].split("=")[1];

            // Construir el manifest exacto que espera Mercado Pago
            String manifest = "id:" + dataId + ";request-id:" + xRequestId + ";ts:" + ts + ";";

            // Calcular el HMAC usando la clave secreta
            Mac sha256HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256HMAC.init(secretKey);
            String hashCalculado = Base64.getEncoder().encodeToString(sha256HMAC.doFinal(manifest.getBytes(StandardCharsets.UTF_8)));

            // Comparar el hash calculado con el recibido
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