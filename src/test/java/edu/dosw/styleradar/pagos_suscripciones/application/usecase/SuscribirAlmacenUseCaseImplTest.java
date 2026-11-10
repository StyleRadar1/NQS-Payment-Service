package edu.dosw.styleradar.pagos_suscripciones.application.usecase;

import edu.dosw.styleradar.pagos_suscripciones.domain.exception.PlanNotFoundException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuscribirAlmacenUseCaseImplTest {

    @Mock private PlanRepositoryPort planRepository;
    @Mock private SuscripcionRepositoryPort suscripcionRepository;
    @Mock private OrdenPagoRepositoryPort ordenPagoRepository;
    @Mock private MercadoPagoPort mercadoPagoPort;

    @InjectMocks
    private SuscribirAlmacenUseCaseImpl useCase;

    @Test
    void debeLanzarExcepcionSiPlanNoExiste() {
        when(planRepository.buscarPorId(99L)).thenReturn(Optional.empty());

        assertThrows(PlanNotFoundException.class, () -> 
            useCase.iniciarSuscripcion("USER-1", 99L, TipoSuscriptor.USUARIO, "test@test.com")
        );
        verifyNoInteractions(suscripcionRepository, ordenPagoRepository, mercadoPagoPort);
    }

    @Test
    void debeIniciarSuscripcionCorrectamente() {
        // Arrange
        Plan plan = new Plan(2L, "Radar+", TipoSuscriptor.USUARIO, new Monto(BigDecimal.valueOf(14900), "COP"), Map.of());
        Suscripcion suscripcionGuardada = new Suscripcion(100L, "USER-1", TipoSuscriptor.USUARIO, 2L);
        
        when(planRepository.buscarPorId(2L)).thenReturn(Optional.of(plan));
        when(suscripcionRepository.guardar(any(Suscripcion.class))).thenReturn(suscripcionGuardada);
        when(mercadoPagoPort.crearPreferenciaPago(any(), any(), anyString()))
            .thenReturn(Map.of("init_point", "https://mp.com/pay", "preapproval_id", "PRE-123"));

        // Act
        String initPoint = useCase.iniciarSuscripcion("USER-1", 2L, TipoSuscriptor.USUARIO, "test@test.com");

        // Assert
        assertEquals("https://mp.com/pay", initPoint);
        verify(ordenPagoRepository, times(1)).guardar(any(OrdenPago.class));
        verify(suscripcionRepository, times(2)).guardar(any(Suscripcion.class)); // 1 creación + 1 actualización
        verify(mercadoPagoPort, times(1)).crearPreferenciaPago(any(), any(), eq("test@test.com"));
    }
}