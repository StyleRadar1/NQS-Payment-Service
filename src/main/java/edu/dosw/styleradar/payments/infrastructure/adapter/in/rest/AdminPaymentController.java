package edu.dosw.styleradar.payments.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.payments.application.dto.response.PaymentResponse;
import edu.dosw.styleradar.payments.application.port.in.AdminPaymentPort;
import edu.dosw.styleradar.payments.domain.model.PaymentStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/payments")
@RequiredArgsConstructor
@Tag(name = "Administration", description = "Global payment query")
public class AdminPaymentController {

    private final AdminPaymentPort adminPaymentPort;

    @GetMapping
    @Operation(summary = "List all payments, with an optional status filter")
    public List<PaymentResponse> list(
            @RequestParam(value = "status", required = false) PaymentStatus status) {
        return adminPaymentPort.list(status);
    }
}
