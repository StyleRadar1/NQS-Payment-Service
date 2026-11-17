package edu.dosw.styleradar.pagos.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Plan (SR-RF-67, SR-RF-100, SR-RF-102)")
class PlanTest {

    private final Monto precio = Monto.cop(new BigDecimal("14900.00"));

    @Test
    void creaPlanDePagoValido() {
        Map<String, Object> limites = new HashMap<>();
        limites.put("max_wishlist", -1);

        Plan plan = new Plan(2L, " Radar+ ", TipoSuscriptor.USUARIO, precio, limites, true);

        assertThat(plan.getId()).isEqualTo(2L);
        assertThat(plan.getNombre()).isEqualTo("Radar+");
        assertThat(plan.getPrecio()).isEqualTo(precio);
        assertThat(plan.isActivo()).isTrue();
        assertThat(plan.esDePago()).isTrue();
        assertThat(plan.esGratuito()).isFalse();
        assertThat(plan.esDeTipo(TipoSuscriptor.USUARIO)).isTrue();
        assertThat(plan.getLimites()).containsEntry("max_wishlist", -1);
    }

    @Test
    void limitesEsInmutableYCopiaElMapa() {
        Map<String, Object> limites = new HashMap<>();
        limites.put("max_productos", 50);
        Plan plan = new Plan(3L, "Almacén Básico", TipoSuscriptor.ALMACEN, precio, limites, true);

        limites.put("max_productos", 10);

        assertThat(plan.getLimites()).containsEntry("max_productos", 50);
        assertThatThrownBy(() -> plan.getLimites().put("otro", 1))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void limitesNuloQuedaVacio() {
        Plan plan = new Plan(1L, "Radar", TipoSuscriptor.USUARIO, Monto.cop(BigDecimal.ZERO), null, true);

        assertThat(plan.getLimites()).isEmpty();
        assertThat(plan.esGratuito()).isTrue();
    }

    @Test
    void planGratuitoDeFabrica() {
        Plan radar = Plan.gratuito(1L, "Radar");

        assertThat(radar.esGratuito()).isTrue();
        assertThat(radar.esDeTipo(TipoSuscriptor.USUARIO)).isTrue();
    }

    @Test
    void validaCamposObligatorios() {
        assertThatThrownBy(() -> new Plan(1L, " ", TipoSuscriptor.USUARIO, precio, Map.of(), true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre");
        assertThatThrownBy(() -> new Plan(1L, "Radar", null, precio, Map.of(), true))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("tipo");
        assertThatThrownBy(() -> new Plan(1L, "Radar", TipoSuscriptor.USUARIO, null, Map.of(), true))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("precio");
    }
}
