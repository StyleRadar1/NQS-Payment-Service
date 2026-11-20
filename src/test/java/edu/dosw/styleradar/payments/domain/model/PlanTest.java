package edu.dosw.styleradar.payments.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
class PlanTest {

    private final Amount price = Amount.cop(new BigDecimal("14900.00"));

    @Test
    void creaPlanDePaymentValido() {
        Map<String, Object> limits = new HashMap<>();
        limits.put("max_wishlist", -1);

        Plan plan = new Plan(2L, " Radar+ ", SubscriberType.USER, price, limits, true);

        assertThat(plan.getId()).isEqualTo(2L);
        assertThat(plan.getName()).isEqualTo("Radar+");
        assertThat(plan.getPrice()).isEqualTo(price);
        assertThat(plan.isActive()).isTrue();
        assertThat(plan.esDePayment()).isTrue();
        assertThat(plan.isFree()).isFalse();
        assertThat(plan.isOfType(SubscriberType.USER)).isTrue();
        assertThat(plan.getLimits()).containsEntry("max_wishlist", -1);
    }

    @Test
    void limitsEsInmutableYCopiaElMapa() {
        Map<String, Object> limits = new HashMap<>();
        limits.put("max_productos", 50);
        Plan plan = new Plan(3L, "Almacén Básico", SubscriberType.STORE, price, limits, true);

        limits.put("max_productos", 10);

        assertThat(plan.getLimits()).containsEntry("max_productos", 50);
        assertThatThrownBy(() -> plan.getLimits().put("otro", 1))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void limitsNuloQuedaVacio() {
        Plan plan = new Plan(1L, "Radar", SubscriberType.USER, Amount.cop(BigDecimal.ZERO), null, true);

        assertThat(plan.getLimits()).isEmpty();
        assertThat(plan.isFree()).isTrue();
    }

    @Test
    void planFreeDeFabrica() {
        Plan radar = Plan.free(1L, "Radar");

        assertThat(radar.isFree()).isTrue();
        assertThat(radar.isOfType(SubscriberType.USER)).isTrue();
    }

    @Test
    void validaCamposObligatorios() {
        assertThatThrownBy(() -> new Plan(1L, " ", SubscriberType.USER, price, Map.of(), true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("nombre");
        assertThatThrownBy(() -> new Plan(1L, "Radar", null, price, Map.of(), true))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("tipo");
        assertThatThrownBy(() -> new Plan(1L, "Radar", SubscriberType.USER, null, Map.of(), true))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("precio");
    }
}
