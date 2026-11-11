package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.in.PagoPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pagos")
@RequiredArgsConstructor
@Tag(name = "Pagos", description = "Endpoints para consulta y gestión de pagos")
public class PagoController {

    private final PagoPort pagoPort;

    @Operation(summary = "Consultar historial de pagos")
    @GetMapping("/historial/{suscriptorId}")
    public ResponseEntity<List<OrdenPago>> consultarHistorial(@PathVariable String suscriptorId) {
        return ResponseEntity.ok(pagoPort.consultarHistorialPagos(suscriptorId));
    }

    @Operation(summary = "Reembolsar un pago")
    @PostMapping("/{ordenId}/reembolsar")
    public ResponseEntity<Void> reembolsar(
            @PathVariable Long ordenId,
            @RequestParam String monto,
            @RequestParam String motivo) {
        pagoPort.reembolsarPago(ordenId, monto, motivo);
        return ResponseEntity.ok().build();
    }
}