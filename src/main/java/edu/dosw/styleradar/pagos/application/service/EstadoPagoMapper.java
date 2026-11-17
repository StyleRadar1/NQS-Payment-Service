package edu.dosw.styleradar.pagos.application.service;

import edu.dosw.styleradar.pagos.domain.model.EstadoPago;

import java.util.Locale;
import java.util.Optional;

/**
 * Traduce los estados de Mercado Pago a los cuatro estados del dominio.
 *
 * <p>SR-RF-75 RN-03 y SR-RF-94 RN-03: la pasarela es la fuente de verdad y
 * StyleRadar solo maneja PENDIENTE, APROBADO, RECHAZADO y REEMBOLSADO.</p>
 */
public final class EstadoPagoMapper {

    private EstadoPagoMapper() {
    }

    public static Optional<EstadoPago> desdeMercadoPago(String estadoMp) {
        if (estadoMp == null || estadoMp.isBlank()) {
            return Optional.empty();
        }
        return switch (estadoMp.trim().toLowerCase(Locale.ROOT)) {
            case "approved" -> Optional.of(EstadoPago.APROBADO);
            case "pending", "in_process", "authorized", "in_mediation" -> Optional.of(EstadoPago.PENDIENTE);
            case "rejected", "cancelled", "canceled" -> Optional.of(EstadoPago.RECHAZADO);
            case "refunded", "charged_back" -> Optional.of(EstadoPago.REEMBOLSADO);
            default -> Optional.empty();
        };
    }
}
