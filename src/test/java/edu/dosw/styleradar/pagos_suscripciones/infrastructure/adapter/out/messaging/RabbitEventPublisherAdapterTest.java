package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.messaging;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.Suscripcion;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.TipoSuscriptor;
import edu.dosw.styleradar.pagos_suscripciones.infrastructure.config.RabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDate;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitEventPublisherAdapterTest {

    @Mock private RabbitTemplate rabbitTemplate;
    @InjectMocks private RabbitEventPublisherAdapter adapter;

    @Test
    void debePublicarPagoConfirmado() {
        Suscripcion suscripcion = new Suscripcion(1L, "USER-1", TipoSuscriptor.USUARIO, 2L);
        suscripcion.activar(LocalDate.now().plusMonths(1));

        adapter.publicarPagoConfirmado(suscripcion);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE), 
                eq(RabbitMQConfig.RK_PAGO_CONFIRMADO), 
                anyMap()
        );
    }

    @Test
    void debePublicarSuscripcionSuspendida() {
        Suscripcion suscripcion = new Suscripcion(1L, "USER-1", TipoSuscriptor.USUARIO, 2L);

        adapter.publicarSuscripcionSuspendida(suscripcion);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.EXCHANGE), 
                eq(RabbitMQConfig.RK_SUSCRIPCION_SUSPENDIDA), 
                anyMap()
        );
    }
}