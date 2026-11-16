package edu.dosw.styleradar.pagos.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.pagos.application.dto.response.PagoResponse;
import edu.dosw.styleradar.pagos.application.port.in.AdminPagoPort;
import edu.dosw.styleradar.pagos.domain.model.EstadoPago;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * SR-RF-75: listado global de pagos para el administrador de StyleRadar.
 * El rol ADMIN se exige en {@code SecurityConfig}.
 */
@RestController
@RequestMapping("/api/v1/admin/pagos")
@RequiredArgsConstructor
@Tag(name = "Administración", description = "Consulta global de pagos")
public class AdminPagoController {

    private final AdminPagoPort adminPagoPort;

    @GetMapping
    @Operation(summary = "Lista todos los pagos, con filtro opcional por estado")
    public List<PagoResponse> listar(
            @RequestParam(value = "estado", required = false) EstadoPago estado) {
        return adminPagoPort.listar(estado);
    }
}
