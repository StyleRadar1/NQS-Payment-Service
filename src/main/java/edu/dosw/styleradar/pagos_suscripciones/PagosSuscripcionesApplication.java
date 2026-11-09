package edu.dosw.styleradar.pagos_suscripciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class PagosSuscripcionesApplication {

    public static void main(String[] args) {
        SpringApplication.run(PagosSuscripcionesApplication.class, args);
    }
}