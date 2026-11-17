package edu.dosw.styleradar.pagos.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object inmutable para representar dinero.
 *
 * <p>SR-RF-75: el valor del pago debe ser igual o mayor que cero.
 * SR-RF-98: un reembolso no puede superar el valor original y se acumula
 * en la misma moneda.</p>
 */
public record Monto(BigDecimal valor, String moneda) {

    public Monto {
        Objects.requireNonNull(valor, "El valor del monto es obligatorio.");
        if (valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El valor del monto no puede ser negativo.");
        }
        if (moneda == null || moneda.isBlank()) {
            throw new IllegalArgumentException("La moneda del monto es obligatoria.");
        }
        moneda = moneda.trim().toUpperCase();
        if (moneda.length() != 3) {
            throw new IllegalArgumentException("La moneda debe tener tres letras ISO 4217.");
        }
        valor = valor.setScale(2, RoundingMode.HALF_UP);
    }

    public static Monto cop(BigDecimal valor) {
        return new Monto(valor, "COP");
    }

    public static Monto cero(String moneda) {
        return new Monto(BigDecimal.ZERO, moneda);
    }

    public Monto sumar(Monto otro) {
        validarMismaMoneda(otro);
        return new Monto(valor.add(otro.valor), moneda);
    }

    public Monto restar(Monto otro) {
        validarMismaMoneda(otro);
        BigDecimal resultado = valor.subtract(otro.valor);
        if (resultado.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El resultado del monto no puede ser negativo.");
        }
        return new Monto(resultado, moneda);
    }

    public boolean esCero() {
        return valor.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean esMayorQue(Monto otro) {
        validarMismaMoneda(otro);
        return valor.compareTo(otro.valor) > 0;
    }

    public boolean esMenorQue(Monto otro) {
        validarMismaMoneda(otro);
        return valor.compareTo(otro.valor) < 0;
    }

    public boolean esIgualA(Monto otro) {
        validarMismaMoneda(otro);
        return valor.compareTo(otro.valor) == 0;
    }

    private void validarMismaMoneda(Monto otro) {
        Objects.requireNonNull(otro, "El monto de comparación es obligatorio.");
        if (!moneda.equals(otro.moneda)) {
            throw new IllegalArgumentException("Los montos deben estar en la misma moneda.");
        }
    }
}
