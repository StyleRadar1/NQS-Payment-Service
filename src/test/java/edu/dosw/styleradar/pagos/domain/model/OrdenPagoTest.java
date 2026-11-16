package edu.dosw.styleradar.pagos.domain.model;

import edu.dosw.styleradar.pagos.domain.exception.Mensajes;
import edu.dosw.styleradar.pagos.domain.exception.PagoYaProcesadoException;
import edu.dosw.styleradar.pagos.domain.exception.SolicitudInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("OrdenPago (SR-RF-93/94/98)")
class OrdenPagoTest {

    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 14, 10, 0);

    private OrdenPago pendiente() {
        return OrdenPago.pendiente(1L, Monto.cop(new BigDecimal("100.00")), AHORA);
    }

    private OrdenPago aprobada() {
        OrdenPago orden = pendiente();
        orden.actualizarEstado(EstadoPago.APROBADO, "MP-1");
        return orden;
    }

    @Test
    void nacePendienteSinPagoExterno() {
        OrdenPago orden = pendiente();

        assertThat(orden.getEstado()).isEqualTo(EstadoPago.PENDIENTE);
        assertThat(orden.getIdPagoExterno()).isNull();
        assertThat(orden.getMontoReembolsado().esCero()).isTrue();
        assertThat(orden.saldoReembolsable().valor()).isEqualByComparingTo("100.00");
        assertThat(orden.estaPendiente()).isTrue();
        assertThat(orden.esReembolsoParcial()).isFalse();
        assertThat(orden.fueReembolsadoTotalmente()).isFalse();
        assertThat(orden.getFechaCreacion()).isEqualTo(AHORA);
        assertThat(orden.getId()).isNull();
    }

    @Test
    void validaDatosObligatorios() {
        assertThatThrownBy(() -> OrdenPago.pendiente(null, Monto.cop(BigDecimal.TEN), AHORA))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("suscripción");
        assertThatThrownBy(() -> OrdenPago.pendiente(1L, null, AHORA))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("monto");
    }

    @Test
    void rehidrataConReembolsoNuloComoCero() {
        OrdenPago orden = OrdenPago.rehidratar(5L, 1L, Monto.cop(new BigDecimal("100.00")),
                EstadoPago.APROBADO, "MP-1", null, null, AHORA);

        assertThat(orden.getId()).isEqualTo(5L);
        assertThat(orden.getMontoReembolsado().esCero()).isTrue();
    }

    @Nested
    @DisplayName("Transiciones de estado")
    class Estados {

        @Test
        void actualizaAprobadoConIdExterno() {
            OrdenPago orden = pendiente();

            orden.actualizarEstado(EstadoPago.APROBADO, "MP-123");

            assertThat(orden.getEstado()).isEqualTo(EstadoPago.APROBADO);
            assertThat(orden.getIdPagoExterno()).isEqualTo("MP-123");
            assertThat(orden.estaAprobada()).isTrue();
        }

        @Test
        void actualizaRechazado() {
            OrdenPago orden = pendiente();

            orden.actualizarEstado(EstadoPago.RECHAZADO, "MP-456");

            assertThat(orden.getEstado()).isEqualTo(EstadoPago.RECHAZADO);
            assertThat(orden.estaRechazada()).isTrue();
        }

        @Test
        void reembolsadoDesdeLaPasarelaMarcaElMontoCompleto() {
            OrdenPago orden = aprobada();

            orden.actualizarEstado(EstadoPago.REEMBOLSADO, "MP-1");

            assertThat(orden.getEstado()).isEqualTo(EstadoPago.REEMBOLSADO);
            assertThat(orden.getMontoReembolsado().valor()).isEqualByComparingTo("100.00");
            assertThat(orden.fueReembolsadoTotalmente()).isTrue();
        }

        @Test
        void ignoraEstadosPosterioresSiYaFueReembolsado() {
            OrdenPago orden = aprobada();
            orden.actualizarEstado(EstadoPago.REEMBOLSADO, "MP-1");

            orden.actualizarEstado(EstadoPago.APROBADO, "MP-2");

            assertThat(orden.getEstado()).isEqualTo(EstadoPago.REEMBOLSADO);
            assertThat(orden.getIdPagoExterno()).isEqualTo("MP-1");
        }

        @Test
        void rechazaEstadoNulo() {
            assertThatThrownBy(() -> pendiente().actualizarEstado(null, "MP-1"))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Reembolsos (SR-RF-98)")
    class Reembolsos {

        @Test
        void reembolsoTotalPasaAReembolsado() {
            OrdenPago orden = aprobada();

            orden.reembolsar(Monto.cop(new BigDecimal("100.00")), "Devolución completa");

            assertThat(orden.getEstado()).isEqualTo(EstadoPago.REEMBOLSADO);
            assertThat(orden.getMontoReembolsado().valor()).isEqualByComparingTo("100.00");
            assertThat(orden.saldoReembolsable().esCero()).isTrue();
            assertThat(orden.esReembolsoParcial()).isFalse();
            assertThat(orden.fueReembolsadoTotalmente()).isTrue();
            assertThat(orden.getMotivoReembolso()).isEqualTo("Devolución completa");
        }

        @Test
        void reembolsoParcialConservaAprobadoYAcumula() {
            OrdenPago orden = aprobada();

            orden.reembolsar(Monto.cop(new BigDecimal("40.00")), "Primera parte");

            assertThat(orden.getEstado()).isEqualTo(EstadoPago.APROBADO);
            assertThat(orden.getMontoReembolsado().valor()).isEqualByComparingTo("40.00");
            assertThat(orden.saldoReembolsable().valor()).isEqualByComparingTo("60.00");
            assertThat(orden.esReembolsoParcial()).isTrue();
            assertThat(orden.fueReembolsadoTotalmente()).isFalse();
        }

        @Test
        void segundoReembolsoCompletaElTotal() {
            OrdenPago orden = aprobada();
            orden.reembolsar(Monto.cop(new BigDecimal("40.00")), "Primera parte");

            orden.reembolsar(Monto.cop(new BigDecimal("60.00")), "Segunda parte");

            assertThat(orden.getEstado()).isEqualTo(EstadoPago.REEMBOLSADO);
            assertThat(orden.getMontoReembolsado().valor()).isEqualByComparingTo("100.00");
            assertThat(orden.getMotivoReembolso()).isEqualTo("Segunda parte");
        }

        @Test
        void noPermiteSuperarElSaldo() {
            OrdenPago orden = aprobada();

            assertThatThrownBy(() -> orden.reembolsar(
                    Monto.cop(new BigDecimal("100.01")), "Excede"))
                    .isInstanceOf(SolicitudInvalidaException.class)
                    .hasMessage(Mensajes.MONTO_SUPERA_SALDO);
        }

        @Test
        void validaMontoYmotivo() {
            OrdenPago orden = aprobada();

            assertThatThrownBy(() -> orden.reembolsar(null, "Motivo"))
                    .isInstanceOf(SolicitudInvalidaException.class)
                    .hasMessage(Mensajes.MONTO_INVALIDO);
            assertThatThrownBy(() -> orden.reembolsar(Monto.cop(BigDecimal.ZERO), "Motivo"))
                    .isInstanceOf(SolicitudInvalidaException.class)
                    .hasMessage(Mensajes.MONTO_INVALIDO);
            assertThatThrownBy(() -> orden.reembolsar(Monto.cop(BigDecimal.ONE), " "))
                    .isInstanceOf(SolicitudInvalidaException.class)
                    .hasMessage(Mensajes.MOTIVO_REEMBOLSO);
        }

        @Test
        void soloReembolsaPagosAprobados() {
            OrdenPago pendiente = pendiente();

            assertThatThrownBy(() -> pendiente.reembolsar(Monto.cop(BigDecimal.ONE), "Motivo"))
                    .isInstanceOf(SolicitudInvalidaException.class)
                    .hasMessage(Mensajes.PAGO_NO_APROBADO_REEMBOLSO);
        }

        @Test
        void noReembolsaDosVecesElTotal() {
            OrdenPago orden = aprobada();
            orden.reembolsar(Monto.cop(new BigDecimal("100.00")), "Total");

            assertThatThrownBy(() -> orden.reembolsar(Monto.cop(BigDecimal.ONE), "Otra"))
                    .isInstanceOf(PagoYaProcesadoException.class)
                    .hasMessage(Mensajes.PAGO_YA_REEMBOLSADO);
        }
    }
}
