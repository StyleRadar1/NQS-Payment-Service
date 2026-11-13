package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.Suscripcion;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.TipoSuscriptor;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.in.SuscripcionPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/suscripciones")
@RequiredArgsConstructor
@Tag(name = "Suscripciones")
public class SuscripcionController {

    private final SuscripcionPort suscripcionPort;

    @Operation(summary = "Iniciar suscripción",
               description = "El suscriptorId se toma del header X-User-Id (inyectado por el Gateway tras validar JWT).")
    @PostMapping("/iniciar")
    public ResponseEntity<String> iniciar(
            @RequestHeader("X-User-Id") String suscriptorId,
            @RequestBody CrearSuscripcionRequest req) {
        return ResponseEntity.ok(suscripcionPort.iniciarSuscripcion(
                suscriptorId, req.planId(),
                TipoSuscriptor.valueOf(req.tipoSuscriptor().toUpperCase()),
                req.emailPayer()));
    }

    @GetMapping("/{suscriptorId}")
    public ResponseEntity<Suscripcion> consultar(
            @PathVariable String suscriptorId,
            @RequestHeader("X-User-Id") String headerId) {
        if (!suscriptorId.equals(headerId)) return ResponseEntity.status(403).build();
        return ResponseEntity.ok(suscripcionPort.consultarSuscripcion(suscriptorId));
    }

    @PostMapping("/{suscriptorId}/cancelar")
    public ResponseEntity<Void> cancelar(
            @PathVariable String suscriptorId,
            @RequestHeader("X-User-Id") String headerId) {
        if (!suscriptorId.equals(headerId)) return ResponseEntity.status(403).build();
        suscripcionPort.cancelarRenovacion(suscriptorId);
        return ResponseEntity.ok().build();
    }

    @Deprecated(since = "2026-11-11")
    @Operation(summary = "[DEPRECADO · SR-RF-101] Cambiar de plan (fuera de alcance vigente)",
               deprecated = true)
    @PutMapping("/{suscriptorId}/cambiar-plan")
    public ResponseEntity<Map<String, String>> cambiarPlan(
            @PathVariable String suscriptorId,
            @RequestHeader("X-User-Id") String headerId,
            @RequestParam Long nuevoPlanId,
            @RequestParam String emailPayer) {
        if (!suscriptorId.equals(headerId)) return ResponseEntity.status(403).build();
        String initPoint = suscripcionPort.programarCambioPlan(suscriptorId, nuevoPlanId, emailPayer);
        if (initPoint != null) {
            return ResponseEntity.ok(Map.of(
                "tipo", "UPGRADE", "init_point", initPoint,
                "mensaje", "Pague la diferencia para activar el nuevo plan inmediatamente."));
        }
        return ResponseEntity.ok(Map.of(
            "tipo", "DOWNGRADE",
            "mensaje", "El cambio se aplicará en la próxima renovación."));
    }

    /** FIX: suscriptorId removido — viene por header X-User-Id. */
    public record CrearSuscripcionRequest(
            @Schema(example = "2") Long planId,
            @Schema(example = "USUARIO") String tipoSuscriptor,
            @Schema(example = "test@test.com") String emailPayer) {}
}