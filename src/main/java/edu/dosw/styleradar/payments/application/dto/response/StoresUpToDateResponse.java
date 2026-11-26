package edu.dosw.styleradar.payments.application.dto.response;

import java.util.List;

/** NIT de las tiendas con suscripcion activa y vigente. */
public record StoresUpToDateResponse(List<String> taxIds) {
}
