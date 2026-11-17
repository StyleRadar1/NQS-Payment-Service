package edu.dosw.styleradar.pagos.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ValidadorEmail (SR-RF-67/93/100)")
class ValidadorEmailTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "test_user_117646358391238183361@testuser.com",
            "a.b+c@dominio.co",
            "  usuario@styleradar.com  "
    })
    void aceptaCorreosValidos(String correo) {
        assertThat(ValidadorEmail.esValido(correo)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "a@b", "sin-arroba.com", "@dominio.com", "a@.com", "a@b.c"})
    void rechazaCorreosInvalidos(String correo) {
        assertThat(ValidadorEmail.esValido(correo)).isFalse();
    }

    @Test
    void rechazaNulo() {
        assertThat(ValidadorEmail.esValido(null)).isFalse();
    }
}
