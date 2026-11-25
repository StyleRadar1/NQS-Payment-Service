package edu.dosw.styleradar.payments.infrastructure.adapter.out.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.dosw.styleradar.payments.domain.event.PaymentConfirmedEvent;
import edu.dosw.styleradar.payments.domain.event.SubscriptionPlanUpdatedEvent;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.entity.OutboxEventEntity;
import edu.dosw.styleradar.payments.infrastructure.adapter.out.persistence.repository.OutboxEventJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutboxEventPublisherAdapterTest {

    @Mock
    private OutboxEventJpaRepository repository;

    @Test
    void storesPlanEventInOutbox() {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        OutboxEventPublisherAdapter adapter = new OutboxEventPublisherAdapter(
                repository, mapper, "styleradar.pagos.pago.confirmado",
                "styleradar.pagos.suscripcion.suspendida",
                "styleradar.pagos.suscripcion.plan.actualizado");
        SubscriptionPlanUpdatedEvent event = SubscriptionPlanUpdatedEvent.create(
                edu.dosw.styleradar.payments.TestFixtures.activeUser(
                        "10", edu.dosw.styleradar.payments.TestFixtures.HOY,
                        edu.dosw.styleradar.payments.TestFixtures.HOY.plusDays(30)),
                null, "PLAN_RADAR_PLUS", "corr");

        adapter.publishSubscriptionPlanUpdated(event);

        ArgumentCaptor<OutboxEventEntity> captor = ArgumentCaptor.forClass(OutboxEventEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getRoutingKey())
                .isEqualTo("styleradar.pagos.suscripcion.plan.actualizado");
        assertThat(captor.getValue().getPayload()).contains("PLAN_RADAR_PLUS");
    }

    @Test
    void storesPaymentEventInOutbox() {
        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        OutboxEventPublisherAdapter adapter = new OutboxEventPublisherAdapter(
                repository, mapper, "styleradar.pagos.pago.confirmado",
                "styleradar.pagos.suscripcion.suspendida",
                "styleradar.pagos.suscripcion.plan.actualizado");
        PaymentConfirmedEvent event = PaymentConfirmedEvent.create(
                new PaymentConfirmedEvent.Data(1L, 2L, "10", 10L, "USUARIO", 2L,
                        null, null, "APPROVED", "ACTIVA",
                        new java.math.BigDecimal("14900"), "COP"), "corr");

        adapter.publishPaymentConfirmed(event);

        ArgumentCaptor<OutboxEventEntity> captor = ArgumentCaptor.forClass(OutboxEventEntity.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getEventId()).isEqualTo(event.eventId());
        assertThat(captor.getValue().getRoutingKey()).isEqualTo("styleradar.pagos.pago.confirmado");
        assertThat(captor.getValue().getStatus()).isEqualTo("PENDING");
        assertThat(captor.getValue().getPayload()).contains("\"usuarioId\":10");
    }
}
