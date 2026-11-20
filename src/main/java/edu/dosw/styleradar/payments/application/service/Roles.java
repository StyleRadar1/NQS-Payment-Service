package edu.dosw.styleradar.payments.application.service;

import java.util.Locale;

public final class Roles {

    private Roles() {
    }

    public static boolean isAdmin(String role) {
        if (role == null || role.isBlank()) {
            return false;
        }
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        return normalized.equals("ADMIN")
                || normalized.equals("ROLE_ADMIN")
                || normalized.equals("ADMIN_STYLERADAR")
                || normalized.equals("ROLE_ADMIN_STYLERADAR");
    }

    public static boolean isOwner(String subscriberId, String requesterId) {
        return subscriberId != null && subscriberId.equals(requesterId);
    }
}
