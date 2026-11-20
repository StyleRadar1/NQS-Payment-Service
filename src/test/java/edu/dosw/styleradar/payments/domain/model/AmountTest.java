package edu.dosw.styleradar.payments.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
class AmountTest {

    @Test
    void normalizaCurrencyYEscala() {
        Amount amount = new Amount(new BigDecimal("14900"), "cop");

        assertThat(amount.value()).isEqualByComparingTo("14900.00");
        assertThat(amount.currency()).isEqualTo("COP");
    }

    @Test
    void rechazaValueNulo() {
        assertThatThrownBy(() -> new Amount(null, "COP"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("monto");
    }

    @Test
    void rechazaValueNegativo() {
        assertThatThrownBy(() -> new Amount(new BigDecimal("-1"), "COP"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negativo");
    }

    @Test
    void rechazaCurrencyVaciaOInvalida() {
        assertThatThrownBy(() -> new Amount(BigDecimal.ONE, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("moneda");
        assertThatThrownBy(() -> new Amount(BigDecimal.ONE, "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("moneda");
        assertThatThrownBy(() -> new Amount(BigDecimal.ONE, "CO"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tres letras");
    }

    @Test
    void creaCopYZero() {
        assertThat(Amount.cop(new BigDecimal("89000")).currency()).isEqualTo("COP");
        assertThat(Amount.zero("COP").esZero()).isTrue();
    }

    @Nested
    class Operaciones {

        private final Amount base = Amount.cop(new BigDecimal("100.00"));

        @Test
        void sumaYRestarMismaCurrency() {
            Amount suma = base.add(Amount.cop(new BigDecimal("50")));
            Amount resta = base.subtract(Amount.cop(new BigDecimal("40")));

            assertThat(suma.value()).isEqualByComparingTo("150.00");
            assertThat(resta.value()).isEqualByComparingTo("60.00");
        }

        @Test
        void rechazaCurrencysDistintas() {
            Amount usd = new Amount(BigDecimal.TEN, "USD");

            assertThatThrownBy(() -> base.add(usd))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("misma moneda");
            assertThatThrownBy(() -> base.subtract(usd))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("misma moneda");
            assertThatThrownBy(() -> base.isGreaterThan(usd))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void restaNoPuedeSerNegativa() {
            assertThatThrownBy(() -> base.subtract(Amount.cop(new BigDecimal("101"))))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("negativo");
        }

        @Test
        void comparaciones() {
            Amount mayor = Amount.cop(new BigDecimal("200"));
            Amount menor = Amount.cop(new BigDecimal("50"));

            assertThat(mayor.isGreaterThan(base)).isTrue();
            assertThat(menor.isLessThan(base)).isTrue();
            assertThat(base.isEqualTo(Amount.cop(new BigDecimal("100")))).isTrue();
            assertThat(base.isEqualTo(mayor)).isFalse();
        }

        @Test
        void comparaContraAmountNulo() {
            assertThatThrownBy(() -> base.add(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
