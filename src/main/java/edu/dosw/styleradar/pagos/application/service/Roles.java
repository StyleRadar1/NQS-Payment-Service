package edu.dosw.styleradar.pagos.application.service;

import java.util.Locale;

/**
 * Utilidades de autorización basadas en las cabeceras del Gateway.
 *
 * <p>SR-RF-75 y SR-RF-98: solo el administrador de StyleRadar consulta todos
 * los pagos y reembolsa. SR-RF-97: cada actor consulta solo su historial.</p>
 */
public final class Roles {

    private Roles() {
    }

    public static boolean esAdmin(String rol) {
        if (rol == null || rol.isBlank()) {
            return false;
        }
        String normalizado = rol.trim().toUpperCase(Locale.ROOT);
        return normalizado.equals("ADMIN")
                || normalizado.equals("ROLE_ADMIN")
                || normalizado.equals("ADMIN_STYLERADAR")
                || normalizado.equals("ROLE_ADMIN_STYLERADAR");
    }

    public static boolean esTitular(String suscriptorId, String solicitanteId) {
        return suscriptorId != null && suscriptorId.equals(solicitanteId);
    }
}
