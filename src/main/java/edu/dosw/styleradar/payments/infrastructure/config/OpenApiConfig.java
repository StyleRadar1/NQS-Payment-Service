package edu.dosw.styleradar.payments.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI paymentsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("StyleRadar · ms-payments-subscriptions")
                        .version("1.0.0")
                        .description("""
                                Payments y subscriptions de StyleRadar (SR-RF-67, 68, 75, 93-103).
                                Payments with Mercado Pago, HMAC webhook, Redis idempotency,
                                daily reconciliation and events in styleradar.exchange.
                                """)
                        .contact(new Contact().name("Squad NQS · DOSW 2026")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
