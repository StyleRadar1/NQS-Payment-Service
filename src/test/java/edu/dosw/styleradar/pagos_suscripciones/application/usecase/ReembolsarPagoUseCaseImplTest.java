package edu.dosw.styleradar.pagos_suscripciones.application.usecase;

import edu.dosw.styleradar.pagos_suscripciones.domain.exception.PaymentProcessingException;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.*;
import edu.dosw.styleradar.pagos_suscripciones.domain.port.out.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReembolsarPagoUseCaseImplTest {

    @Mock private OrdenPagoRepositoryPort ordenPagoRepository;
    @Mock private SuscripcionRepositoryPort suscripcionRepository;
    @Mock private PlanRepositoryPort planRepository;
    @Mock private MercadoPagoPort mercadoPagoPort;

    @InjectMocks private ReembolsarPagoUseCaseImpl useCase;

    @Test
    void debeReembolsarCorrectamente() {
        OrdenPago o = new OrdenPago(1L, 100L, new Monto(new BigDecimal("100"), "COP"), "MP-1");
        o.aprobar();
        when(ordenPagoRepository.buscarPorId(1L)).thenReturn(Optional.of(o));

        useCase.reembolsarPago(1L, "50", "motivo");

        verify(mercadoPagoPort).reembolsarPago("MP-1", "50");
        verify(ordenPagoRepository).guardar(o);
    }

    @Test
    void reembolsoTotalDebeCancelarSuscripcion() {
        OrdenPago o = new OrdenPago(1L, 100L, new Monto(new BigDecimal("100"), "COP"), "MP-1");
        o.aprobar();
        Suscripcion s = new Suscripcion(100L, "U", TipoSuscriptor.USUARIO, 2L);
        s.setPreapprovalIdMp("PRE-1");

        when(ordenPagoRepository.buscarPorId(1L)).thenReturn(Optional.of(o));
        when(suscripcionRepository.buscarPorId(100L)).thenReturn(Optional.of(s));

        useCase.reembolsarPago(1L, "100", "total");

        assertFalse(s.isAutoRenovacion());
        verify(mercadoPagoPort).cancelarPreapproval("PRE-1");
    }

    @Test
    void debeLanzarSiOrdenNoExiste() {
        when(ordenPagoRepository.buscarPorId(1L)).thenReturn(Optional.empty());
        assertThrows(PaymentProcessingException.class, () -> useCase.reembolsarPago(1L, "50", "m"));
    }

    @Test
    void reintentarDebeLanzarSiYaEstaActiva() {
        OrdenPago o = new OrdenPago(1L, 100L, new Monto(BigDecimal.TEN, "COP"), "MP-1");
        Suscripcion s = new Suscripcion(100L, "U", TipoSuscriptor.USUARIO, 2L);
        s.rehidratar(EstadoSuscripcion.ACTIVA, null, null, true, null, null);
        when(ordenPagoRepository.buscarPorId(1L)).thenReturn(Optional.of(o));
        when(suscripcionRepository.buscarPorId(100L)).thenReturn(Optional.of(s));

        assertThrows(IllegalStateException.class,
            () -> useCase.reintentarPago(1L, "U", "e@t.com"));
    }

    @Test
    void reintentarDebeCrearNuevaOrden() {
        OrdenPago o = new OrdenPago(1L, 100L, new Monto(BigDecimal.TEN, "COP"), "MP-1");
        o.rechazar();
        Suscripcion s = new Suscripcion(100L, "U", TipoSuscriptor.USUARIO, 2L);
        s.rehidratar(EstadoSuscripcion.PENDIENTE, null, null, true, null, null);
        when(ordenPagoRepository.buscarPorId(1L)).thenReturn(Optional.of(o));
        when(suscripcionRepository.buscarPorId(100L)).thenReturn(Optional.of(s));
        when(mercadoPagoPort.crearPreferenciaPago(any(), any(), anyString()))
            .thenReturn(java.util.Map.of("init_point", "https://mp.com/r", "preapproval_id", "PRE-N"));

        String init = useCase.reintentarPago(1L, "U", "e@t.com");

        assertEquals("https://mp.com/r", init);
        verify(ordenPagoRepository, times(1)).guardar(any(OrdenPago.class)); // ← antes times(2)
        verify(suscripcionRepository, never()).guardar(any()); // la suscripción NO se modifica
    }

    @Test
    void historialDebeRetornarLista() {
        when(ordenPagoRepository.buscarPorSuscriptorId("U")).thenReturn(List.of());
        assertTrue(useCase.consultarHistorialPagos("U").isEmpty());
    }

    @Test
void debeLanzarSiOrdenNoAprobadaNiReembolsada() {
    OrdenPago o = new OrdenPago(1L, 100L, new Monto(BigDecimal.TEN, "COP"), "MP-1");
    // estado PENDIENTE por defecto
    when(ordenPagoRepository.buscarPorId(1L)).thenReturn(Optional.of(o));

    assertThrows(IllegalStateException.class,
            () -> useCase.reembolsarPago(1L, "5", "motivo"));
}

@Test
void debeReembolsarParcialSinCancelarSuscripcion() {
    OrdenPago o = new OrdenPago(1L, 100L, new Monto(new BigDecimal("100"), "COP"), "MP-1");
    o.aprobar();
    when(ordenPagoRepository.buscarPorId(1L)).thenReturn(Optional.of(o));

    useCase.reembolsarPago(1L, "30", "parcial");

    verify(suscripcionRepository, never()).buscarPorId(any());
    verify(mercadoPagoPort, never()).cancelarPreapproval(any());
}

@Test
void debeLanzarSiReintentoNoEsDelMismoSuscriptor() {
    OrdenPago o = new OrdenPago(1L, 100L, new Monto(BigDecimal.TEN, "COP"), "MP-1");
    o.rechazar();
    Suscripcion s = new Suscripcion(100L, "OTRO", TipoSuscriptor.USUARIO, 2L);
    s.rehidratar(EstadoSuscripcion.PENDIENTE, null, null, true, null, null);
    when(ordenPagoRepository.buscarPorId(1L)).thenReturn(Optional.of(o));
    when(suscripcionRepository.buscarPorId(100L)).thenReturn(Optional.of(s));

    assertThrows(SecurityException.class,
            () -> useCase.reintentarPago(1L, "U", "e@t.com"));
}

@Test
void reintentarDebeLanzarSiOrdenNoEsRechazadaNiPendiente() {
    OrdenPago o = new OrdenPago(1L, 100L, new Monto(BigDecimal.TEN, "COP"), "MP-1");
    o.aprobar();
    Suscripcion s = new Suscripcion(100L, "U", TipoSuscriptor.USUARIO, 2L);
    s.rehidratar(EstadoSuscripcion.PENDIENTE, null, null, true, null, null);
    when(ordenPagoRepository.buscarPorId(1L)).thenReturn(Optional.of(o));
    when(suscripcionRepository.buscarPorId(100L)).thenReturn(Optional.of(s));

    assertThrows(IllegalStateException.class,
            () -> useCase.reintentarPago(1L, "U", "e@t.com"));
}

@Test
void reintentarDebeLanzarSiSuscripcionNoExiste() {
    OrdenPago o = new OrdenPago(1L, 100L, new Monto(BigDecimal.TEN, "COP"), "MP-1");
    o.rechazar();
    when(ordenPagoRepository.buscarPorId(1L)).thenReturn(Optional.of(o));
    when(suscripcionRepository.buscarPorId(100L)).thenReturn(Optional.empty());

    assertThrows(PaymentProcessingException.class,
            () -> useCase.reintentarPago(1L, "U", "e@t.com"));
}
}