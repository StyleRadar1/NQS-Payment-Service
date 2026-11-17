package edu.dosw.styleradar.pagos.infrastructure.adapter.out.payment;

import edu.dosw.styleradar.pagos.application.port.out.MercadoPagoPort;
import edu.dosw.styleradar.pagos.domain.exception.PaymentProcessingException;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.payment.dto.CancelPreapprovalRequest;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.payment.dto.PaymentResponse;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.payment.dto.PreapprovalRequest;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.payment.dto.PreapprovalResponse;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.payment.dto.RefundRequest;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.payment.dto.RefundResponse;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Adaptador real de Mercado Pago (perfil QA/PROD).
 *
 * <p>ADR-04: REST + OpenFeign. ADR-05: solo se activa cuando el perfil no es
 * dev ni test. SR-RNF-13: el token se toma de una variable de entorno.</p>
 */
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
    public PreapprovalCreado crearPreapproval(PreapprovalSolicitud solicitud) {
        validarConfiguracion();
        PreapprovalRequest request = new PreapprovalRequest(
                solicitud.razon(),
                solicitud.referenciaExterna(),
                solicitud.correoPagador(),
                solicitud.urlRetorno(),
                new PreapprovalRequest.AutoRecurring(
                        solicitud.monto().valor(),
                        solicitud.monto().moneda(),
                        1,
                        "months"));
        try {
            PreapprovalResponse response = feignClient.crearPreapproval(authorization(), request);
            String initPoint = elegirInitPoint(response);
            return new PreapprovalCreado(response.id(), initPoint, response.status());
        } catch (FeignException ex) {
            log.error("Mercado Pago rechazó la creación de la preapproval: {}", ex.getMessage());
            throw new PaymentProcessingException(
                    "No fue posible iniciar el pago en este momento. Inténtalo nuevamente.", ex);
        }
    }

    @Override
    public PagoMp consultarPago(String idPago) {
        validarConfiguracion();
        try {
            PaymentResponse response = feignClient.consultarPago(authorization(), idPago);
            return new PagoMp(
                    response.id() == null ? idPago : String.valueOf(response.id()),
                    response.status(),
                    response.externalReference(),
                    response.preapprovalId(),
                    response.transactionAmount(),
                    response.currencyId());
        } catch (FeignException.NotFound ex) {
            log.warn("Mercado Pago no encontró el pago {}", idPago);
            return null;
        } catch (FeignException ex) {
            throw new PaymentProcessingException(
                    "No fue posible consultar el estado del pago en Mercado Pago.", ex);
        }
    }

    @Override
    public ReembolsoMp reembolsar(String idPago, BigDecimal monto) {
        validarConfiguracion();
        try {
            RefundResponse response = feignClient.reembolsar(
                    authorization(), idPago, new RefundRequest(monto));
            return new ReembolsoMp(
                    response.id() == null ? null : String.valueOf(response.id()),
                    response.amount());
        } catch (FeignException ex) {
            log.error("Mercado Pago rechazó el reembolso del pago {}: {}", idPago, ex.getMessage());
            throw new PaymentProcessingException(
                    "No fue posible procesar el reembolso. Inténtalo nuevamente.", ex);
        }
    }

    @Override
    public void cancelarPreapproval(String idPreapproval) {
        validarConfiguracion();
        try {
            feignClient.cancelarPreapproval(
                    authorization(), idPreapproval, new CancelPreapprovalRequest("cancelled"));
        } catch (FeignException ex) {
            throw new PaymentProcessingException(
                    "No fue posible cancelar la renovación. Inténtalo nuevamente.", ex);
        }
    }

    private void validarConfiguracion() {
        if (accessToken == null || accessToken.isBlank()) {
            throw new PaymentProcessingException("Mercado Pago no está configurado.");
        }
    }

    private String authorization() {
        return "Bearer " + accessToken;
    }

    private String elegirInitPoint(PreapprovalResponse response) {
        boolean sandbox = accessToken.startsWith("TEST");
        if (sandbox && response.sandboxInitPoint() != null && !response.sandboxInitPoint().isBlank()) {
            return response.sandboxInitPoint();
        }
        return response.initPoint();
    }
}
