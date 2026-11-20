package edu.dosw.styleradar.payments.application.service;

import edu.dosw.styleradar.payments.domain.model.PaymentStatus;

import java.util.Locale;
import java.util.Optional;

public final class PaymentStatusMapper {

    private PaymentStatusMapper() {
    }

    public static Optional<PaymentStatus> fromMercadoPago(String mercadoPagoStatus) {
        if (mercadoPagoStatus == null || mercadoPagoStatus.isBlank()) {
            return Optional.empty();
        }
        return switch (mercadoPagoStatus.trim().toLowerCase(Locale.ROOT)) {
            case "approved" -> Optional.of(PaymentStatus.APPROVED);
            case "pending", "in_process", "authorized", "in_mediation" -> Optional.of(PaymentStatus.PENDING);
            case "rejected", "cancelled", "canceled" -> Optional.of(PaymentStatus.REJECTED);
            default -> Optional.empty();
        };
    }
}
