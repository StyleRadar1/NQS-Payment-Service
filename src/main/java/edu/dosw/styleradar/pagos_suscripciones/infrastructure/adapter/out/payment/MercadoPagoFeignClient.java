package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.payment;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "mercadoPagoClient", url = "${mercadopago.api.url}")
public interface MercadoPagoFeignClient {

    @PostMapping("/preapproval")
    Map<String, Object> crearPreapproval(
            @RequestHeader("Authorization") String authorization,
            @RequestBody Map<String, Object> request
    );

    @GetMapping("/v1/payments/{id}")
    Map<String, Object> consultarPago(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("id") String id
    );

    @PostMapping("/preapproval/{id}/cancel")
    void cancelarPreapproval(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("id") String id
    );

    @PostMapping("/v1/payments/{id}/refunds")
    Map<String, Object> reembolsarPago(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("id") String id,
            @RequestBody Map<String, Object> request
    );

    // --- NUEVO ENDPOINT ---
    @GetMapping("/authorized_payments/{id}")
    Map<String, Object> consultarPagoAutorizado(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("id") String id
    );
}