package edu.dosw.styleradar.payments.infrastructure.adapter.out.stores;

import edu.dosw.styleradar.payments.application.port.out.StoreAccountPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * En dev/test no hay ms-tiendas disponible. La validación de propiedad se
 * cubre con pruebas de controlador y el adaptador real; este perfil mantiene
 * el flujo local operativo.
 */
@Component
@Profile({"dev", "test"})
public class MockStoreAccountAdapter implements StoreAccountPort {

    @Override
    public boolean belongsToStore(String userId, String taxId, String authorization) {
        return true;
    }
}
