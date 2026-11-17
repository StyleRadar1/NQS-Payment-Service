package edu.dosw.styleradar.pagos.infrastructure.adapter.out.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dosw.styleradar.pagos.domain.event.PagoConfirmadoEvent;
import edu.dosw.styleradar.pagos.domain.event.SuscripcionSuspendidaEvent;
import edu.dosw.styleradar.pagos.domain.exception.PaymentProcessingException;
import edu.dosw.styleradar.pagos.domain.model.TipoSuscriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
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
@DisplayName("RabbitEventPublisherAdapter (ADR-07, D10)")
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
    void publicaPagoConfirmadoConPropiedadesJson() {
        PagoConfirmadoEvent evento = PagoConfirmadoEvent.crear(
                new PagoConfirmadoEvent.Data(1L, 2L, "USER-1", TipoSuscriptor.USUARIO,
                        2L, null, null, "APROBADO"), "corr-1");

        adapter.publicarPagoConfirmado(evento);

        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<MessagePostProcessor> processor =
                ArgumentCaptor.forClass(MessagePostProcessor.class);
        verify(rabbitTemplate).convertAndSend(
                eq("styleradar.exchange"), eq("styleradar.pagos.pago.confirmado"),
                json.capture(), processor.capture());

        assertThat(json.getValue()).contains("\"eventType\":\"styleradar.pagos.pago.confirmado\"");
        assertThat(json.getValue()).contains("\"suscriptorId\":\"USER-1\"");

        Message message = processor.getValue().postProcessMessage(new Message(new byte[0]));
        assertThat(message.getMessageProperties().getContentType()).isEqualTo("application/json");
        assertThat(message.getMessageProperties().getMessageId()).isEqualTo(evento.eventId().toString());
        assertThat((String) message.getMessageProperties().getHeader("eventType"))
                .isEqualTo("styleradar.pagos.pago.confirmado");
    }

    @Test
    void publicaSuscripcionSuspendida() {
        SuscripcionSuspendidaEvent evento = SuscripcionSuspendidaEvent.crear(
                new SuscripcionSuspendidaEvent.Data(1L, "ALMACEN-1", TipoSuscriptor.ALMACEN,
                        3L, "900123", null, LocalDate.of(2026, 11, 1), "FALTA_DE_PAGO"), "corr-2");

        adapter.publicarSuscripcionSuspendida(evento);

        verify(rabbitTemplate).convertAndSend(
                eq("styleradar.exchange"), eq("styleradar.pagos.suscripcion.suspendida"),
                anyString(), any(MessagePostProcessor.class));
    }

    @Test
    void envuelveFalloDelBroker() {
        doThrow(new AmqpException("caído")).when(rabbitTemplate)
                .convertAndSend(anyString(), anyString(), anyString(), any(MessagePostProcessor.class));
        PagoConfirmadoEvent evento = PagoConfirmadoEvent.crear(
                new PagoConfirmadoEvent.Data(1L, 2L, "USER-1", TipoSuscriptor.USUARIO,
                        2L, null, null, "APROBADO"), null);

        assertThatThrownBy(() -> adapter.publicarPagoConfirmado(evento))
                .isInstanceOf(PaymentProcessingException.class)
                .hasMessageContaining("publicar el evento");
    }
}
