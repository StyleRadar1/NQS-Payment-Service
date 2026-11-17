package edu.dosw.styleradar.pagos.infrastructure.adapter.out.payment;

import edu.dosw.styleradar.pagos.application.port.out.MercadoPagoPort;
import edu.dosw.styleradar.pagos.domain.exception.PaymentProcessingException;
import edu.dosw.styleradar.pagos.domain.model.Monto;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.payment.dto.PaymentResponse;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.payment.dto.PreapprovalResponse;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.payment.dto.RefundResponse;
import feign.FeignException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("MercadoPagoAdapter (SR-RF-93/94/95/98)")
class MercadoPagoAdapterTest {

    private final MercadoPagoFeignClient feign = mock(MercadoPagoFeignClient.class);

    private MercadoPagoPort.PreapprovalSolicitud solicitud() {
        return new MercadoPagoPort.PreapprovalSolicitud("Suscripción Radar+", "SUS-1",
                "pagador@testuser.com", Monto.cop(new BigDecimal("14900.00")), "https://app/back");
    }

    @Test
    void creaPreapprovalYUsaSandboxConTokenDePrueba() {
        when(feign.crearPreapproval(eq("Bearer TEST-123"), any()))
                .thenReturn(new PreapprovalResponse("PRE-1", "https://prod/init",
                        "https://sandbox/init", "pending", "SUS-1"));
        MercadoPagoAdapter adapter = new MercadoPagoAdapter(feign, "TEST-123");

        MercadoPagoPort.PreapprovalCreado creado = adapter.crearPreapproval(solicitud());

        assertThat(creado.id()).isEqualTo("PRE-1");
        assertThat(creado.initPoint()).isEqualTo("https://sandbox/init");
    }

    @Test
    void creaPreapprovalYUsaInitPointRealEnProduccion() {
        when(feign.crearPreapproval(eq("Bearer APP-123"), any()))
                .thenReturn(new PreapprovalResponse("PRE-1", "https://prod/init",
                        "https://sandbox/init", "pending", "SUS-1"));
        MercadoPagoAdapter adapter = new MercadoPagoAdapter(feign, "APP-123");

        assertThat(adapter.crearPreapproval(solicitud()).initPoint()).isEqualTo("https://prod/init");
    }

    @Test
    void devuelveElInitPointAunqueNoHayaSandbox() {
        when(feign.crearPreapproval(anyString(), any()))
                .thenReturn(new PreapprovalResponse("PRE-1", "https://prod/init", null, "pending", null));
        MercadoPagoAdapter adapter = new MercadoPagoAdapter(feign, "TEST-123");

        assertThat(adapter.crearPreapproval(solicitud()).initPoint()).isEqualTo("https://prod/init");
    }

    @Test
    void envuelveErroresDeCreacion() {
        when(feign.crearPreapproval(anyString(), any()))
                .thenThrow(mock(FeignException.class));
        MercadoPagoAdapter adapter = new MercadoPagoAdapter(feign, "TEST-123");

        assertThatThrownBy(() -> adapter.crearPreapproval(solicitud()))
                .isInstanceOf(PaymentProcessingException.class)
                .hasMessageContaining("iniciar el pago");
    }

    @Test
    void exigeTokenConfigurado() {
        MercadoPagoAdapter adapter = new MercadoPagoAdapter(feign, " ");

        assertThatThrownBy(() -> adapter.crearPreapproval(solicitud()))
                .isInstanceOf(PaymentProcessingException.class)
                .hasMessageContaining("no está configurado");
    }

    @Test
    void consultaPago() {
        when(feign.consultarPago("Bearer TEST-123", "MP-1"))
                .thenReturn(new PaymentResponse(1L, "approved", "accredited", "SUS-1",
                        "PRE-1", new BigDecimal("14900.00"), "COP"));
        MercadoPagoAdapter adapter = new MercadoPagoAdapter(feign, "TEST-123");

        MercadoPagoPort.PagoMp pago = adapter.consultarPago("MP-1");

        assertThat(pago.id()).isEqualTo("1");
        assertThat(pago.estado()).isEqualTo("approved");
        assertThat(pago.referenciaExterna()).isEqualTo("SUS-1");
        assertThat(pago.preapprovalId()).isEqualTo("PRE-1");
        assertThat(pago.monto()).isEqualByComparingTo("14900.00");
    }

    @Test
    void devuelveNuloSiElPagoNoExiste() {
        when(feign.consultarPago(anyString(), eq("MP-404")))
                .thenThrow(mock(FeignException.NotFound.class));
        MercadoPagoAdapter adapter = new MercadoPagoAdapter(feign, "TEST-123");

        assertThat(adapter.consultarPago("MP-404")).isNull();
    }

    @Test
    void envuelveErroresDeConsulta() {
        when(feign.consultarPago(anyString(), anyString()))
                .thenThrow(mock(FeignException.class));
        MercadoPagoAdapter adapter = new MercadoPagoAdapter(feign, "TEST-123");

        assertThatThrownBy(() -> adapter.consultarPago("MP-1"))
                .isInstanceOf(PaymentProcessingException.class)
                .hasMessageContaining("consultar el estado");
    }

    @Test
    void reembolsaYenvuelveErrores() {
        when(feign.reembolsar(anyString(), eq("MP-1"), any()))
                .thenReturn(new RefundResponse(9L, 1L, new BigDecimal("40.00"), "approved"));
        MercadoPagoAdapter adapter = new MercadoPagoAdapter(feign, "TEST-123");

        MercadoPagoPort.ReembolsoMp reembolso = adapter.reembolsar("MP-1", new BigDecimal("40.00"));

        assertThat(reembolso.id()).isEqualTo("9");
        assertThat(reembolso.monto()).isEqualByComparingTo("40.00");

        when(feign.reembolsar(anyString(), eq("MP-2"), any())).thenThrow(mock(FeignException.class));
        assertThatThrownBy(() -> adapter.reembolsar("MP-2", BigDecimal.ONE))
                .isInstanceOf(PaymentProcessingException.class)
                .hasMessageContaining("reembolso");
    }

    @Test
    void cancelaPreapprovalYenvuelveErrores() {
        MercadoPagoAdapter adapter = new MercadoPagoAdapter(feign, "TEST-123");

        adapter.cancelarPreapproval("PRE-1");
        verify(feign).cancelarPreapproval(eq("Bearer TEST-123"), eq("PRE-1"), any());

        when(feign.cancelarPreapproval(anyString(), eq("PRE-2"), any()))
                .thenThrow(mock(FeignException.class));
        assertThatThrownBy(() -> adapter.cancelarPreapproval("PRE-2"))
                .isInstanceOf(PaymentProcessingException.class)
                .hasMessageContaining("cancelar la renovación");
    }
}
