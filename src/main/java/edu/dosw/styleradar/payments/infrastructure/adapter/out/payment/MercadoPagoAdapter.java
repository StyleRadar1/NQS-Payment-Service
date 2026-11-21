package edu.dosw.styleradar.payments.infrastructure.adapter.out.payment;

import edu.dosw.styleradar.payments.application.port.out.MercadoPagoPort;
import edu.dosw.styleradar.payments.domain.exception.PaymentProcessingException;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.payment.dto.CancelPreapprovalRequest;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.payment.dto.PaymentResponse;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.payment.dto.AuthorizedPaymentResponse;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.payment.dto.PreapprovalRequest;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.payment.dto.PreapprovalResponse;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!dev & !test")
public class MercadoPagoAdapter implements MercadoPagoPort {

    private final MercadoPagoFeignClient feignClient;
    private final String accessToken;

    public MercadoPagoAdapter(MercadoPagoFeignClient feignClient,
                              @Value("${mercadopago.access-token}") String accessToken) {
        this.feignClient = feignClient;
        this.accessToken = accessToken;
    }

    @Override
    public PreapprovalCreated createPreapproval(PreapprovalRequest preapproval) {
        validateConfiguration();
        edu.dosw.styleradar.payments.infrastructure.adapter.out.payment.dto.PreapprovalRequest request =
                new edu.dosw.styleradar.payments.infrastructure.adapter.out.payment.dto.PreapprovalRequest(
                        preapproval.reason(),
                        preapproval.externalReference(),
                        preapproval.payerEmail(),
                        preapproval.backUrl(),
                        new edu.dosw.styleradar.payments.infrastructure.adapter.out.payment.dto.PreapprovalRequest.AutoRecurring(
                                preapproval.amount().value(),
                                preapproval.amount().currency(),
                                1,
                                "months"));
        try {
            PreapprovalResponse response = feignClient.createPreapproval(authorization(), request);
            String initPoint = selectInitPoint(response);
            return new PreapprovalCreated(response.id(), initPoint, response.status());
        } catch (FeignException ex) {
            log.error("Mercado Pago rejected preapproval creation: {}", ex.getMessage());
            throw new PaymentProcessingException(
                    "No fue posible iniciar el pago en este momento. Inténtalo nuevamente.", ex);
        }
    }

    @Override
    public MercadoPagoPayment getPayment(String paymentId) {
        validateConfiguration();
        try {
            PaymentResponse response = feignClient.getPayment(authorization(), paymentId);
            return new MercadoPagoPayment(
                    response.id() == null ? paymentId : String.valueOf(response.id()),
                    response.status(),
                    response.externalReference(),
                    response.preapprovalId(),
                    response.transactionAmount(),
                    response.currencyId());
        } catch (FeignException.NotFound ex) {
            log.warn("Mercado Pago did not find payment {}", paymentId);
            return null;
        } catch (FeignException ex) {
            throw new PaymentProcessingException(
                    "No fue posible consultar el estado del pago en Mercado Pago.", ex);
        }
    }

    @Override
    public AuthorizedPayment getAuthorizedPayment(String authorizedPaymentId) {
        validateConfiguration();
        try {
            AuthorizedPaymentResponse response = feignClient.getAuthorizedPayment(authorization(), authorizedPaymentId);
            if (response == null || response.payment() == null || response.payment().id() == null) {
                return null;
            }
            return new AuthorizedPayment(
                    String.valueOf(response.id()),
                    response.status(),
                    response.externalReference(),
                    response.preapprovalId(),
                    String.valueOf(response.payment().id()),
                    response.transactionAmount(),
                    response.currencyId());
        } catch (FeignException.NotFound ex) {
            log.warn("Mercado Pago did not find authorized payment {}", authorizedPaymentId);
            return null;
        } catch (FeignException ex) {
            throw new PaymentProcessingException(
                    "No fue posible consultar el pago autorizado en Mercado Pago.", ex);
        }
    }

    @Override
    public void cancelPreapproval(String preapprovalId) {
        validateConfiguration();
        try {
            feignClient.cancelPreapproval(
                    authorization(), preapprovalId, new CancelPreapprovalRequest("cancelled"));
        } catch (FeignException ex) {
            throw new PaymentProcessingException(
                    "No fue posible desactivar la renovación. Inténtalo nuevamente.", ex);
        }
    }

    private void validateConfiguration() {
        if (accessToken == null || accessToken.isBlank()) {
            throw new PaymentProcessingException("Mercado Pago no está configurado.");
        }
    }

    private String authorization() {
        return "Bearer " + accessToken;
    }

    private String selectInitPoint(PreapprovalResponse response) {
        boolean sandbox = accessToken.startsWith("TEST");
        if (sandbox && response.sandboxInitPoint() != null && !response.sandboxInitPoint().isBlank()) {
            return response.sandboxInitPoint();
        }
        return response.initPoint();
    }
}
