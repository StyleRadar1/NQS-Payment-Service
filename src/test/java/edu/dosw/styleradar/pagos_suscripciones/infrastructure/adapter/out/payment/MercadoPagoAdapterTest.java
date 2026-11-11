package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.payment;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.Monto;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.Suscripcion;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.TipoSuscriptor;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MercadoPagoAdapterTest {

    @Mock private MercadoPagoFeignClient feignClient;
    @InjectMocks private MercadoPagoAdapter adapter;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(adapter, "accessToken", "TEST-TOKEN");
    }

    @Test
    void debeCrearPreferenciaPagoReal() {
        OrdenPago orden = new OrdenPago(1L, 1L, new Monto(BigDecimal.TEN, "COP"), "MP-123");
        Suscripcion suscripcion = new Suscripcion(1L, "USER-1", TipoSuscriptor.USUARIO, 2L);

        // Simulamos la respuesta exacta de Mercado Pago (Map<String, Object>)
        Map<String, Object> response = new HashMap<>();
        response.put("init_point", "http://mp.com/pay");
        response.put("id", "PRE-123");

        when(feignClient.crearPreapproval(anyString(), any())).thenReturn(response);

        Map<String, String> result = adapter.crearPreferenciaPago(orden, suscripcion, "test@test.com");

        assertEquals("http://mp.com/pay", result.get("init_point"));
        assertEquals("PRE-123", result.get("preapproval_id"));
    }

    @Test
    void debeManejarRespuestaNulaEnCrearPreferencia() {
        OrdenPago orden = new OrdenPago(1L, 1L, new Monto(BigDecimal.TEN, "COP"), "MP-123");
        Suscripcion suscripcion = new Suscripcion(1L, "USER-1", TipoSuscriptor.USUARIO, 2L);

        when(feignClient.crearPreapproval(anyString(), any())).thenReturn(new HashMap<>());

        Map<String, String> result = adapter.crearPreferenciaPago(orden, suscripcion, "test@test.com");

        assertEquals("", result.get("init_point")); // Cubre el null check
    }

    @Test
    void debeCancelarPreapprovalReal() {
        adapter.cancelarPreapproval("PRE-123");
        verify(feignClient).cancelarPreapproval(anyString(), eq("PRE-123"));
    }

    @Test
    void debeReembolsarPagoReal() {
        adapter.reembolsarPago("MP-123", "100.50");
        verify(feignClient).reembolsarPago(anyString(), eq("MP-123"), any());
    }
}