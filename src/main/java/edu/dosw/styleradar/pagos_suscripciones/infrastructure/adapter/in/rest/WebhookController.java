package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.pagos_suscripciones.domain.port.in.WebhookPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Tag(name = "Webhooks", description = "Endpoints públicos para recibir notificaciones de pasarelas de pago")
public class WebhookController {

    private final WebhookPort webhookPort;

    @Operation(summary = "Recibir notificación de Mercado Pago", description = "Procesa los eventos enviados por Mercado Pago (pagos aprobados, rechazados, etc.)")
    @PostMapping("/mercadopago")
    public ResponseEntity<Void> recibirWebhook(
            @RequestHeader(value = "x-signature", required = false) String firmaHmac,
            @RequestBody Map<String, Object> payload) {
        
                // Aquí deberías validar la firma HMAC usando la clave secreta de Mercado Pago.
                // Por simplicidad, extraemos el ID y estado del payload.
        String idPagoExterno = (String) payload.get("data.id");
        String estadoPago = (String) payload.getOrDefault("action", "unknown"); 
        String tipoEvento = (String) payload.getOrDefault("type", "payment");

        webhookPort.procesarNotificacionPago(idPagoExterno, estadoPago, tipoEvento);
        
        return ResponseEntity.ok().build();
    }
}