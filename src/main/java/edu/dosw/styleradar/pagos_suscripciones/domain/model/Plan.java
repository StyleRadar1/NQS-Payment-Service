package edu.dosw.styleradar.pagos_suscripciones.domain.model;

import java.util.Map;

public class Plan {
    private final Long id;
    private final String nombre;
    private final TipoSuscriptor tipoSuscriptor;
    private final Monto precio;
    private final Map<String, Object> limites; // Ej: {"wishlist_max": 20}
    private boolean activo;

    public Plan(Long id, String nombre, TipoSuscriptor tipoSuscriptor, Monto precio, Map<String, Object> limites) {
        this.id = id;
        this.nombre = nombre;
        this.tipoSuscriptor = tipoSuscriptor;
        this.precio = precio;
        this.limites = limites;
        this.activo = true;
    }
    // Getters y lógica de desactivación...
}