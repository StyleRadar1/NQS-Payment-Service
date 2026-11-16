package edu.dosw.styleradar.pagos.infrastructure.config;

import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Exchange compartido del broker de StyleRadar.
 *
 * <p>El equipo de infraestructura exige declarar {@code styleradar.exchange}
 * como topic durable y sin auto-delete. El productor solo declara el exchange;
 * las colas y bindings los crean los consumidores
 * ({@code styleradar.{servicio}.{evento}.q}).</p>
 */
@Configuration
@ConditionalOnProperty(prefix = "styleradar.rabbit", name = "enabled",
        havingValue = "true", matchIfMissing = true)
public class RabbitMQConfig {

    @Bean
    public TopicExchange styleradarExchange(@Value("${styleradar.rabbit.exchange}") String nombre) {
        return ExchangeBuilder.topicExchange(nombre)
                .durable(true)
                .build();
    }
}
