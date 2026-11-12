package edu.dosw.styleradar.pagos_suscripciones.application.service;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.*;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.MercadoPagoPort;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.OrdenPagoRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConciliacionSchedulerTest {

    @Mock private OrdenPagoRepositoryPort ordenPagoRepository;
    @Mock private MercadoPagoPort mercadoPagoPort;
    @InjectMocks private ConciliacionScheduler scheduler;
    @Mock private SuscripcionActivacionService suscripcionActivacionService;

    @Test
    void debeActualizarSiMPTieneEstadoDistinto() {
        OrdenPago o = new OrdenPago(1L, 100L, new Monto(BigDecimal.TEN, "COP"), "MP-1");
        when(ordenPagoRepository.buscarPendientesAntesDe(any(LocalDateTime.class)))
            .thenReturn(List.of(o));
        when(mercadoPagoPort.consultarEstadoPago("MP-1")).thenReturn("approved");

        scheduler.conciliarPagosPendientes();

        verify(ordenPagoRepository).guardar(o);
    }

    @Test
    void noDebeActualizarSiEstadoCoincide() {
        OrdenPago o = new OrdenPago(1L, 100L, new Monto(BigDecimal.TEN, "COP"), "MP-1");
        when(ordenPagoRepository.buscarPendientesAntesDe(any(LocalDateTime.class)))
            .thenReturn(List.of(o));
        when(mercadoPagoPort.consultarEstadoPago("MP-1")).thenReturn("pending");

        scheduler.conciliarPagosPendientes();

        verify(ordenPagoRepository, never()).guardar(any());
    }
    @Test
void debeActivarSuscripcionSiMPConfirmaApproved() {
    OrdenPago o = new OrdenPago(1L, 100L, new Monto(BigDecimal.TEN, "COP"), "MP-1");
    when(ordenPagoRepository.buscarPendientesAntesDe(any(LocalDateTime.class)))
            .thenReturn(List.of(o));
    when(mercadoPagoPort.consultarEstadoPago("MP-1")).thenReturn("approved");

    scheduler.conciliarPagosPendientes();

    verify(ordenPagoRepository).guardar(o);
    // FIX P0: la conciliación también activa la suscripción
    verify(suscripcionActivacionService).activarYExtenderSuscripcion(o);
}

@Test
void debeSaltarOrdenSinIdPagoExterno() {
    OrdenPago o = new OrdenPago(1L, 100L, new Monto(BigDecimal.TEN, "COP"), null);
    when(ordenPagoRepository.buscarPendientesAntesDe(any(LocalDateTime.class)))
            .thenReturn(List.of(o));

    scheduler.conciliarPagosPendientes();

    verifyNoInteractions(mercadoPagoPort);
    verify(suscripcionActivacionService, never()).activarYExtenderSuscripcion(any());
}

@Test
void debeCapturarExcepcionYContinuar() {
    OrdenPago o = new OrdenPago(1L, 100L, new Monto(BigDecimal.TEN, "COP"), "MP-1");
    when(ordenPagoRepository.buscarPendientesAntesDe(any(LocalDateTime.class)))
            .thenReturn(List.of(o));
    when(mercadoPagoPort.consultarEstadoPago("MP-1"))
            .thenThrow(new RuntimeException("timeout"));

    scheduler.conciliarPagosPendientes(); // no debe lanzar

    verify(ordenPagoRepository, never()).guardar(any());
}

@Test
void debeIgnorarEstadoDesconocido() {
    OrdenPago o = new OrdenPago(1L, 100L, new Monto(BigDecimal.TEN, "COP"), "MP-1");
    when(ordenPagoRepository.buscarPendientesAntesDe(any(LocalDateTime.class)))
            .thenReturn(List.of(o));
    when(mercadoPagoPort.consultarEstadoPago("MP-1")).thenReturn("weird_status");

    scheduler.conciliarPagosPendientes();

    verify(ordenPagoRepository, never()).guardar(any());
}
}