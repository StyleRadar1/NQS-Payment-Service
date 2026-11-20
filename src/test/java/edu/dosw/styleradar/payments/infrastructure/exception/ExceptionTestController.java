package edu.dosw.styleradar.payments.infrastructure.exception;

import edu.dosw.styleradar.payments.application.dto.request.StartSubscriptionRequest;
import edu.dosw.styleradar.payments.domain.exception.IdempotencyException;
import edu.dosw.styleradar.payments.domain.exception.OperationNotAllowedException;
import edu.dosw.styleradar.payments.domain.exception.PaymentProcessingException;
import edu.dosw.styleradar.payments.domain.exception.InvalidRequestException;
import edu.dosw.styleradar.payments.domain.exception.SubscriptionNotFoundException;
import edu.dosw.styleradar.payments.domain.model.PaymentStatus;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ExceptionTestController {

    @GetMapping("/test/400")
    void badRequest() {
        throw new InvalidRequestException("Request inválida");
    }

    @GetMapping("/test/403")
    void forbidden() {
        throw new OperationNotAllowedException("Sin permiso");
    }

    @GetMapping("/test/404")
    void notFound() {
        throw new SubscriptionNotFoundException();
    }

    @GetMapping("/test/409")
    void conflict() {
        throw new IdempotencyException();
    }

    @GetMapping("/test/422")
    void unprocessable() {
        throw new PaymentProcessingException("Payment fallido");
    }

    @GetMapping("/test/500")
    void internal() {
        throw new RuntimeException("boom");
    }

    @GetMapping("/test/illegal")
    void illegal() {
        throw new IllegalStateException("Status inválido");
    }

    @GetMapping("/test/integrity")
    void integrity() {
        throw new DataIntegrityViolationException("duplicado");
    }

    @GetMapping("/test/denied")
    void denied() {
        throw new AccessDeniedException("denied");
    }

    @GetMapping("/test/header")
    void header(@RequestHeader("X-User-Id") String userId) {
    }

    @GetMapping("/test/type")
    void type(@RequestParam PaymentStatus status) {
    }

    @PostMapping("/test/validation")
    void validation(@Valid @RequestBody StartSubscriptionRequest request) {
    }
}
