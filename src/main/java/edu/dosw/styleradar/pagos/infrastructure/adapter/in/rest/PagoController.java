package edu.dosw.styleradar.pagos.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.pagos.application.dto.request.ReembolsarPagoRequest;
import edu.dosw.styleradar.pagos.application.dto.request.ReintentarPagoRequest;
import edu.dosw.styleradar.pagos.application.dto.response.PagoResponse;
import edu.dosw.styleradar.pagos.application.dto.response.ReembolsoResponse;
import edu.dosw.styleradar.pagos.application.dto.response.ReintentoResponse;
import edu.dosw.styleradar.pagos.application.port.in.PagoPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Endpoints de pagos del actor.
 *
 * <p>SR-RF-97: historial propio. SR-RF-96: reintento de un pago rechazado.
 * SR-RF-98: reembolso total o parcial por parte del administrador.</p>
 */
@RestController
@RequestMapping("/api/v1/pagos")
@RequiredArgsConstructor
@Tag(name = "Pagos", description = "Historial, reintentos y reembolsos")
public class PagoController {

    private final PagoPort pagoPort;

    @GetMapping("/historial")
    @Operation(summary = "Consulta el historial de pagos del usuario autenticado")
    public List<PagoResponse> historial(@RequestHeader("X-User-Id") String suscriptorId) {
        return pagoPort.historial(suscriptorId);
    }

    @PostMapping("/{ordenId}/reintentar")
    @Operation(summary = "Genera un nuevo intento de pago para una orden rechazada")
    public ReintentoResponse reintentar(
            @PathVariable Long ordenId,
            @RequestHeader("X-User-Id") String solicitanteId,
            @Valid @RequestBody ReintentarPagoRequest request) {
        return pagoPort.reintentar(ordenId, request.emailPayer(), solicitanteId);
    }

    @PostMapping("/{ordenId}/reembolsar")
    @Operation(summary = "Solicita un reembolso total o parcial de un pago aprobado")
    public ReembolsoResponse reembolsar(
            @PathVariable Long ordenId,
            @RequestHeader(value = "X-User-Role", required = false) String rol,
            @Valid @RequestBody ReembolsarPagoRequest request) {
        return pagoPort.reembolsar(ordenId, request.monto(), request.motivo(), rol);
    }
}
