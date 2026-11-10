package edu.dosw.styleradar.pagos_suscripciones;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test") // <--- ESTA LÍNEA ES LA SOLUCIÓN
class PagosSuscripcionesApplicationTests {

	@Test
	void contextLoads() {
	}

}