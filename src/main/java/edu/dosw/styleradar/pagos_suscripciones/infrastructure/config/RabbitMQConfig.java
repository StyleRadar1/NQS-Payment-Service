package edu.dosw.styleradar.pagos_suscripciones.infrastructure.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE = "styleradar.payments.exchange";
    public static final String RK_PAGO_CONFIRMADO = "pago.confirmado";
    public static final String RK_SUSCRIPCION_SUSPENDIDA = "suscripcion.suspendida";

    @Bean
    public TopicExchange paymentsExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}