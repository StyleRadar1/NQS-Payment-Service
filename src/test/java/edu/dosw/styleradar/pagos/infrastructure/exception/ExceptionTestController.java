package edu.dosw.styleradar.pagos.infrastructure.exception;

import edu.dosw.styleradar.pagos.application.dto.request.IniciarSuscripcionRequest;
import edu.dosw.styleradar.pagos.domain.exception.IdempotencyException;
import edu.dosw.styleradar.pagos.domain.exception.OperacionNoPermitidaException;
import edu.dosw.styleradar.pagos.domain.exception.PaymentProcessingException;
import edu.dosw.styleradar.pagos.domain.exception.SolicitudInvalidaException;
import edu.dosw.styleradar.pagos.domain.exception.SuscripcionNoEncontradaException;
import edu.dosw.styleradar.pagos.domain.model.EstadoPago;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Controlador de pruebas para verificar el mapeo de errores de ADR-10. */
@RestController
public class ExceptionTestController {

    @GetMapping("/test/400")
    void badRequest() {
        throw new SolicitudInvalidaException("Solicitud inválida");
    }

    @GetMapping("/test/403")
    void forbidden() {
        throw new OperacionNoPermitidaException("Sin permiso");
    }

    @GetMapping("/test/404")
    void notFound() {
        throw new SuscripcionNoEncontradaException();
    }

    @GetMapping("/test/409")
    void conflict() {
        throw new IdempotencyException();
    }

    @GetMapping("/test/422")
    void unprocessable() {
        throw new PaymentProcessingException("Pago fallido");
    }

    @GetMapping("/test/500")
    void internal() {
        throw new RuntimeException("boom");
    }

    @GetMapping("/test/illegal")
    void illegal() {
        throw new IllegalStateException("Estado inválido");
    }

    @GetMapping("/test/integrity")
    void integrity() {
        throw new DataIntegrityViolationException("duplicado");
    }

    @GetMapping("/test/denied")
    void denied() {
        throw new AccessDeniedException("denegado");
    }

    @GetMapping("/test/header")
    void header(@RequestHeader("X-User-Id") String userId) {
        // Solo para validar el mapeo 401 de cabecera faltante.
    }

    @GetMapping("/test/type")
    void type(@RequestParam EstadoPago estado) {
        // Solo para validar el mapeo 400 de parámetro inválido.
    }

    @PostMapping("/test/validation")
    void validation(@Valid @RequestBody IniciarSuscripcionRequest request) {
        // Solo para validar el mapeo 400 de Bean Validation.
    }
}
