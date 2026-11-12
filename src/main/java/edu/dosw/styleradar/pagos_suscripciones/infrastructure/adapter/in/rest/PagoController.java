package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.in.PagoPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/pagos")
@RequiredArgsConstructor
@Tag(name = "Pagos")
public class PagoController {

    private final PagoPort pagoPort;

    @Operation(summary = "Historial de pagos propios",
               description = "El suscriptorId se toma del header X-User-Id (inyectado por el Gateway).")
    @GetMapping("/historial")
    public ResponseEntity<List<OrdenPago>> historial(
            @RequestHeader("X-User-Id") String suscriptorId) {
        return ResponseEntity.ok(pagoPort.consultarHistorialPagos(suscriptorId));
    }

    @Operation(summary = "Reembolsar pago (solo ADMIN)")
    @PostMapping("/{ordenId}/reembolsar")
    public ResponseEntity<Void> reembolsar(
            @PathVariable Long ordenId,
            @RequestParam String monto,
            @RequestParam String motivo,
            @RequestHeader(value = "X-User-Role", defaultValue = "USER") String rol) {
        if (!"ADMIN".equalsIgnoreCase(rol)) {
            return ResponseEntity.status(403).build();
        }
        pagoPort.reembolsarPago(ordenId, monto, motivo);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Reintentar pago rechazado/pendiente")
    @PostMapping("/{ordenId}/reintentar")
    public ResponseEntity<Map<String, String>> reintentar(
            @PathVariable Long ordenId,
            @RequestHeader("X-User-Id") String suscriptorId,
            @RequestParam String emailPayer) {
        String initPoint = pagoPort.reintentarPago(ordenId, suscriptorId, emailPayer);
        return ResponseEntity.ok(Map.of("init_point", initPoint));
    }
}