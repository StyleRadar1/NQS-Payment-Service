package edu.dosw.styleradar.payments.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.payments.application.dto.request.RetryPaymentRequest;
import edu.dosw.styleradar.payments.application.dto.response.PaymentResponse;
import edu.dosw.styleradar.payments.application.dto.response.RetryPaymentResponse;
import edu.dosw.styleradar.payments.application.port.in.PaymentPort;
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

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment history and retries")
public class PaymentController {

    private final PaymentPort paymentPort;

    @GetMapping("/history")
    @Operation(summary = "Get the authenticated user's payment history")
    public List<PaymentResponse> history(@RequestHeader("X-User-Id") String subscriberId) {
        return paymentPort.history(subscriberId);
    }

    @PostMapping("/{orderId}/retry")
    @Operation(summary = "Create a new payment attempt for a rejected order")
    public RetryPaymentResponse retry(
            @PathVariable Long orderId,
            @RequestHeader("X-User-Id") String requesterId,
            @Valid @RequestBody RetryPaymentRequest request) {
        return paymentPort.retry(orderId, request.payerEmail(), requesterId);
    }
}
