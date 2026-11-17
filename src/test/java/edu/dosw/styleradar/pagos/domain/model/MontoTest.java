package edu.dosw.styleradar.pagos.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Monto (SR-RF-75, SR-RF-98)")
class MontoTest {

    @Test
    void normalizaMonedaYEscala() {
        Monto monto = new Monto(new BigDecimal("14900"), "cop");

        assertThat(monto.valor()).isEqualByComparingTo("14900.00");
        assertThat(monto.moneda()).isEqualTo("COP");
    }

    @Test
    void rechazaValorNulo() {
        assertThatThrownBy(() -> new Monto(null, "COP"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("valor");
    }

    @Test
    void rechazaValorNegativo() {
        assertThatThrownBy(() -> new Monto(new BigDecimal("-1"), "COP"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("negativo");
    }

    @Test
    void rechazaMonedaVaciaOInvalida() {
        assertThatThrownBy(() -> new Monto(BigDecimal.ONE, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("moneda");
        assertThatThrownBy(() -> new Monto(BigDecimal.ONE, "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("moneda");
        assertThatThrownBy(() -> new Monto(BigDecimal.ONE, "CO"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("tres letras");
    }

    @Test
    void creaCopYCero() {
        assertThat(Monto.cop(new BigDecimal("29900")).moneda()).isEqualTo("COP");
        assertThat(Monto.cero("COP").esCero()).isTrue();
    }

    @Nested
    class Operaciones {

        private final Monto base = Monto.cop(new BigDecimal("100.00"));

        @Test
        void sumaYRestarMismaMoneda() {
            Monto suma = base.sumar(Monto.cop(new BigDecimal("50")));
            Monto resta = base.restar(Monto.cop(new BigDecimal("40")));

            assertThat(suma.valor()).isEqualByComparingTo("150.00");
            assertThat(resta.valor()).isEqualByComparingTo("60.00");
        }

        @Test
        void rechazaMonedasDistintas() {
            Monto usd = new Monto(BigDecimal.TEN, "USD");

            assertThatThrownBy(() -> base.sumar(usd))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("misma moneda");
            assertThatThrownBy(() -> base.restar(usd))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("misma moneda");
            assertThatThrownBy(() -> base.esMayorQue(usd))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void restaNoPuedeSerNegativa() {
            assertThatThrownBy(() -> base.restar(Monto.cop(new BigDecimal("101"))))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("negativo");
        }

        @Test
        void comparaciones() {
            Monto mayor = Monto.cop(new BigDecimal("200"));
            Monto menor = Monto.cop(new BigDecimal("50"));

            assertThat(mayor.esMayorQue(base)).isTrue();
            assertThat(menor.esMenorQue(base)).isTrue();
            assertThat(base.esIgualA(Monto.cop(new BigDecimal("100")))).isTrue();
            assertThat(base.esIgualA(mayor)).isFalse();
        }

        @Test
        void comparaContraMontoNulo() {
            assertThatThrownBy(() -> base.sumar(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }
}
