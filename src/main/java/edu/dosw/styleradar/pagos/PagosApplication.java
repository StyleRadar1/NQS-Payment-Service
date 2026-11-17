package edu.dosw.styleradar.pagos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Punto de entrada de ms-pagos-suscripciones.
 *
 * <p>Puerto 8400 en todos los entornos (local, QA, PROD y Gateway).
 * La configuración se externaliza por variables de entorno.</p>
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableFeignClients(basePackages = "edu.dosw.styleradar.pagos.infrastructure.adapter.out.payment")
@EnableScheduling
@ConfigurationPropertiesScan
public class PagosApplication {

    public static void main(String[] args) {
        SpringApplication.run(PagosApplication.class, args);
    }
}
