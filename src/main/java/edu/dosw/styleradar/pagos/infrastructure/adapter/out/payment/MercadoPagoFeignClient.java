package edu.dosw.styleradar.pagos.infrastructure.adapter.out.payment;

import edu.dosw.styleradar.pagos.infrastructure.adapter.out.payment.dto.CancelPreapprovalRequest;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.payment.dto.PaymentResponse;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.payment.dto.PreapprovalRequest;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.payment.dto.PreapprovalResponse;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.payment.dto.RefundRequest;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.payment.dto.RefundResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

/**
 * Cliente declarativo de Mercado Pago (ADR-04).
 *
 * <p>Se usa REST + OpenFeign, sin SDK. La URL base y el token vienen de
 * variables de entorno (SR-RNF-13).</p>
 */
@FeignClient(name = "mercadoPagoFeignClient", url = "${mercadopago.api.url}")
public interface MercadoPagoFeignClient {

    @PostMapping("/preapproval")
    PreapprovalResponse crearPreapproval(@RequestHeader("Authorization") String authorization,
                                         @RequestBody PreapprovalRequest request);

    @GetMapping("/v1/payments/{id}")
    PaymentResponse consultarPago(@RequestHeader("Authorization") String authorization,
                                  @PathVariable("id") String id);

    @PostMapping("/v1/payments/{id}/refunds")
    RefundResponse reembolsar(@RequestHeader("Authorization") String authorization,
                              @PathVariable("id") String id,
                              @RequestBody RefundRequest request);

    @PutMapping("/preapproval/{id}")
    PreapprovalResponse cancelarPreapproval(@RequestHeader("Authorization") String authorization,
                                            @PathVariable("id") String id,
                                            @RequestBody CancelPreapprovalRequest request);
}
