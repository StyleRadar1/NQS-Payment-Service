package edu.dosw.styleradar.pagos.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Documentación OpenAPI 3 de ms-pagos-suscripciones. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI pagosOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("StyleRadar · ms-pagos-suscripciones")
                        .version("1.0.0")
                        .description("""
                                Pagos y suscripciones de StyleRadar (SR-RF-67, 68, 75, 93-103).
                                Pagos con Mercado Pago, webhook HMAC, idempotencia con Redis,
                                conciliación diaria y eventos en styleradar.exchange.
                                """)
                        .contact(new Contact().name("Squad NQS · DOSW 2026")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth")).servers(List.of(new Server().url("/")));
    }
}
