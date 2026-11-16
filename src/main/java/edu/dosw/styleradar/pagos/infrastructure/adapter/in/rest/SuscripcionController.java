package edu.dosw.styleradar.pagos.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.pagos.application.dto.request.CancelarSuscripcionRequest;
import edu.dosw.styleradar.pagos.application.dto.request.IniciarSuscripcionRequest;
import edu.dosw.styleradar.pagos.application.dto.response.CancelacionResponse;
import edu.dosw.styleradar.pagos.application.dto.response.IniciarSuscripcionResponse;
import edu.dosw.styleradar.pagos.application.dto.response.SuscripcionResponse;
import edu.dosw.styleradar.pagos.application.port.in.SuscripcionPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints de suscripciones.
 *
 * <p>SR-RF-67/93/100: iniciar el pago. SR-RF-67/102: consultar estado,
 * vigencia y plan. SR-RF-68/101: cancelar la renovación. El cambio de plan
 * quedó fuera de alcance (decisión D3).</p>
 */
@RestController
@RequestMapping("/api/v1/suscripciones")
@RequiredArgsConstructor
@Tag(name = "Suscripciones", description = "Pago, consulta y cancelación de suscripciones")
public class SuscripcionController {

    private final SuscripcionPort suscripcionPort;

    @PostMapping("/iniciar")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Inicia el pago de una suscripción y devuelve el enlace de Mercado Pago")
    public IniciarSuscripcionResponse iniciar(
            @RequestHeader("X-User-Id") String suscriptorId,
            @Valid @RequestBody IniciarSuscripcionRequest request) {
        return suscripcionPort.iniciar(suscriptorId, request);
    }

    @GetMapping("/{suscriptorId}")
    @Operation(summary = "Consulta el estado, la vigencia y el plan de una suscripción")
    public SuscripcionResponse consultar(
            @PathVariable String suscriptorId,
            @RequestHeader("X-User-Id") String solicitanteId,
            @RequestHeader(value = "X-User-Role", required = false) String rol) {
        return suscripcionPort.consultar(suscriptorId, solicitanteId, rol);
    }

    @PostMapping("/{suscriptorId}/cancelar")
    @Operation(summary = "Cancela la renovación automática y conserva el periodo ya pagado")
    public CancelacionResponse cancelar(
            @PathVariable String suscriptorId,
            @RequestHeader("X-User-Id") String solicitanteId,
            @RequestHeader(value = "X-User-Role", required = false) String rol,
            @Valid @RequestBody CancelarSuscripcionRequest request) {
        return suscripcionPort.cancelar(suscriptorId, request.confirmacion(), solicitanteId, rol);
    }
}
