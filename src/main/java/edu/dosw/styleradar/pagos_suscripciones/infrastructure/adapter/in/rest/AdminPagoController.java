package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.EstadoPago;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.in.AdminPagoPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/pagos")
@RequiredArgsConstructor
@Tag(name = "Admin - Pagos")
public class AdminPagoController {

    private final AdminPagoPort adminPagoPort;

    @Operation(summary = "Consultar todos los pagos (ADMIN)")
    @GetMapping
    public ResponseEntity<List<OrdenPago>> listar(
            @RequestHeader(value = "X-User-Role", defaultValue = "USER") String rol,
            @RequestParam(required = false) EstadoPago estado) {
        if (!"ADMIN".equalsIgnoreCase(rol)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(adminPagoPort.consultarPagos(estado));
    }
}
