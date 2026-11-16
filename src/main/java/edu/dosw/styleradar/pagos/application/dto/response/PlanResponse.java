package edu.dosw.styleradar.pagos.application.dto.response;

import edu.dosw.styleradar.pagos.domain.model.Plan;
import edu.dosw.styleradar.pagos.domain.model.TipoSuscriptor;

import java.util.Map;

/**
 * Plan visible para el frontend y para los consumidores de beneficios.
 *
 * <p>SR-RF-100: el usuario debe conocer Radar+ y su precio antes de pagar.
 * SR-RF-102: los límites permiten aplicar los beneficios del plan.</p>
 */
public record PlanResponse(
        Long id,
        String nombre,
        TipoSuscriptor tipoSuscriptor,
        MontoResponse precio,
        Map<String, Object> limites,
        boolean activo) {

    public static PlanResponse desde(Plan plan) {
        return new PlanResponse(
                plan.getId(),
                plan.getNombre(),
                plan.getTipoSuscriptor(),
                MontoResponse.desde(plan.getPrecio()),
                plan.getLimites(),
                plan.isActivo());
    }
}
