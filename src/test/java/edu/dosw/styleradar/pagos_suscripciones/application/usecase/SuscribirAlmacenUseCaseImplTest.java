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

    @Test
    void debeLanzarExcepcionSiPlanNoExisteAlIniciar() {
        when(planRepository.buscarPorId(99L)).thenReturn(Optional.empty());
        assertThrows(PlanNotFoundException.class, () -> 
            useCase.iniciarSuscripcion("USER-1", 99L, TipoSuscriptor.USUARIO, "test@test.com"));
    }

    @Test
    void debeLanzarExcepcionSiPlanInactivo() {
        Plan plan = new Plan(2L, "Radar+", TipoSuscriptor.USUARIO, new Monto(BigDecimal.TEN, "COP"), Map.of());
        plan.desactivar();
        when(planRepository.buscarPorId(2L)).thenReturn(Optional.of(plan));

        assertThrows(IllegalArgumentException.class, () -> 
            useCase.iniciarSuscripcion("USER-1", 2L, TipoSuscriptor.USUARIO, "test@test.com"));
    }

    @Test
    void debeIniciarSuscripcionCorrectamente() {
        Plan plan = new Plan(2L, "Radar+", TipoSuscriptor.USUARIO, new Monto(BigDecimal.TEN, "COP"), Map.of());
        Suscripcion suscripcionGuardada = new Suscripcion(100L, "USER-1", TipoSuscriptor.USUARIO, 2L);
        
        when(planRepository.buscarPorId(2L)).thenReturn(Optional.of(plan));
        when(suscripcionRepository.guardar(any(Suscripcion.class))).thenReturn(suscripcionGuardada);
        when(mercadoPagoPort.crearPreferenciaPago(any(), any(), anyString()))
            .thenReturn(Map.of("init_point", "https://mp.com/pay", "preapproval_id", "PRE-123"));

        String initPoint = useCase.iniciarSuscripcion("USER-1", 2L, TipoSuscriptor.USUARIO, "test@test.com");

        assertEquals("https://mp.com/pay", initPoint);
        verify(ordenPagoRepository, times(1)).guardar(any(OrdenPago.class));
        verify(suscripcionRepository, times(2)).guardar(any(Suscripcion.class)); 
    }

    @Test
    void debeLanzarExcepcionAlCambiarPlanSiPlanNoExiste() {
        when(planRepository.buscarPorId(99L)).thenReturn(Optional.empty());
        assertThrows(PlanNotFoundException.class, () -> useCase.programarCambioPlan("USER-1", 99L));
    }

    @Test
    void debeLanzarExcepcionAlCambiarPlanSiSuscripcionNoExiste() {
        when(planRepository.buscarPorId(2L)).thenReturn(Optional.of(new Plan(2L, "Radar+", TipoSuscriptor.USUARIO, new Monto(BigDecimal.TEN, "COP"), Map.of())));
        when(suscripcionRepository.buscarPorSuscriptorId("USER-1")).thenReturn(Optional.empty());

        assertThrows(SuscripcionNoEncontradaException.class, () -> useCase.programarCambioPlan("USER-1", 2L));
    }

    @Test
    void debeProgramarCambioDePlanExitosamente() {
        Suscripcion suscripcion = new Suscripcion(1L, "USER-1", TipoSuscriptor.USUARIO, 1L);
        Plan nuevoPlan = new Plan(2L, "Radar+", TipoSuscriptor.USUARIO, new Monto(BigDecimal.TEN, "COP"), Map.of());

        when(planRepository.buscarPorId(2L)).thenReturn(Optional.of(nuevoPlan));
        when(suscripcionRepository.buscarPorSuscriptorId("USER-1")).thenReturn(Optional.of(suscripcion));

        useCase.programarCambioPlan("USER-1", 2L);

        assertEquals(2L, suscripcion.getPlanProgramadoId());
        verify(suscripcionRepository).guardar(suscripcion);
    }

        @Test
    void debeCancelarRenovacionExitosamente() {
        Suscripcion suscripcion = new Suscripcion(1L, "USER-1", TipoSuscriptor.USUARIO, 1L);
        suscripcion.setPreapprovalIdMp("PRE-123");
        when(suscripcionRepository.buscarPorSuscriptorId("USER-1")).thenReturn(Optional.of(suscripcion));

        useCase.cancelarRenovacion("USER-1");

        assertFalse(suscripcion.isAutoRenovacion());
        verify(mercadoPagoPort).cancelarPreapproval("PRE-123");
        verify(suscripcionRepository).guardar(suscripcion);
    }

    @Test
    void debeConsultarSuscripcionExitosamente() {
        Suscripcion suscripcion = new Suscripcion(1L, "USER-1", TipoSuscriptor.USUARIO, 1L);
        when(suscripcionRepository.buscarPorSuscriptorId("USER-1")).thenReturn(Optional.of(suscripcion));

        Suscripcion result = useCase.consultarSuscripcion("USER-1");

        assertNotNull(result);
        assertEquals("USER-1", result.getSuscriptorId());
    }
}