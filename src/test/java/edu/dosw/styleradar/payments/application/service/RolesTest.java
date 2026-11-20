package edu.dosw.styleradar.payments.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
class RolesTest {

    @Test
    void reconoceAdministrador() {
        assertThat(Roles.isAdmin("ADMIN")).isTrue();
        assertThat(Roles.isAdmin("admin")).isTrue();
        assertThat(Roles.isAdmin("ROLE_ADMIN")).isTrue();
        assertThat(Roles.isAdmin("ADMIN_STYLERADAR")).isTrue();
        assertThat(Roles.isAdmin("ROLE_ADMIN_STYLERADAR")).isTrue();
    }

    @Test
    void noReconoceOtrosRoles() {
        assertThat(Roles.isAdmin(null)).isFalse();
        assertThat(Roles.isAdmin(" ")).isFalse();
        assertThat(Roles.isAdmin("USER")).isFalse();
        assertThat(Roles.isAdmin("COMPRADOR")).isFalse();
        assertThat(Roles.isAdmin("ADMIN_STORE")).isFalse();
    }

    @Test
    void validaTitularidad() {
        assertThat(Roles.isOwner("USER-1", "USER-1")).isTrue();
        assertThat(Roles.isOwner("USER-1", "USER-2")).isFalse();
        assertThat(Roles.isOwner(null, "USER-1")).isFalse();
        assertThat(Roles.isOwner("USER-1", null)).isFalse();
    }
}
