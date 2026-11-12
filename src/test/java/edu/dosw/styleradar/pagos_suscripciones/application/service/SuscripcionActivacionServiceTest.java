package edu.dosw.styleradar.pagos_suscripciones.application.service;

import edu.dosw.styleradar.pagos_suscripciones.domain.exception.PaymentProcessingException;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuscripcionActivacionServiceTest {

    @Mock private SuscripcionRepositoryPort suscripcionRepository;
    @Mock private PlanRepositoryPort planRepository;
    @Mock private EventPublisherPort eventPublisher;

    @InjectMocks private SuscripcionActivacionService service;

    private OrdenPago orden() {
        return new OrdenPago(1L, 100L, new Monto(new BigDecimal("14900"), "COP"), "MP-1");
    }

    @Test
void debeLanzarSiSuscripcionNoExiste() {
    OrdenPago o = orden();                          // preparar fuera
    when(suscripcionRepository.buscarPorId(100L)).thenReturn(Optional.empty());

    assertThrows(PaymentProcessingException.class,
            () -> service.activarYExtenderSuscripcion(o));   // una sola invocación
}

    @Test
void debeActivarSiEstabaPendiente() {
    OrdenPago o = orden();
    Suscripcion s = new Suscripcion(100L, "U", TipoSuscriptor.USUARIO, 2L);
    Plan p = new Plan(2L, "Radar+", TipoSuscriptor.USUARIO,
            new Monto(new BigDecimal("14900"), "COP"), Map.of("max", -1));
    when(suscripcionRepository.buscarPorId(100L)).thenReturn(Optional.of(s));
    when(planRepository.buscarPorId(2L)).thenReturn(Optional.of(p));

    service.activarYExtenderSuscripcion(o);

    verify(suscripcionRepository).guardar(any(Suscripcion.class));
    verify(eventPublisher).publicarPagoConfirmado(any());
}

@Test
void debeRenovarSiYaEstabaActiva() {
    OrdenPago o = orden();
    Suscripcion s = new Suscripcion(100L, "U", TipoSuscriptor.USUARIO, 2L);
    s.rehidratar(EstadoSuscripcion.ACTIVA, LocalDate.now(),
            LocalDate.now().plusDays(10), true, null, null);
    when(suscripcionRepository.buscarPorId(100L)).thenReturn(Optional.of(s));
    when(planRepository.buscarPorId(2L)).thenReturn(Optional.empty());

    service.activarYExtenderSuscripcion(o);

    verify(suscripcionRepository).guardar(s);
    verify(eventPublisher).publicarPagoConfirmado(any());
}
}