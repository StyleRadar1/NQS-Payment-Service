package edu.dosw.styleradar.payments.infrastructure.adapter.out.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dosw.styleradar.payments.domain.event.PaymentConfirmedEvent;
import edu.dosw.styleradar.payments.domain.event.SubscriptionSuspendedEvent;
import edu.dosw.styleradar.payments.domain.exception.PaymentProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitEventPublisherAdapterTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private RabbitEventPublisherAdapter adapter;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
        adapter = new RabbitEventPublisherAdapter(rabbitTemplate, objectMapper,
                "styleradar.exchange", "styleradar.pagos.pago.confirmado",
                "styleradar.pagos.suscripcion.suspendida");
    }

    @Test
    void publishesPaymentConfirmedWithJsonProperties() {
        PaymentConfirmedEvent event = PaymentConfirmedEvent.create(
                new PaymentConfirmedEvent.Data(1L, 2L, "USER-1", 1L, "USUARIO",
                        2L, null, null, "ACTIVA"), "corr-1");

        adapter.publishPaymentConfirmed(event);

        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MessagePostProcessor> processor =
                ArgumentCaptor.forClass(MessagePostProcessor.class);
        verify(rabbitTemplate).convertAndSend(
                eq("styleradar.exchange"), eq("styleradar.pagos.pago.confirmado"),
                json.capture(), processor.capture());

        assertThat(json.getValue()).contains("\"eventType\":\"styleradar.pagos.pago.confirmado\"");
        assertThat(json.getValue()).contains("\"suscriptorId\":\"USER-1\"");
        assertThat(json.getValue()).contains("\"tipoSuscriptor\":\"USUARIO\"");

        Message message = processor.getValue().postProcessMessage(new Message(new byte[0]));
        assertThat(message.getMessageProperties().getContentType()).isEqualTo("application/json");
        assertThat(message.getMessageProperties().getMessageId()).isEqualTo(event.eventId().toString());
        assertThat((String) message.getMessageProperties().getHeader("eventType"))
                .isEqualTo("styleradar.pagos.pago.confirmado");
    }

    @Test
    void publishesSubscriptionSuspended() {
        SubscriptionSuspendedEvent event = SubscriptionSuspendedEvent.create(
                new SubscriptionSuspendedEvent.Data(1L, "STORE-1", "ALMACEN",
                        3L, "900123456", null, LocalDate.of(2026, 11, 1), "FALTA_DE_PAGO"), "corr-2");

        adapter.publishSubscriptionSuspended(event);

        verify(rabbitTemplate).convertAndSend(
                eq("styleradar.exchange"), eq("styleradar.pagos.suscripcion.suspendida"),
                anyString(), any(MessagePostProcessor.class));
    }

    @Test
    void wrapsBrokerFailure() {
        doThrow(new AmqpException("broker down")).when(rabbitTemplate)
                .convertAndSend(anyString(), anyString(), anyString(), any(MessagePostProcessor.class));
        PaymentConfirmedEvent event = PaymentConfirmedEvent.create(
                new PaymentConfirmedEvent.Data(1L, 2L, "USER-1", 1L, "USUARIO",
                        2L, null, null, "ACTIVA"), null);

        assertThatThrownBy(() -> adapter.publishPaymentConfirmed(event))
                .isInstanceOf(PaymentProcessingException.class)
                .hasMessageContaining("publicar el evento");
    }
}

