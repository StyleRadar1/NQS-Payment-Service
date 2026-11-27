package edu.dosw.styleradar.payments.infrastructure.adapter.out.stores;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreAccountAdapterTest {

    @Mock
    private StoreAccountFeignClient feignClient;

    @InjectMocks
    private StoreAccountAdapter adapter;

    @Test
    void aceptaElNitDeLaTiendaAunqueTengaDigitoDeVerificacion() {
        when(feignClient.findStoreByUser("3", "Bearer token"))
                .thenReturn(new StoreAccountResponse("900555777-1"));

        assertThat(adapter.belongsToStore("3", "900555777", "Bearer token")).isTrue();
        verify(feignClient).findStoreByUser("3", "Bearer token");
    }

    @Test
    void rechazaUnNitQueNoCoincideConLaTienda() {
        when(feignClient.findStoreByUser("3", "Bearer token"))
                .thenReturn(new StoreAccountResponse("900555777-1"));

        assertThat(adapter.belongsToStore("3", "900123675", "Bearer token")).isFalse();
    }

    @Test
    void rechazaSinAuthorizationSinLlamarAlServicioDeTiendas() {
        assertThat(adapter.belongsToStore("3", "900555777", null)).isFalse();
    }
}
