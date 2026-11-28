package edu.dosw.styleradar.payments.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
class EmailValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "test_user_117646358391238183361@testuser.com",
            "a.b+c@dominio.co",
            "  user@styleradar.com  "
    })
    void aceptaEmailsValidos(String email) {
        assertThat(EmailValidator.isValid(email)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "a@b", "sin-arroba.com", "@dominio.com", "a@.com", "a@b.c"})
    void rechazaEmailsInvalidos(String email) {
        assertThat(EmailValidator.isValid(email)).isFalse();
    }

    @Test
    void rechazaNulo() {
        assertThat(EmailValidator.isValid(null)).isFalse();
    }
}
