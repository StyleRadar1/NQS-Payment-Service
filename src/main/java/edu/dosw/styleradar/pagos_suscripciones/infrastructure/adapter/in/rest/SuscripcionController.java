package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.Suscripcion;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.TipoSuscriptor;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.in.SuscripcionPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/suscripciones")
@RequiredArgsConstructor
@Tag(name = "Suscripciones", description = "Endpoints para la gestión de suscripciones (Almacenes y Usuarios)")
public class SuscripcionController {

    private final SuscripcionPort suscripcionPort;

    @Operation(summary = "Iniciar una nueva suscripción", description = "Crea una orden de pago y retorna el link de pago de Mercado Pago (init_point).")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Link de pago generado exitosamente", 
                     content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
        @ApiResponse(responseCode = "404", description = "El plan solicitado no existe"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos")
    })
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

    @Operation(summary = "Consultar suscripción por suscriptor", description = "Obtiene los detalles de la suscripción de un usuario o almacén.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Suscripción encontrada"),
        @ApiResponse(responseCode = "404", description = "No se encontró suscripción para el suscriptor")
    })
    @GetMapping("/{suscriptorId}")
    public ResponseEntity<Suscripcion> consultarSuscripcion(
            @Parameter(description = "ID del usuario o NIT del almacén", example = "USER-123") 
            @PathVariable String suscriptorId) {
        return ResponseEntity.ok(suscripcionPort.consultarSuscripcion(suscriptorId));
    }

    @Operation(summary = "Cancelar renovación automática", description = "Detiene el cobro automático en Mercado Pago. Los beneficios se mantienen hasta el final del periodo pagado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Renovación cancelada exitosamente"),
        @ApiResponse(responseCode = "404", description = "No se encontró suscripción activa")
    })
    @PostMapping("/{suscriptorId}/cancelar")
    public ResponseEntity<Void> cancelarRenovacion(
            @Parameter(description = "ID del usuario o NIT del almacén", example = "USER-123") 
            @PathVariable String suscriptorId) {
        suscripcionPort.cancelarRenovacion(suscriptorId);
        return ResponseEntity.ok().build();
    }

    public record CrearSuscripcionRequest(
            @Schema(description = "ID del usuario o NIT del almacén", example = "USER-123") String suscriptorId, 
            @Schema(description = "ID del plan a suscribir", example = "2") Long planId, 
            @Schema(description = "Tipo de suscriptor", example = "USUARIO", allowableValues = {"USUARIO", "ALMACEN"}) String tipoSuscriptor, 
            @Schema(description = "Correo del pagador para Mercado Pago", example = "test_user@test.com") String emailPayer
    ) {}

    @Operation(summary = "Programar cambio de plan")
    @PutMapping("/{suscriptorId}/cambiar-plan")
    public ResponseEntity<Void> cambiarPlan(
            @PathVariable String suscriptorId,
            @RequestParam Long nuevoPlanId) {
        suscripcionPort.programarCambioPlan(suscriptorId, nuevoPlanId);
        return ResponseEntity.ok().build();
    }
}