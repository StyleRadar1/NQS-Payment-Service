package edu.dosw.styleradar.pagos.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Suscripción (SR-RF-67/68/95/99/101/103)")
class SuscripcionTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 14);
    private static final LocalDateTime AHORA = LocalDateTime.of(2026, 11, 1, 10, 0);

    private Suscripcion pendiente() {
        return Suscripcion.pendiente("USER-1", TipoSuscriptor.USUARIO, 2L, AHORA);
    }

    private Suscripcion activa(LocalDate inicio, LocalDate fin) {
        return Suscripcion.rehidratar(10L, "USER-1", TipoSuscriptor.USUARIO, 2L, null,
                EstadoSuscripcion.ACTIVA, inicio, fin, true, "PRE-1", null, AHORA);
    }

    @Test
    void nacePendienteSinFechas() {
        Suscripcion suscripcion = pendiente();

        assertThat(suscripcion.getEstado()).isEqualTo(EstadoSuscripcion.PENDIENTE);
        assertThat(suscripcion.getFechaInicio()).isNull();
        assertThat(suscripcion.getFechaFin()).isNull();
        assertThat(suscripcion.isAutoRenovacion()).isTrue();
        assertThat(suscripcion.getId()).isNull();
        assertThat(suscripcion.esPendiente()).isTrue();
    }

    @Test
    void validaDatosObligatorios() {
        assertThatThrownBy(() -> Suscripcion.pendiente(" ", TipoSuscriptor.USUARIO, 2L, AHORA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("suscriptor");
        assertThatThrownBy(() -> Suscripcion.pendiente("USER-1", null, 2L, AHORA))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("tipo");
        assertThatThrownBy(() -> Suscripcion.pendiente("USER-1", TipoSuscriptor.USUARIO, null, AHORA))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("plan");
    }

    @Test
    void asignaPreapprovalYNit() {
        Suscripcion suscripcion = pendiente();

        suscripcion.asignarPreapproval("PRE-123");
        suscripcion.asignarNit("900123456-7");

        assertThat(suscripcion.getPreapprovalIdMp()).isEqualTo("PRE-123");
        assertThat(suscripcion.getNit()).isEqualTo("900123456-7");
    }

    @Test
    void asignaNitVacioComoNulo() {
        Suscripcion suscripcion = pendiente();
        suscripcion.asignarNit("900123456");
        suscripcion.asignarNit(" ");

        assertThat(suscripcion.getNit()).isNull();
    }

    @Test
    void rechazaPreapprovalVaciaYNitLargo() {
        Suscripcion suscripcion = pendiente();

        assertThatThrownBy(() -> suscripcion.asignarPreapproval(" "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("preapproval");
        assertThatThrownBy(() -> suscripcion.asignarNit("9".repeat(31)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("NIT");
    }

    @Nested
    @DisplayName("Activación y renovación (SR-RF-95)")
    class Activacion {

        @Test
        void primeraActivacionAsignaPeriodoMensual() {
            Suscripcion suscripcion = pendiente();

            suscripcion.activar(HOY);

            assertThat(suscripcion.getEstado()).isEqualTo(EstadoSuscripcion.ACTIVA);
            assertThat(suscripcion.getFechaInicio()).isEqualTo(HOY);
            assertThat(suscripcion.getFechaFin()).isEqualTo(HOY.plusMonths(1));
            assertThat(suscripcion.isAutoRenovacion()).isTrue();
        }

        @Test
        void renovacionVigenteExtiendeDesdeFechaFin() {
            Suscripcion suscripcion = activa(HOY.minusMonths(1), HOY);

            suscripcion.activar(HOY);

            assertThat(suscripcion.getFechaFin()).isEqualTo(HOY.plusMonths(1));
            assertThat(suscripcion.getFechaInicio()).isEqualTo(HOY.minusMonths(1));
        }

        @Test
        void renovacionVencidaCuentaDesdeElPago() {
            Suscripcion suscripcion = activa(HOY.minusMonths(2), HOY.minusDays(3));

            suscripcion.activar(HOY);

            assertThat(suscripcion.getFechaInicio()).isEqualTo(HOY);
            assertThat(suscripcion.getFechaFin()).isEqualTo(HOY.plusMonths(1));
        }

        @Test
        void reactivacionDesdeSuspendidaCuentaDesdeElPago() {
            Suscripcion suscripcion = Suscripcion.rehidratar(10L, "USER-1", TipoSuscriptor.USUARIO, 2L,
                    null, EstadoSuscripcion.SUSPENDIDA, HOY.minusMonths(2), HOY.minusMonths(1),
                    false, "PRE-1", null, AHORA);

            suscripcion.activar(HOY);

            assertThat(suscripcion.getEstado()).isEqualTo(EstadoSuscripcion.ACTIVA);
            assertThat(suscripcion.getFechaInicio()).isEqualTo(HOY);
            assertThat(suscripcion.getFechaFin()).isEqualTo(HOY.plusMonths(1));
            assertThat(suscripcion.isAutoRenovacion()).isTrue();
        }

        @Test
        void rechazaFechaNula() {
            assertThatThrownBy(() -> pendiente().activar(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Cancelación y suspensión (SR-RF-68/99/101/103)")
    class CancelacionSuspension {

        @Test
        void cancelaRenovacionDeUnaSuscripcionActiva() {
            Suscripcion suscripcion = activa(HOY.minusDays(10), HOY.plusDays(20));

            suscripcion.cancelarRenovacion();

            assertThat(suscripcion.isAutoRenovacion()).isFalse();
            assertThat(suscripcion.getEstado()).isEqualTo(EstadoSuscripcion.ACTIVA);
            assertThat(suscripcion.getFechaFin()).isEqualTo(HOY.plusDays(20));
        }

        @Test
        void noCancelaRenovacionDePendienteOSuspendida() {
            assertThatThrownBy(() -> pendiente().cancelarRenovacion())
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("activa");

            Suscripcion suspendida = Suscripcion.rehidratar(10L, "USER-1", TipoSuscriptor.USUARIO, 2L,
                    null, EstadoSuscripcion.SUSPENDIDA, HOY.minusMonths(2), HOY.minusMonths(1),
                    false, null, null, AHORA);
            assertThatThrownBy(suspendida::cancelarRenovacion)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void suspendeCuandoElPeriodoYaVencio() {
            Suscripcion suscripcion = activa(HOY.minusMonths(2), HOY.minusDays(1));

            suscripcion.suspenderPorFaltaDePago(HOY);

            assertThat(suscripcion.getEstado()).isEqualTo(EstadoSuscripcion.SUSPENDIDA);
            assertThat(suscripcion.isAutoRenovacion()).isFalse();
        }

        @Test
        void noSuspendeSiAunNoVence() {
            Suscripcion suscripcion = activa(HOY.minusDays(10), HOY.plusDays(10));

            assertThatThrownBy(() -> suscripcion.suspenderPorFaltaDePago(HOY))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("aún no ha vencido");
        }

        @Test
        void noSuspendeUnaSuscripcionNoActiva() {
            assertThatThrownBy(() -> pendiente().suspenderPorFaltaDePago(HOY))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("activa");
            assertThatThrownBy(() -> pendiente().suspenderPorFaltaDePago(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Vigencia y acceso")
    class Vigencia {

        @Test
        void activaConFechaFinFuturaTieneAcceso() {
            Suscripcion suscripcion = activa(HOY.minusDays(10), HOY.plusDays(10));

            assertThat(suscripcion.estaVigente(HOY)).isTrue();
            assertThat(suscripcion.permiteAcceso(HOY)).isTrue();
        }

        @Test
        void laFechaFinEsInclusiva() {
            Suscripcion suscripcion = activa(HOY.minusMonths(1), HOY);

            assertThat(suscripcion.estaVigente(HOY)).isTrue();
        }

        @Test
        void activaVencidaNoTieneAcceso() {
            Suscripcion suscripcion = activa(HOY.minusMonths(2), HOY.minusDays(1));

            assertThat(suscripcion.estaVigente(HOY)).isFalse();
            assertThat(suscripcion.permiteAcceso(HOY)).isFalse();
        }

        @Test
        void pendienteYSuspendidaNoTienenAcceso() {
            assertThat(pendiente().estaVigente(HOY)).isFalse();

            Suscripcion suspendida = Suscripcion.rehidratar(10L, "USER-1", TipoSuscriptor.ALMACEN, 3L,
                    null, EstadoSuscripcion.SUSPENDIDA, HOY.minusMonths(2), HOY.minusMonths(1),
                    false, null, "900123", AHORA);
            assertThat(suspendida.estaVigente(HOY)).isFalse();
            assertThat(suspendida.esSuspendida()).isTrue();
            assertThat(suspendida.esDeTipo(TipoSuscriptor.ALMACEN)).isTrue();
        }
    }

    @Test
    void rehidrataTodosLosCampos() {
        Suscripcion suscripcion = Suscripcion.rehidratar(7L, "ALMACEN-9", TipoSuscriptor.ALMACEN, 3L, 2L,
                EstadoSuscripcion.ACTIVA, HOY.minusDays(5), HOY.plusDays(25), false,
                "PRE-9", "900123456-7", AHORA);

        assertThat(suscripcion.getId()).isEqualTo(7L);
        assertThat(suscripcion.getSuscriptorId()).isEqualTo("ALMACEN-9");
        assertThat(suscripcion.getTipoSuscriptor()).isEqualTo(TipoSuscriptor.ALMACEN);
        assertThat(suscripcion.getPlanId()).isEqualTo(3L);
        assertThat(suscripcion.getPlanProgramadoId()).isEqualTo(2L);
        assertThat(suscripcion.getNit()).isEqualTo("900123456-7");
        assertThat(suscripcion.getPreapprovalIdMp()).isEqualTo("PRE-9");
        assertThat(suscripcion.getFechaActualizacion()).isEqualTo(AHORA);
        assertThat(suscripcion.esActiva()).isTrue();
    }

    @Test
    void marcarActualizadaCambiaLaFecha() {
        Suscripcion suscripcion = pendiente();
        LocalDateTime antes = suscripcion.getFechaActualizacion();

        suscripcion.marcarActualizada();

        assertThat(suscripcion.getFechaActualizacion()).isAfterOrEqualTo(antes);
    }
}
