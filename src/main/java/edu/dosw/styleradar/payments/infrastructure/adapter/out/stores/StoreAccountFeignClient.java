package edu.dosw.styleradar.payments.infrastructure.adapter.out.stores;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "storeAccountFeignClient", url = "${styleradar.stores.url:http://localhost:8140}")
public interface StoreAccountFeignClient {

    @GetMapping("/api/v1/users/{userId}/store")
    StoreAccountResponse findStoreByUser(
            @PathVariable("userId") String userId,
            @RequestHeader("Authorization") String authorization);
}
