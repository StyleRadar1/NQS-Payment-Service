package edu.dosw.styleradar.pagos_suscripciones.application.usecase;

import edu.dosw.styleradar.pagos_suscripciones.domain.exception.PlanNotFoundException;
import edu.dosw.styleradar.pagos_suscripciones.domain.exception.SuscripcionNoEncontradaException;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.*;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuscribirAlmacenUseCaseImplTest {

    @Mock private PlanRepositoryPort planRepository;
    @Mock private SuscripcionRepositoryPort suscripcionRepository;
    @Mock private OrdenPagoRepositoryPort ordenPagoRepository;
    @Mock private MercadoPagoPort mercadoPagoPort;

    @InjectMocks private SuscribirAlmacenUseCaseImpl useCase;

    private Plan plan(Long id, TipoSuscriptor tipo, BigDecimal precio) {
        return new Plan(id, "Plan" + id, tipo, new Monto(precio, "COP"), Map.of());
    }

    @Test
    void debeLanzarSiPlanNoExiste() {
        when(planRepository.buscarPorId(99L)).thenReturn(Optional.empty());
        assertThrows(PlanNotFoundException.class,
            () -> useCase.iniciarSuscripcion("U", 99L, TipoSuscriptor.USUARIO, "e@t.com"));
    }

    @Test
    void debeLanzarSiPlanInactivo() {
        Plan p = plan(2L, TipoSuscriptor.USUARIO, BigDecimal.TEN);
        p.desactivar();
        when(planRepository.buscarPorId(2L)).thenReturn(Optional.of(p));
        assertThrows(IllegalArgumentException.class,
            () -> useCase.iniciarSuscripcion("U", 2L, TipoSuscriptor.USUARIO, "e@t.com"));
    }

    @Test
    void debeLanzarSiTipoSuscriptorDifiere() {
        Plan p = plan(2L, TipoSuscriptor.USUARIO, BigDecimal.TEN);
        when(planRepository.buscarPorId(2L)).thenReturn(Optional.of(p));
        assertThrows(IllegalArgumentException.class,
            () -> useCase.iniciarSuscripcion("U", 2L, TipoSuscriptor.ALMACEN, "e@t.com"));
    }

    @Test
    void debeLanzarSiYaExisteSuscripcionActiva() {
        Plan p = plan(2L, TipoSuscriptor.USUARIO, BigDecimal.TEN);
        Suscripcion existente = new Suscripcion(10L, "U", TipoSuscriptor.USUARIO, 2L);
        existente.rehidratar(EstadoSuscripcion.ACTIVA, LocalDate.now(), LocalDate.now().plusDays(10), true, null, null);

        when(planRepository.buscarPorId(2L)).thenReturn(Optional.of(p));
        when(suscripcionRepository.buscarPorSuscriptorId("U")).thenReturn(Optional.of(existente));

        assertThrows(IllegalStateException.class,
            () -> useCase.iniciarSuscripcion("U", 2L, TipoSuscriptor.USUARIO, "e@t.com"));
    }

    @Test
    void debeCrearSuscripcionCuandoEsNueva() {
        Plan p = plan(2L, TipoSuscriptor.USUARIO, BigDecimal.TEN);
        Suscripcion guardada = new Suscripcion(100L, "U", TipoSuscriptor.USUARIO, 2L);

        when(planRepository.buscarPorId(2L)).thenReturn(Optional.of(p));
        when(suscripcionRepository.buscarPorSuscriptorId("U")).thenReturn(Optional.empty());
        when(suscripcionRepository.guardar(any())).thenReturn(guardada);
        when(mercadoPagoPort.crearPreferenciaPago(any(), any(), anyString()))
            .thenReturn(Map.of("init_point", "https://mp.com/x", "preapproval_id", "PRE-123"));

        String init = useCase.iniciarSuscripcion("U", 2L, TipoSuscriptor.USUARIO, "e@t.com");

        assertEquals("https://mp.com/x", init);
        verify(ordenPagoRepository).guardar(any(OrdenPago.class));
    }

    @Test
    void upgradeDebeCrearOrdenPorDiferenciaYDevolverInitPoint() {
        Plan actual = plan(1L, TipoSuscriptor.USUARIO, new BigDecimal("10000"));
        Plan nuevo  = plan(2L, TipoSuscriptor.USUARIO, new BigDecimal("20000"));
        Suscripcion s = new Suscripcion(1L, "U", TipoSuscriptor.USUARIO, 1L);
        s.rehidratar(EstadoSuscripcion.ACTIVA, LocalDate.now(), LocalDate.now().plusMonths(1), true, "PRE-1", null);

        when(suscripcionRepository.buscarPorSuscriptorId("U")).thenReturn(Optional.of(s));
        when(planRepository.buscarPorId(1L)).thenReturn(Optional.of(actual));
        when(planRepository.buscarPorId(2L)).thenReturn(Optional.of(nuevo));
        when(mercadoPagoPort.crearPreferenciaPago(any(), any(), anyString()))
            .thenReturn(Map.of("init_point", "https://mp.com/upg", "preapproval_id", "PRE-2"));

        String init = useCase.programarCambioPlan("U", 2L, "e@t.com");

        assertEquals("https://mp.com/upg", init);
        assertEquals(2L, s.getPlanProgramadoId());
        verify(ordenPagoRepository, times(1)).guardar(any(OrdenPago.class));
    }

    @Test
    void downgradeDebeProgramarSinCobroYDevolverNull() {
        Plan actual = plan(2L, TipoSuscriptor.USUARIO, new BigDecimal("20000"));
        Plan nuevo  = plan(1L, TipoSuscriptor.USUARIO, new BigDecimal("10000"));
        Suscripcion s = new Suscripcion(1L, "U", TipoSuscriptor.USUARIO, 2L);
        s.rehidratar(EstadoSuscripcion.ACTIVA, LocalDate.now(), LocalDate.now().plusMonths(1), true, "PRE-1", null);

        when(suscripcionRepository.buscarPorSuscriptorId("U")).thenReturn(Optional.of(s));
        when(planRepository.buscarPorId(2L)).thenReturn(Optional.of(actual));
        when(planRepository.buscarPorId(1L)).thenReturn(Optional.of(nuevo));

        String init = useCase.programarCambioPlan("U", 1L, "e@t.com");

        assertNull(init);
        assertEquals(1L, s.getPlanProgramadoId());
        verify(ordenPagoRepository, never()).guardar(any());
        verify(mercadoPagoPort, never()).crearPreferenciaPago(any(), any(), any());
    }

    @Test
    void debeRechazarCambioSiTipoSuscriptorDifiere() {
        Plan actual = plan(1L, TipoSuscriptor.USUARIO, BigDecimal.TEN);
        Plan nuevo  = plan(3L, TipoSuscriptor.ALMACEN, BigDecimal.TEN);
        Suscripcion s = new Suscripcion(1L, "U", TipoSuscriptor.USUARIO, 1L);
        s.rehidratar(EstadoSuscripcion.ACTIVA, LocalDate.now(), LocalDate.now().plusMonths(1), true, null, null);

        when(suscripcionRepository.buscarPorSuscriptorId("U")).thenReturn(Optional.of(s));
        when(planRepository.buscarPorId(1L)).thenReturn(Optional.of(actual));
        when(planRepository.buscarPorId(3L)).thenReturn(Optional.of(nuevo));

        assertThrows(IllegalArgumentException.class,
            () -> useCase.programarCambioPlan("U", 3L, "e@t.com"));
    }

    @Test
    void cancelarRenovacionDebeSerIdempotente() {
        Suscripcion s = new Suscripcion(1L, "U", TipoSuscriptor.USUARIO, 1L);
        s.setPreapprovalIdMp("PRE-1");
        s.cancelarRenovacion(); // ya cancelada
        when(suscripcionRepository.buscarPorSuscriptorId("U")).thenReturn(Optional.of(s));

        useCase.cancelarRenovacion("U");

        verify(mercadoPagoPort, never()).cancelarPreapproval(anyString());
        verify(suscripcionRepository, never()).guardar(any());
    }

    @Test
void debeLanzarSiSuscripcionExistenteEstaPendiente() {
    Plan p = plan(2L, TipoSuscriptor.USUARIO, BigDecimal.TEN);
    Suscripcion ex = new Suscripcion(10L, "U", TipoSuscriptor.USUARIO, 2L);
    ex.rehidratar(EstadoSuscripcion.PENDIENTE, null, null, true, null, null);

    when(planRepository.buscarPorId(2L)).thenReturn(Optional.of(p));
    when(suscripcionRepository.buscarPorSuscriptorId("U")).thenReturn(Optional.of(ex));

    assertThrows(IllegalStateException.class,
            () -> useCase.iniciarSuscripcion("U", 2L, TipoSuscriptor.USUARIO, "e@t.com"));
}

@Test
void cancelarRenovacionDebeLanzarSiNoExiste() {
    when(suscripcionRepository.buscarPorSuscriptorId("U")).thenReturn(Optional.empty());
    assertThrows(SuscripcionNoEncontradaException.class,
            () -> useCase.cancelarRenovacion("U"));
}

@Test
void cambiarPlanDebeLanzarSiSuscripcionNoEstaActiva() {
    Suscripcion s = new Suscripcion(1L, "U", TipoSuscriptor.USUARIO, 1L);
    s.rehidratar(EstadoSuscripcion.PENDIENTE, null, null, true, null, null);
    when(suscripcionRepository.buscarPorSuscriptorId("U")).thenReturn(Optional.of(s));

    assertThrows(IllegalStateException.class,
            () -> useCase.programarCambioPlan("U", 2L, "e@t.com"));
}

@Test
void cambiarPlanDebeLanzarSiMismoPlan() {
    Plan actual = plan(1L, TipoSuscriptor.USUARIO, BigDecimal.TEN);
    Suscripcion s = new Suscripcion(1L, "U", TipoSuscriptor.USUARIO, 1L);
    s.rehidratar(EstadoSuscripcion.ACTIVA, LocalDate.now(), LocalDate.now().plusMonths(1), true, null, null);

    when(suscripcionRepository.buscarPorSuscriptorId("U")).thenReturn(Optional.of(s));
    when(planRepository.buscarPorId(1L)).thenReturn(Optional.of(actual));

    assertThrows(IllegalArgumentException.class,
            () -> useCase.programarCambioPlan("U", 1L, "e@t.com"));
}

@Test
void cambiarPlanDebeLanzarSiNuevoPlanNoActivo() {
    Plan actual = plan(1L, TipoSuscriptor.USUARIO, new BigDecimal("100"));
    Plan nuevo = plan(2L, TipoSuscriptor.USUARIO, new BigDecimal("200"));
    nuevo.desactivar();
    Suscripcion s = new Suscripcion(1L, "U", TipoSuscriptor.USUARIO, 1L);
    s.rehidratar(EstadoSuscripcion.ACTIVA, LocalDate.now(), LocalDate.now().plusMonths(1), true, null, null);

    when(suscripcionRepository.buscarPorSuscriptorId("U")).thenReturn(Optional.of(s));
    when(planRepository.buscarPorId(1L)).thenReturn(Optional.of(actual));
    when(planRepository.buscarPorId(2L)).thenReturn(Optional.of(nuevo));

    assertThrows(IllegalArgumentException.class,
            () -> useCase.programarCambioPlan("U", 2L, "e@t.com"));
}

}