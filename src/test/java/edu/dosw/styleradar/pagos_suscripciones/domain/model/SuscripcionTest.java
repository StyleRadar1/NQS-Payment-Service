package edu.dosw.styleradar.pagos_suscripciones.domain.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class SuscripcionTest {

    @Test
    void debeIniciarEnEstadoPendiente() {
        Suscripcion sub = new Suscripcion(1L, "USER-1", TipoSuscriptor.USUARIO, 1L);
        assertEquals(EstadoSuscripcion.PENDIENTE, sub.getEstado());
        assertTrue(sub.isAutoRenovacion());
    }

    @Test
    void debeActivarSuscripcion() {
        Suscripcion sub = new Suscripcion(1L, "USER-1", TipoSuscriptor.USUARIO, 1L);
        LocalDate fechaFin = LocalDate.now().plusMonths(1);
        
        sub.activar(fechaFin);
        
        assertEquals(EstadoSuscripcion.ACTIVA, sub.getEstado());
        assertEquals(fechaFin, sub.getFechaFin());
        assertEquals(LocalDate.now(), sub.getFechaInicio());
    }

    @Test
    void debeSuspenderPorFaltaDePago() {
        Suscripcion sub = new Suscripcion(1L, "USER-1", TipoSuscriptor.USUARIO, 1L);
        sub.suspenderPorFaltaDePago();
        
        assertEquals(EstadoSuscripcion.SUSPENDIDA, sub.getEstado());
        assertFalse(sub.isAutoRenovacion());
    }

    @Test
    void debeCancelarRenovacion() {
        Suscripcion sub = new Suscripcion(1L, "USER-1", TipoSuscriptor.USUARIO, 1L);
        sub.cancelarRenovacion();
        assertFalse(sub.isAutoRenovacion());
    }

    @Test
    void debeDetectarSuscripcionVencida() {
        Suscripcion sub = new Suscripcion(1L, "USER-1", TipoSuscriptor.USUARIO, 1L);
        sub.activar(LocalDate.now().minusDays(1)); // Venció ayer
        
        assertTrue(sub.estaVencida());
    }

    @Test
    void debeProgramarYAplicarCambioDePlan() {
        Suscripcion sub = new Suscripcion(1L, "USER-1", TipoSuscriptor.USUARIO, 1L);
        
        sub.programarCambioPlan(2L);
        assertEquals(2L, sub.getPlanProgramadoId());
        assertEquals(1L, sub.getPlanId()); // Aún no cambia
        
        sub.aplicarCambioPlan();
        assertEquals(2L, sub.getPlanId()); // Ahora sí
        assertNull(sub.getPlanProgramadoId());
    }

    @Test
    void debePermitirModificarEstadoYPreapproval() {
        Suscripcion sub = new Suscripcion(1L, "USER-1", TipoSuscriptor.USUARIO, 1L);
        
        sub.setEstado(EstadoSuscripcion.CANCELADA);
        sub.setPreapprovalIdMp("PRE-123");
        sub.setPlanId(3L);
        
        assertEquals(EstadoSuscripcion.CANCELADA, sub.getEstado());
        assertEquals("PRE-123", sub.getPreapprovalIdMp());
        assertEquals(3L, sub.getPlanId());
    }
}