package edu.dosw.styleradar.pagos_suscripciones.domain.model;

import java.math.BigDecimal;

public record Monto(BigDecimal valor, String moneda) {
    public Monto {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto no puede ser negativo");
        }
        if (moneda == null || moneda.isBlank()) {
            throw new IllegalArgumentException("La moneda es obligatoria");
        }
    }
    public Monto sumar(Monto otro) {
        if (!this.moneda.equals(otro.moneda)) {
            throw new IllegalArgumentException("No se pueden sumar montos con diferentes monedas");
        }
        return new Monto(this.valor.add(otro.valor), this.moneda);
    }
}