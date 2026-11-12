package edu.dosw.styleradar.pagos_suscripciones.infrastructure.config;

import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE = "styleradar.payments.exchange";
    public static final String RK_PAGO_CONFIRMADO = "pago.confirmado";
    public static final String RK_SUSCRIPCION_SUSPENDIDA = "suscripcion.suspendida";

    // Creamos una cola de prueba y la enlazamos. Así el exchange se crea obligatoriamente.
    @Bean
    public Declarables topicBindings() {
        TopicExchange exchange = new TopicExchange(EXCHANGE);
        Queue pagoQueue = new Queue("demo.pago.confirmado.queue", false);
        Queue suspQueue = new Queue("demo.suscripcion.suspendida.queue", false);
        return new Declarables(
            exchange,
            pagoQueue,
            suspQueue,
            BindingBuilder.bind(pagoQueue).to(exchange).with(RK_PAGO_CONFIRMADO),
            BindingBuilder.bind(suspQueue).to(exchange).with(RK_SUSCRIPCION_SUSPENDIDA)
        );
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}