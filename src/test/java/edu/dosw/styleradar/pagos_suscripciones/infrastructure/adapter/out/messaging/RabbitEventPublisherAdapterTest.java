package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.messaging;

import edu.dosw.styleradar.pagos_suscripciones.application.dto.event.PagoConfirmadoEvent;
import edu.dosw.styleradar.pagos_suscripciones.application.dto.event.SuscripcionSuspendidaEvent;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.TipoSuscriptor;
import edu.dosw.styleradar.pagos_suscripciones.infrastructure.config.RabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitEventPublisherAdapterTest {

    @Mock private RabbitTemplate rabbitTemplate;
    @InjectMocks private RabbitEventPublisherAdapter adapter;

    @Test
    void debePublicarPagoConfirmadoConPayloadVersionado() {
        PagoConfirmadoEvent evt = new PagoConfirmadoEvent(
            UUID.randomUUID().toString(),
            PagoConfirmadoEvent.TYPE,
            PagoConfirmadoEvent.VERSION,
            LocalDateTime.now(),
            10L, "USER-1", TipoSuscriptor.USUARIO,
            2L, "Radar+",
            Map.of("max_wishlist", -1, "probador_foto", true),
            LocalDate.now(), LocalDate.now().plusMonths(1),
            new BigDecimal("14900"), "COP"
        );

        adapter.publicarPagoConfirmado(evt);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(
            eq(RabbitMQConfig.EXCHANGE),
            eq(RabbitMQConfig.RK_PAGO_CONFIRMADO),
            captor.capture()
        );

        PagoConfirmadoEvent enviado = (PagoConfirmadoEvent) captor.getValue();
        assertEquals("USER-1", enviado.suscriptorId());
        assertEquals(2L, enviado.planId());
        assertNotNull(enviado.limites());
        assertEquals("pago.confirmado", enviado.eventType());
    }

    @Test
    void debePublicarSuscripcionSuspendidaConPayloadVersionado() {
        SuscripcionSuspendidaEvent evt = new SuscripcionSuspendidaEvent(
            UUID.randomUUID().toString(),
            SuscripcionSuspendidaEvent.TYPE,
            SuscripcionSuspendidaEvent.VERSION,
            LocalDateTime.now(),
            10L, "STORE-1", TipoSuscriptor.ALMACEN,
            3L, 1L, "Falta de pago"
        );

        adapter.publicarSuscripcionSuspendida(evt);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(rabbitTemplate).convertAndSend(
            eq(RabbitMQConfig.EXCHANGE),
            eq(RabbitMQConfig.RK_SUSCRIPCION_SUSPENDIDA),
            captor.capture()
        );

        SuscripcionSuspendidaEvent enviado = (SuscripcionSuspendidaEvent) captor.getValue();
        assertEquals("STORE-1", enviado.suscriptorId());
        assertEquals("Falta de pago", enviado.motivo());
        assertEquals(3L, enviado.planAnteriorId());
    }
}