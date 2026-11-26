package edu.dosw.styleradar.payments.infrastructure.adapter.in.rest;

import edu.dosw.styleradar.payments.application.dto.response.StoresUpToDateResponse;
import edu.dosw.styleradar.payments.application.dto.request.CancelSubscriptionRequest;
import edu.dosw.styleradar.payments.application.dto.request.StartSubscriptionRequest;
import edu.dosw.styleradar.payments.application.dto.response.CancellationResponse;
import edu.dosw.styleradar.payments.application.dto.response.StartSubscriptionResponse;
import edu.dosw.styleradar.payments.application.dto.response.SubscriptionResponse;
import edu.dosw.styleradar.payments.application.port.in.SubscriptionPort;
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

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscriptions", description = "Payment, query and cancellation of subscriptions")
public class SubscriptionController {

    private final SubscriptionPort subscriptionPort;

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Start a subscription payment and return the Mercado Pago link")
    public StartSubscriptionResponse start(
            @RequestHeader("X-User-Id") String subscriberId,
            @Valid @RequestBody StartSubscriptionRequest request) {
        return subscriptionPort.start(subscriberId, request);
    }

    @GetMapping("/stores/up-to-date")
    @Operation(summary = "List the tax IDs of stores with an active, unexpired subscription (used by the map)")
    public StoresUpToDateResponse storesUpToDate() {
        return subscriptionPort.listStoresUpToDate();
    }

    @GetMapping("/{subscriberId}")
    @Operation(summary = "Get the status, validity and plan of a subscription")
    public SubscriptionResponse get(
            @PathVariable String subscriberId,
            @RequestHeader("X-User-Id") String requesterId,
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        return subscriptionPort.get(subscriberId, requesterId, role);
    }

    @PostMapping("/{subscriberId}/cancel")
    @Operation(summary = "Cancel automatic renewal while keeping the paid period")
    public CancellationResponse cancel(
            @PathVariable String subscriberId,
            @RequestHeader("X-User-Id") String requesterId,
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @Valid @RequestBody CancelSubscriptionRequest request) {
        return subscriptionPort.cancel(subscriberId, request.confirmed(), requesterId, role);
    }
}

