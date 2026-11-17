package edu.dosw.styleradar.pagos.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Roles (SR-RF-75/97/98)")
class RolesTest {

    @Test
    void reconoceAdministrador() {
        assertThat(Roles.esAdmin("ADMIN")).isTrue();
        assertThat(Roles.esAdmin("admin")).isTrue();
        assertThat(Roles.esAdmin("ROLE_ADMIN")).isTrue();
        assertThat(Roles.esAdmin("ADMIN_STYLERADAR")).isTrue();
        assertThat(Roles.esAdmin("ADMIN_ALMACEN")).isFalse();
    }

    @Test
    void noReconoceOtrosRoles() {
        assertThat(Roles.esAdmin(null)).isFalse();
        assertThat(Roles.esAdmin(" ")).isFalse();
        assertThat(Roles.esAdmin("USUARIO")).isFalse();
        assertThat(Roles.esAdmin("COMPRADOR")).isFalse();
    }

    @Test
    void validaTitularidad() {
        assertThat(Roles.esTitular("USER-1", "USER-1")).isTrue();
        assertThat(Roles.esTitular("USER-1", "USER-2")).isFalse();
        assertThat(Roles.esTitular(null, "USER-1")).isFalse();
        assertThat(Roles.esTitular("USER-1", null)).isFalse();
    }
}
