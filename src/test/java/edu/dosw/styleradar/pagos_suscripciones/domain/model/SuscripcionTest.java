package edu.dosw.styleradar.pagos_suscripciones.domain.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class SuscripcionTest {

    @Test
    void debeIniciarEnEstadoPendiente() {
        Suscripcion s = new Suscripcion(1L, "USER-1", TipoSuscriptor.USUARIO, 1L);
        assertEquals(EstadoSuscripcion.PENDIENTE, s.getEstado());
        assertTrue(s.isAutoRenovacion());
    }

    @Test
    void debeActivarSinReiniciarFechaInicioSiYaExiste() {
        Suscripcion s = new Suscripcion(1L, "U", TipoSuscriptor.USUARIO, 1L);
        LocalDate inicioOriginal = LocalDate.now().minusDays(15);
        s.rehidratar(EstadoSuscripcion.ACTIVA, inicioOriginal, LocalDate.now().plusDays(10), true, null, null);

        s.activar(LocalDate.now().plusMonths(1));

        assertEquals(inicioOriginal, s.getFechaInicio());
        assertEquals(EstadoSuscripcion.ACTIVA, s.getEstado());
    }

    @Test
    void renovarDebeExtenderDesdeFechaFinSiSigueVigente() {
        Suscripcion s = new Suscripcion(1L, "U", TipoSuscriptor.USUARIO, 1L);
        LocalDate finFuturo = LocalDate.now().plusDays(10);
        s.rehidratar(EstadoSuscripcion.ACTIVA, LocalDate.now(), finFuturo, true, null, null);

        s.renovar();

        assertEquals(finFuturo.plusMonths(1), s.getFechaFin());
    }

    @Test
    void renovarDebeExtenderDesdeHoySiYaVencio() {
        Suscripcion s = new Suscripcion(1L, "U", TipoSuscriptor.USUARIO, 1L);
        s.rehidratar(EstadoSuscripcion.VENCIDA, LocalDate.now().minusMonths(2),
                     LocalDate.now().minusDays(5), true, null, null);

        s.renovar();

        assertEquals(LocalDate.now().plusMonths(1), s.getFechaFin());
        assertEquals(EstadoSuscripcion.ACTIVA, s.getEstado());
    }

    @Test
    void debeMarcarVencidaLuegoSuspender() {
        Suscripcion s = new Suscripcion(1L, "U", TipoSuscriptor.ALMACEN, 3L);
        s.marcarVencida();
        assertEquals(EstadoSuscripcion.VENCIDA, s.getEstado());
        s.suspenderPorFaltaDePago();
        assertEquals(EstadoSuscripcion.SUSPENDIDA, s.getEstado());
    }

    @Test
    void cancelarRenovacionEsIdempotente() {
        Suscripcion s = new Suscripcion(1L, "U", TipoSuscriptor.USUARIO, 1L);
        s.cancelarRenovacion();
        s.cancelarRenovacion(); // no debe lanzar
        assertFalse(s.isAutoRenovacion());
    }

    @Test
    void programarYAplicarCambioPlan() {
        Suscripcion s = new Suscripcion(1L, "U", TipoSuscriptor.USUARIO, 1L);
        s.programarCambioPlan(2L);
        assertEquals(2L, s.getPlanProgramadoId());
        assertEquals(1L, s.getPlanId());
        s.aplicarCambioPlan();
        assertEquals(2L, s.getPlanId());
        assertNull(s.getPlanProgramadoId());
    }

    @Test
    void revertirAPlanGratuitoDebeResetear() {
        Suscripcion s = new Suscripcion(1L, "U", TipoSuscriptor.USUARIO, 2L);
        s.revertirAPlanGratuito(1L);
        assertEquals(1L, s.getPlanId());
        assertEquals(EstadoSuscripcion.SUSPENDIDA, s.getEstado());
        assertFalse(s.isAutoRenovacion());
        assertEquals(LocalDate.now(), s.getFechaFin());
    }

    @Test
void activarSinFechaInicioPreviaDebeSetearHoy() {
    Suscripcion s = new Suscripcion(1L, "U", TipoSuscriptor.USUARIO, 1L);
    s.activar(LocalDate.now().plusMonths(1));
    assertEquals(LocalDate.now(), s.getFechaInicio());
}

@Test
void renovarSinFechaInicioDebeSetearHoy() {
    Suscripcion s = new Suscripcion(1L, "U", TipoSuscriptor.USUARIO, 1L);
    s.renovar();
    assertEquals(LocalDate.now(), s.getFechaInicio());
    assertEquals(EstadoSuscripcion.ACTIVA, s.getEstado());
}

@Test
void rehidratarConPlanProgramadoDebeConservarlo() {
    Suscripcion s = new Suscripcion(1L, "U", TipoSuscriptor.USUARIO, 1L);
    s.rehidratar(EstadoSuscripcion.ACTIVA, LocalDate.now(),
            LocalDate.now().plusMonths(1), true, "PRE-1", 2L);
    assertEquals(2L, s.getPlanProgramadoId());
    assertEquals("PRE-1", s.getPreapprovalIdMp());
}

@Test
void aplicarCambioPlanSinPlanProgramadoNoCambia() {
    Suscripcion s = new Suscripcion(1L, "U", TipoSuscriptor.USUARIO, 1L);
    s.aplicarCambioPlan();
    assertEquals(1L, s.getPlanId());
}
}