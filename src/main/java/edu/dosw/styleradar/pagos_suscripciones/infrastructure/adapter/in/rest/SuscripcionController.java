package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.Suscripcion;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.TipoSuscriptor;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.in.SuscripcionPort;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/suscripciones")
@RequiredArgsConstructor
public class SuscripcionController {

    private final SuscripcionPort suscripcionPort;

    @PostMapping("/iniciar")
    public ResponseEntity<String> iniciarSuscripcion(@RequestBody CrearSuscripcionRequest request) {
        String initPoint = suscripcionPort.iniciarSuscripcion(
                request.suscriptorId(),
                request.planId(),
                TipoSuscriptor.valueOf(request.tipoSuscriptor().toUpperCase()),
                request.emailPayer()
        );
        return ResponseEntity.ok(initPoint);
    }

    @GetMapping("/{suscriptorId}")
    public ResponseEntity<Suscripcion> consultarSuscripcion(@PathVariable String suscriptorId) {
        return ResponseEntity.ok(suscripcionPort.consultarSuscripcion(suscriptorId));
    }

    @PostMapping("/{suscriptorId}/cancelar")
    public ResponseEntity<Void> cancelarRenovacion(@PathVariable String suscriptorId) {
        suscripcionPort.cancelarRenovacion(suscriptorId);
        return ResponseEntity.ok().build();
    }

    public record CrearSuscripcionRequest(String suscriptorId, Long planId, String tipoSuscriptor, String emailPayer) {}
}