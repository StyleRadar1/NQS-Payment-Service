package edu.dosw.styleradar.pagos_suscripciones.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class PlanTest {

    @Test
    void debeCrearPlanActivoPorDefecto() {
        Plan plan = new Plan(1L, "Radar", TipoSuscriptor.USUARIO, new Monto(BigDecimal.ZERO, "COP"), Map.of());
        
        assertTrue(plan.isActivo());
        assertEquals("Radar", plan.getNombre());
        assertEquals(BigDecimal.ZERO, plan.getPrecio().valor());
    }

    @Test
    void debeDesactivarPlan() {
        Plan plan = new Plan(1L, "Radar", TipoSuscriptor.USUARIO, new Monto(BigDecimal.ZERO, "COP"), Map.of());
        plan.desactivar();
        
        assertFalse(plan.isActivo());
    }
}