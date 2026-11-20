package edu.dosw.styleradar.payments.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Amount(BigDecimal value, String currency) {

    public Amount {
        Objects.requireNonNull(value, "El monto es obligatorio.");
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto no puede ser negativo.");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("La moneda es obligatoria.");
        }
        currency = currency.trim().toUpperCase();
        if (currency.length() != 3) {
            throw new IllegalArgumentException("La moneda debe tener tres letras ISO 4217.");
        }
        value = value.setScale(2, RoundingMode.HALF_UP);
    }

    public static Amount cop(BigDecimal value) {
        return new Amount(value, "COP");
    }

    public static Amount zero(String currency) {
        return new Amount(BigDecimal.ZERO, currency);
    }

    public Amount add(Amount otro) {
        validateMismaCurrency(otro);
        return new Amount(value.add(otro.value), currency);
    }

    public Amount subtract(Amount otro) {
        validateMismaCurrency(otro);
        BigDecimal resultado = value.subtract(otro.value);
        if (resultado.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto resultante no puede ser negativo.");
        }
        return new Amount(resultado, currency);
    }

    public boolean esZero() {
        return value.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isGreaterThan(Amount otro) {
        validateMismaCurrency(otro);
        return value.compareTo(otro.value) > 0;
    }

    public boolean isLessThan(Amount otro) {
        validateMismaCurrency(otro);
        return value.compareTo(otro.value) < 0;
    }

    public boolean isEqualTo(Amount otro) {
        validateMismaCurrency(otro);
        return value.compareTo(otro.value) == 0;
    }

    private void validateMismaCurrency(Amount otro) {
        Objects.requireNonNull(otro, "El monto de comparación es obligatorio.");
        if (!currency.equals(otro.currency)) {
            throw new IllegalArgumentException("Los montos deben estar en la misma moneda.");
        }
    }
}
