package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.payment;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.Monto;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.Suscripcion;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.TipoSuscriptor;
import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MercadoPagoAdapterTest {

    @Mock private MercadoPagoFeignClient feignClient;
    @InjectMocks private MercadoPagoAdapter adapter;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adapter, "accessToken", "TEST-TOKEN");
        ReflectionTestUtils.setField(adapter, "webhookUrl", "https://ngrok/webhook");
    }

    @Test
    void debeCrearPreferenciaPagoReal() {
        OrdenPago orden = new OrdenPago(1L, 1L, new Monto(BigDecimal.TEN, "COP"), "MP-123");
        Suscripcion sub = new Suscripcion(1L, "USER-1", TipoSuscriptor.USUARIO, 2L);
        Map<String, Object> resp = new HashMap<>();
        resp.put("init_point", "http://mp.com/pay");
        resp.put("id", "PRE-123");
        when(feignClient.crearPreapproval(anyString(), any())).thenReturn(resp);

        Map<String, String> result = adapter.crearPreferenciaPago(orden, sub, "test@test.com");
        assertEquals("http://mp.com/pay", result.get("init_point"));
    }

    @Test
    void debeLanzarExcepcionSiMpDevuelveNullEnCrearPreferencia() {
        OrdenPago orden = new OrdenPago(1L, 1L, new Monto(BigDecimal.TEN, "COP"), "MP-1");
        Suscripcion sub = new Suscripcion(1L, "USER-1", TipoSuscriptor.USUARIO, 2L);
        when(feignClient.crearPreapproval(anyString(), any())).thenReturn(null);

        assertThrows(RuntimeException.class, () -> adapter.crearPreferenciaPago(orden, sub, "e@t.com"));
    }

    @Test
    void debeCancelarPreapproval() {
        adapter.cancelarPreapproval("PRE-123");
        verify(feignClient).cancelarPreapproval(anyString(), eq("PRE-123"));
    }

    @Test
    void debeReembolsarPago() {
        adapter.reembolsarPago("MP-123", "100.50");
        verify(feignClient).reembolsarPago(anyString(), eq("MP-123"), any());
    }

    @Test
    void consultarEstadoPagoDebeRetornarStatusDeMP() {
        Map<String, Object> pago = new HashMap<>();
        pago.put("status", "approved");
        when(feignClient.consultarPago(anyString(), eq("MP-123"))).thenReturn(pago);
        assertEquals("approved", adapter.consultarEstadoPago("MP-123"));
    }

    @Test
    void consultarEstadoPagoDebeRetornarNullSiRespuestaNula() {
        when(feignClient.consultarPago(anyString(), eq("MP-123"))).thenReturn(null);
        assertNull(adapter.consultarEstadoPago("MP-123"));
    }

    @Test
    void consultarPagoDebeLanzarExcepcionSiFeignFallaConNotFound() {
        // Cubre el catch (FeignException.NotFound e)
        when(feignClient.consultarPago(anyString(), eq("MP-123")))
                .thenThrow(mock(FeignException.NotFound.class));
        assertThrows(RuntimeException.class, () -> adapter.consultarPago("MP-123"));
    }

    @Test
    void consultarPagoDebeLanzarExcepcionSiFeignFallaGenerico() {
        // Cubre el catch (Exception e)
        when(feignClient.consultarPago(anyString(), eq("MP-123")))
                .thenThrow(new RuntimeException("timeout"));
        assertThrows(RuntimeException.class, () -> adapter.consultarPago("MP-123"));
    }

    @Test
    void consultarPagoAutorizadoDebeLanzarExcepcionSiFeignFalla() {
        // Cubre el catch (Exception e) en consultarPagoAutorizado
        when(feignClient.consultarPagoAutorizado(anyString(), eq("AUT-123")))
                .thenThrow(new RuntimeException("timeout"));
        assertThrows(RuntimeException.class, () -> adapter.consultarPagoAutorizado("AUT-123"));
    }

    @Test
    void debeConsultarPagoCompleto() {
        Map<String, Object> pago = new HashMap<>();
        pago.put("status", "approved");
        pago.put("external_reference", "1");
        when(feignClient.consultarPago(anyString(), eq("MP-123"))).thenReturn(pago);
        Map<String, Object> result = adapter.consultarPago("MP-123");
        assertNotNull(result);
    }

    @Test
    void debeConsultarPagoAutorizado() {
        Map<String, Object> pago = new HashMap<>();
        pago.put("status", "approved");
        pago.put("external_reference", "1");
        when(feignClient.consultarPagoAutorizado(anyString(), eq("AUT-123"))).thenReturn(pago);
        Map<String, Object> result = adapter.consultarPagoAutorizado("AUT-123");
        assertNotNull(result);
    }
}