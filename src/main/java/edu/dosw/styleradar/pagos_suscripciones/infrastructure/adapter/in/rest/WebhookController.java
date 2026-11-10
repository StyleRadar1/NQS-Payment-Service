package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.pagos_suscripciones.application.dto.request.MercadoPagoWebhookRequest;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.in.WebhookPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Endpoints públicos para recibir notificaciones de pasarelas de pago")
public class WebhookController {

    private final WebhookPort webhookPort;

    @Operation(summary = "Recibir notificación de Mercado Pago", description = "Procesa los eventos enviados por Mercado Pago (pagos aprobados, rechazados, etc.)")
    @PostMapping("/mercadopago")
    public ResponseEntity<Void> recibirWebhook(
            @RequestHeader(value = "x-signature", required = false, defaultValue = "dummy") String xSignature,
            @RequestBody MercadoPagoWebhookRequest payload) {

        // Aquí iría tu validación HMAC (la puedes dejar comentada o simplificada para la demo)
        // if (!validarFirma(xSignature, payload)) return ResponseEntity.status(401).build();

        String idPagoExterno = payload.getData().getId();
        String estadoPago = payload.getAction();
        String tipoEvento = payload.getType();

        log.info("Webhook recibido para pago: {} con estado: {}", idPagoExterno, estadoPago);
        webhookPort.procesarNotificacionPago(idPagoExterno, estadoPago, tipoEvento);
        
        return ResponseEntity.ok().build();
    }
}