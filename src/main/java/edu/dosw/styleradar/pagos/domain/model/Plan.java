package edu.dosw.styleradar.pagos.domain.model;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Plan de suscripción ofrecido por StyleRadar.
 *
 * <p>SR-RF-67 RN-01: existe un único plan para almacenes.
 * SR-RF-100 RN-01/RN-02: Radar es gratuito y Radar+ es el único plan de pago
 * para usuarios compradores.
 * SR-RF-102: los límites del plan determinan los beneficios aplicables.</p>
 */
public final class Plan {

    private final Long id;
    private final String nombre;
    private final TipoSuscriptor tipoSuscriptor;
    private final Monto precio;
    private final Map<String, Object> limites;
    private final boolean activo;

    public Plan(Long id,
                String nombre,
                TipoSuscriptor tipoSuscriptor,
                Monto precio,
                Map<String, Object> limites,
                boolean activo) {
        this.id = id;
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del plan es obligatorio.");
        }
        this.nombre = nombre.trim();
        this.tipoSuscriptor = Objects.requireNonNull(tipoSuscriptor, "El tipo de suscriptor es obligatorio.");
        this.precio = Objects.requireNonNull(precio, "El precio del plan es obligatorio.");
        this.limites = limites == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(limites));
        this.activo = activo;
    }

    public static Plan gratuito(Long id, String nombre) {
        return new Plan(id, nombre, TipoSuscriptor.USUARIO, Monto.cop(BigDecimal.ZERO), Map.of(), true);
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoSuscriptor getTipoSuscriptor() {
        return tipoSuscriptor;
    }

    public Monto getPrecio() {
        return precio;
    }

    public Map<String, Object> getLimites() {
        return limites;
    }

    public boolean isActivo() {
        return activo;
    }

    public boolean esGratuito() {
        return precio.esCero();
    }

    public boolean esDePago() {
        return !esGratuito();
    }

    public boolean esDeTipo(TipoSuscriptor tipo) {
        return tipoSuscriptor == tipo;
    }
}
