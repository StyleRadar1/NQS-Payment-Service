package edu.dosw.styleradar.payments;

import edu.dosw.styleradar.payments.application.usecase.WebhookUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class PaymentsApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void contextoCarga() {
        assertThat(applicationContext).isNotNull();
        assertThat(applicationContext.getBean(WebhookUseCase.class)).isNotNull();
    }
}
