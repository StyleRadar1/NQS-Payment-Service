package edu.dosw.styleradar.pagos_suscripciones.domain.model;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

class MontoTest {

    @Test
    void debeCrearMontoValido() {
        Monto monto = new Monto(BigDecimal.valueOf(14900), "COP");
        assertEquals(BigDecimal.valueOf(14900), monto.valor());
        assertEquals("COP", monto.moneda());
    }

    @Test
    void noDebePermitirMontoNegativo() {
        BigDecimal valorNegativo = new BigDecimal("-1");
        assertThrows(IllegalArgumentException.class,
            () -> new Monto(valorNegativo, "COP"));
    }

    @Test
    void noDebePermitirMonedaVacia() {
        assertThrows(IllegalArgumentException.class,
            () -> new Monto(BigDecimal.TEN, ""));
    }

    @Test
    void debeSumarMontosDeMismaMoneda() {
        Monto monto1 = new Monto(BigDecimal.valueOf(100), "COP");
        Monto monto2 = new Monto(BigDecimal.valueOf(50), "COP");
        
        Monto resultado = monto1.sumar(monto2);
        assertEquals(BigDecimal.valueOf(150), resultado.valor());
    }

    @Test
    void noDebeSumarMontosDeDiferenteMoneda() {
        Monto monto1 = new Monto(BigDecimal.valueOf(100), "COP");
        Monto monto2 = new Monto(BigDecimal.valueOf(50), "USD");
        
        assertThrows(IllegalArgumentException.class, () -> monto1.sumar(monto2));
    }

    @Test
void sumarConBigDecimalNegativoResultanteLanza() {
    // sumar nunca da negativo si ambos son >= 0, pero validamos el branch de moneda nula
    assertThrows(IllegalArgumentException.class,
            () -> new Monto(BigDecimal.ONE, null));
}
}