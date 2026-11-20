package edu.dosw.styleradar.payments.infrastructure.adapter.out.payment;

import edu.dosw.styleradar.payments.infrastructure.adapter.out.payment.dto.CancelPreapprovalRequest;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.payment.dto.PaymentResponse;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.payment.dto.PreapprovalRequest;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.payment.dto.PreapprovalResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "mercadoPagoFeignClient", url = "${mercadopago.api.url}")
public interface MercadoPagoFeignClient {

    @PostMapping("/preapproval")
    PreapprovalResponse createPreapproval(@RequestHeader("Authorization") String authorization,
                                         @RequestBody PreapprovalRequest request);

    @GetMapping("/v1/payments/{id}")
    PaymentResponse getPayment(@RequestHeader("Authorization") String authorization,
                               @PathVariable("id") String id);

    @PutMapping("/preapproval/{id}")
    PreapprovalResponse cancelPreapproval(@RequestHeader("Authorization") String authorization,
                                          @PathVariable("id") String id,
                                          @RequestBody CancelPreapprovalRequest request);
}
