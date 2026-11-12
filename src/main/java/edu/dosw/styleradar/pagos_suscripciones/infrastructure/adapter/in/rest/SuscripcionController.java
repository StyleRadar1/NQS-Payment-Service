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

    @Operation(summary = "Iniciar suscripción")
    @PostMapping("/iniciar")
    public ResponseEntity<String> iniciar(@RequestBody CrearSuscripcionRequest req) {
        return ResponseEntity.ok(suscripcionPort.iniciarSuscripcion(
            req.suscriptorId(), req.planId(),
            TipoSuscriptor.valueOf(req.tipoSuscriptor().toUpperCase()),
            req.emailPayer()));
    }

    @GetMapping("/{suscriptorId}")
    public ResponseEntity<Suscripcion> consultar(@PathVariable String suscriptorId) {
        return ResponseEntity.ok(suscripcionPort.consultarSuscripcion(suscriptorId));
    }

    @PostMapping("/{suscriptorId}/cancelar")
    public ResponseEntity<Void> cancelar(@PathVariable String suscriptorId) {
        suscripcionPort.cancelarRenovacion(suscriptorId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Cambiar de plan (upgrade/downgrade)",
               description = "Upgrade devuelve init_point. Downgrade se aplica en renovación.")
    @PutMapping("/{suscriptorId}/cambiar-plan")
    public ResponseEntity<Map<String, String>> cambiarPlan(
            @PathVariable String suscriptorId,
            @RequestParam Long nuevoPlanId,
            @RequestParam String emailPayer) {
        String initPoint = suscripcionPort.programarCambioPlan(suscriptorId, nuevoPlanId, emailPayer);
        if (initPoint != null) {
            return ResponseEntity.ok(Map.of(
                "tipo", "UPGRADE",
                "init_point", initPoint,
                "mensaje", "Pague la diferencia para activar el nuevo plan inmediatamente."));
        }
        return ResponseEntity.ok(Map.of(
            "tipo", "DOWNGRADE",
            "mensaje", "El cambio se aplicará en la próxima renovación."));
    }

    public record CrearSuscripcionRequest(
            @Schema(example = "USER-123") String suscriptorId,
            @Schema(example = "2") Long planId,
            @Schema(example = "USUARIO") String tipoSuscriptor,
            @Schema(example = "test@test.com") String emailPayer) {}
}