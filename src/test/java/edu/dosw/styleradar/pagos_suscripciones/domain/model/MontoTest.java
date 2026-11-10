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
        assertThrows(IllegalArgumentException.class, () -> 
            new Monto(BigDecimal.valueOf(-10), "COP")
        );
    }

    @Test
    void noDebePermitirMonedaVacia() {
        assertThrows(IllegalArgumentException.class, () -> 
            new Monto(BigDecimal.TEN, "")
        );
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
}