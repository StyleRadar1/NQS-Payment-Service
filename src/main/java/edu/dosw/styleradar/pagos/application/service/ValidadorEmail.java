package edu.dosw.styleradar.pagos.application.service;

import java.util.regex.Pattern;

/**
 * Validación del correo del pagador.
 *
 * <p>SR-RF-67 FA-02, SR-RF-93 FA-01 y SR-RF-100 FA-02: no se inicia el pago
 * si el correo no tiene un formato válido.</p>
 */
public final class ValidadorEmail {

    private static final Pattern PATRON =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private ValidadorEmail() {
    }

    public static boolean esValido(String correo) {
        return correo != null && PATRON.matcher(correo.trim()).matches();
    }
}
