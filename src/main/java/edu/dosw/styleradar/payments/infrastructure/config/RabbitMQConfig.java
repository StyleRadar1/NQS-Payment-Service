package edu.dosw.styleradar.payments.infrastructure.config;

import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "styleradar.rabbit", name = "enabled",
        havingValue = "true", matchIfMissing = true)
public class RabbitMQConfig {

    @Bean
    public TopicExchange styleradarExchange(@Value("${styleradar.rabbit.exchange}") String name) {
        return ExchangeBuilder.topicExchange(name)
                .durable(true)
                .build();
    }
}
