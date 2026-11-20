package edu.dosw.styleradar.payments.infrastructure.adapter.out.payment;

import edu.dosw.styleradar.payments.application.port.out.MercadoPagoPort;
import edu.dosw.styleradar.payments.domain.exception.PaymentProcessingException;
import edu.dosw.styleradar.payments.domain.model.Amount;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.payment.dto.PaymentResponse;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.payment.dto.PreapprovalResponse;
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
class MercadoPagoAdapterTest {

    private final MercadoPagoFeignClient feign = mock(MercadoPagoFeignClient.class);

    private MercadoPagoPort.PreapprovalRequest request() {
        return new MercadoPagoPort.PreapprovalRequest("Subscription Radar+", "SUS-1",
                "pagador@testuser.com", Amount.cop(new BigDecimal("14900.00")), "https://app/back");
    }

    @Test
    void creaPreapprovalYUsaSandboxConTokenDePrueba() {
        when(feign.createPreapproval(eq("Bearer TEST-123"), any()))
                .thenReturn(new PreapprovalResponse("PRE-1", "https://prod/init",
                        "https://sandbox/init", "pending", "SUS-1"));
        MercadoPagoAdapter adapter = new MercadoPagoAdapter(feign, "TEST-123");

        MercadoPagoPort.PreapprovalCreated creado = adapter.createPreapproval(request());

        assertThat(creado.id()).isEqualTo("PRE-1");
        assertThat(creado.initPoint()).isEqualTo("https://sandbox/init");
    }

    @Test
    void creaPreapprovalYUsaInitPointRealEnProduccion() {
        when(feign.createPreapproval(eq("Bearer APP-123"), any()))
                .thenReturn(new PreapprovalResponse("PRE-1", "https://prod/init",
                        "https://sandbox/init", "pending", "SUS-1"));
        MercadoPagoAdapter adapter = new MercadoPagoAdapter(feign, "APP-123");

        assertThat(adapter.createPreapproval(request()).initPoint()).isEqualTo("https://prod/init");
    }

    @Test
    void devuelveElInitPointAunqueNoHayaSandbox() {
        when(feign.createPreapproval(anyString(), any()))
                .thenReturn(new PreapprovalResponse("PRE-1", "https://prod/init", null, "pending", null));
        MercadoPagoAdapter adapter = new MercadoPagoAdapter(feign, "TEST-123");

        assertThat(adapter.createPreapproval(request()).initPoint()).isEqualTo("https://prod/init");
    }

    @Test
    void envuelveErroresDeCreacion() {
        when(feign.createPreapproval(anyString(), any()))
                .thenThrow(mock(FeignException.class));
        MercadoPagoAdapter adapter = new MercadoPagoAdapter(feign, "TEST-123");

        assertThatThrownBy(() -> adapter.createPreapproval(request()))
                .isInstanceOf(PaymentProcessingException.class)
                .hasMessageContaining("iniciar el pago");
    }

    @Test
    void exigeTokenConfigurado() {
        MercadoPagoAdapter adapter = new MercadoPagoAdapter(feign, " ");

        assertThatThrownBy(() -> adapter.createPreapproval(request()))
                .isInstanceOf(PaymentProcessingException.class)
                .hasMessageContaining("configurado");
    }

    @Test
    void consultaPayment() {
        when(feign.getPayment("Bearer TEST-123", "MP-1"))
                .thenReturn(new PaymentResponse(1L, "approved", "accredited", "SUS-1",
                        "PRE-1", new BigDecimal("14900.00"), "COP"));
        MercadoPagoAdapter adapter = new MercadoPagoAdapter(feign, "TEST-123");

        MercadoPagoPort.MercadoPagoPayment payment = adapter.getPayment("MP-1");

        assertThat(payment.id()).isEqualTo("1");
        assertThat(payment.status()).isEqualTo("approved");
        assertThat(payment.externalReference()).isEqualTo("SUS-1");
        assertThat(payment.preapprovalId()).isEqualTo("PRE-1");
        assertThat(payment.amount()).isEqualByComparingTo("14900.00");
    }

    @Test
    void devuelveNuloSiElPaymentNoExiste() {
        when(feign.getPayment(anyString(), eq("MP-404")))
                .thenThrow(mock(FeignException.NotFound.class));
        MercadoPagoAdapter adapter = new MercadoPagoAdapter(feign, "TEST-123");

        assertThat(adapter.getPayment("MP-404")).isNull();
    }

    @Test
    void envuelveErroresDeConsulta() {
        when(feign.getPayment(anyString(), anyString()))
                .thenThrow(mock(FeignException.class));
        MercadoPagoAdapter adapter = new MercadoPagoAdapter(feign, "TEST-123");

        assertThatThrownBy(() -> adapter.getPayment("MP-1"))
                .isInstanceOf(PaymentProcessingException.class)
                .hasMessageContaining("consultar el estado");
    }

    @Test
    void cancelaPreapprovalYenvuelveErrores() {
        MercadoPagoAdapter adapter = new MercadoPagoAdapter(feign, "TEST-123");

        adapter.cancelPreapproval("PRE-1");
        verify(feign).cancelPreapproval(eq("Bearer TEST-123"), eq("PRE-1"), any());

        when(feign.cancelPreapproval(anyString(), eq("PRE-2"), any()))
                .thenThrow(mock(FeignException.class));
        assertThatThrownBy(() -> adapter.cancelPreapproval("PRE-2"))
                .isInstanceOf(PaymentProcessingException.class)
                .hasMessageContaining("desactivar la renovación");
    }
}
