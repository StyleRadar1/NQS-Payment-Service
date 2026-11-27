package edu.dosw.styleradar.payments.infrastructure.adapter.out.stores;

import edu.dosw.styleradar.payments.application.port.out.StoreAccountPort;
import edu.dosw.styleradar.payments.domain.exception.PaymentProcessingException;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
@Profile("!dev & !test")
public class StoreAccountAdapter implements StoreAccountPort {

    private final StoreAccountFeignClient feignClient;

    public StoreAccountAdapter(StoreAccountFeignClient feignClient) {
        this.feignClient = Objects.requireNonNull(feignClient);
    }

    @Override
    public boolean belongsToStore(String userId, String taxId, String authorization) {
        if (userId == null || userId.isBlank() || taxId == null || taxId.isBlank()
                || authorization == null || !authorization.startsWith("Bearer ")) {
            return false;
        }

        try {
            StoreAccountResponse store = feignClient.findStoreByUser(userId.trim(), authorization);
            return store != null && sameNit(store.nit(), taxId);
        } catch (FeignException.NotFound ex) {
            log.warn("User {} does not have an associated store", userId);
            return false;
        } catch (FeignException ex) {
            log.error("Could not validate store ownership for user {}: {}", userId, ex.getMessage());
            throw new PaymentProcessingException(
                    "No fue posible validar la tienda asociada al usuario.", ex);
        }
    }

    private static boolean sameNit(String storeNit, String requestedNit) {
        String left = digits(storeNit);
        String right = digits(requestedNit);
        return !left.isBlank() && left.equals(right);
    }

    private static String digits(String value) {
        if (value == null) {
            return "";
        }
        String digits = value.replaceAll("\\D", "");
        return digits.length() > 9 ? digits.substring(0, 9) : digits;
    }
}
