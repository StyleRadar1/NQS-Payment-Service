package edu.dosw.styleradar.pagos;

import edu.dosw.styleradar.pagos.application.usecase.WebhookUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/** Verifica que el contexto completo de Spring se ensamble con H2 y el mock de MP. */
@SpringBootTest
@ActiveProfiles("test")
class PagosApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextoCarga() {
        assertThat(applicationContext).isNotNull();
        assertThat(applicationContext.getBean(WebhookUseCase.class)).isNotNull();
    }
}
