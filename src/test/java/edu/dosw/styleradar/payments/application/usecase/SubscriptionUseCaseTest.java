package edu.dosw.styleradar.payments.application.usecase;

import edu.dosw.styleradar.payments.TestFixtures;
import edu.dosw.styleradar.payments.application.dto.request.StartSubscriptionRequest;
import edu.dosw.styleradar.payments.application.dto.response.CancellationResponse;
import edu.dosw.styleradar.payments.application.dto.response.StartSubscriptionResponse;
import edu.dosw.styleradar.payments.application.dto.response.SubscriptionResponse;
import edu.dosw.styleradar.payments.application.port.out.MercadoPagoPort;
import edu.dosw.styleradar.payments.application.port.out.PaymentOrderRepositoryPort;
import edu.dosw.styleradar.payments.application.port.out.PlanRepositoryPort;
import edu.dosw.styleradar.payments.application.port.out.SubscriptionRepositoryPort;
import edu.dosw.styleradar.payments.domain.exception.Messages;
import edu.dosw.styleradar.payments.domain.exception.UnauthorizedException;
import edu.dosw.styleradar.payments.domain.exception.ConflictException;
import edu.dosw.styleradar.payments.domain.exception.OperationNotAllowedException;
import edu.dosw.styleradar.payments.domain.exception.PaymentProcessingException;
import edu.dosw.styleradar.payments.domain.exception.PlanNotFoundException;
import edu.dosw.styleradar.payments.domain.exception.InvalidRequestException;
import edu.dosw.styleradar.payments.domain.exception.SubscriptionNotFoundException;
import edu.dosw.styleradar.payments.domain.model.SubscriptionStatus;
import edu.dosw.styleradar.payments.domain.model.PaymentOrder;
import edu.dosw.styleradar.payments.domain.model.Subscription;
import edu.dosw.styleradar.payments.domain.model.SubscriberType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionUseCaseTest {

    @Mock
    private PlanRepositoryPort planRepository;
    @Mock
    private SubscriptionRepositoryPort subscriptionRepository;
    @Mock
    private PaymentOrderRepositoryPort paymentOrderRepository;
    @Mock
    private MercadoPagoPort mercadoPago;

    private SubscriptionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SubscriptionUseCase(planRepository, subscriptionRepository,
                paymentOrderRepository, mercadoPago, TestFixtures.CLOCK, "https://app/back");
    }

    private void saveSubscriptionConId() {
        when(subscriptionRepository.save(any(Subscription.class)))
                .thenAnswer(inv -> TestFixtures.conIdSubscription(inv.getArgument(0), 100L));
    }

    private void saveOrderConId() {
        when(paymentOrderRepository.save(any(PaymentOrder.class)))
                .thenAnswer(inv -> TestFixtures.conIdOrder(inv.getArgument(0), 200L));
    }

    private MercadoPagoPort.PreapprovalCreated preapproval() {
        return new MercadoPagoPort.PreapprovalCreated("PRE-1",
                "https://www.mercadopago.com.co/subscriptions/checkout?preapproval_id=PRE-1", "pending");
    }

    @Nested
    class Start {

        @Test
        void iniciaSubscriptionDeUserConRadarPlus() {
            when(planRepository.findPaidPlanByType(SubscriberType.USER))
                    .thenReturn(Optional.of(TestFixtures.planRadarPlus()));
            when(subscriptionRepository.findActiveOrPending("USER-1")).thenReturn(Optional.empty());
            when(subscriptionRepository.findLatestBySubscriber("USER-1")).thenReturn(Optional.empty());
            saveSubscriptionConId();
            saveOrderConId();
            when(mercadoPago.createPreapproval(any())).thenReturn(preapproval());

            StartSubscriptionResponse response = useCase.start("USER-1",
                    new StartSubscriptionRequest(SubscriberType.USER,
                            "pagador@testuser.com", 2L, null, null));

            assertThat(response.subscriptionId()).isEqualTo(100L);
            assertThat(response.orderId()).isEqualTo(200L);
            assertThat(response.status()).isEqualTo(SubscriptionStatus.PENDING);
            assertThat(response.initPoint()).contains("preapproval_id=PRE-1");
            assertThat(response.message()).contains("Radar+");

            ArgumentCaptor<MercadoPagoPort.PreapprovalRequest> request =
                    ArgumentCaptor.forClass(MercadoPagoPort.PreapprovalRequest.class);
            verify(mercadoPago).createPreapproval(request.capture());
            assertThat(request.getValue().externalReference()).isEqualTo("SUS-100");
            assertThat(request.getValue().payerEmail()).isEqualTo("pagador@testuser.com");
            assertThat(request.getValue().amount().value()).isEqualByComparingTo("14900.00");
            assertThat(request.getValue().backUrl()).isEqualTo("https://app/back");

            ArgumentCaptor<Subscription> guardadas = ArgumentCaptor.forClass(Subscription.class);
            verify(subscriptionRepository, org.mockito.Mockito.times(2)).save(guardadas.capture());
            assertThat(guardadas.getAllValues().get(1).getMercadoPagoPreapprovalId()).isEqualTo("PRE-1");
        }

        @Test
        void iniciaSubscriptionDeStoreConNitYPlanUnico() {
            when(planRepository.findPaidPlanByType(SubscriberType.STORE))
                    .thenReturn(Optional.of(TestFixtures.planStore()));
            when(subscriptionRepository.findActiveOrPending("STORE-1")).thenReturn(Optional.empty());
            when(subscriptionRepository.findLatestBySubscriber("STORE-1")).thenReturn(Optional.empty());
            saveSubscriptionConId();
            saveOrderConId();
            when(mercadoPago.createPreapproval(any())).thenReturn(preapproval());

            StartSubscriptionResponse response = useCase.start("STORE-1",
                    new StartSubscriptionRequest(SubscriberType.STORE,
                            "store@testuser.com", 2L, "900123456", java.util.UUID.fromString("a1111111-1111-1111-1111-111111111111")));

            assertThat(response.message()).contains("activar tu almac");
            ArgumentCaptor<Subscription> guardadas = ArgumentCaptor.forClass(Subscription.class);
            verify(subscriptionRepository, org.mockito.Mockito.times(2)).save(guardadas.capture());
            assertThat(guardadas.getAllValues().get(0).getTaxId()).isEqualTo("900123456");
            assertThat(guardadas.getAllValues().get(0).getPlanId()).isEqualTo(3L);
        }

        @Test
        void reutilizaSubscriptionSuspendedDelMismoPlan() {
            when(planRepository.findPaidPlanByType(SubscriberType.USER))
                    .thenReturn(Optional.of(TestFixtures.planRadarPlus()));
            when(subscriptionRepository.findActiveOrPending("USER-1")).thenReturn(Optional.empty());
            when(subscriptionRepository.findLatestBySubscriber("USER-1"))
                    .thenReturn(Optional.of(TestFixtures.suspendedUser("USER-1")));
            saveSubscriptionConId();
            saveOrderConId();
            when(mercadoPago.createPreapproval(any())).thenReturn(preapproval());

            useCase.start("USER-1", new StartSubscriptionRequest(SubscriberType.USER,
                    "pagador@testuser.com", 2L, null, null));

            ArgumentCaptor<Subscription> guardadas = ArgumentCaptor.forClass(Subscription.class);
            verify(subscriptionRepository, org.mockito.Mockito.times(2)).save(guardadas.capture());
            assertThat(guardadas.getAllValues().get(0).getId()).isEqualTo(101L);
        }

        @Test
        void validaEmailDeUserYStore() {
            assertThatThrownBy(() -> useCase.start("USER-1",
                    new StartSubscriptionRequest(SubscriberType.USER, "a@b", null, null, null)))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessage(Messages.INVALID_EMAIL_RADAR_PLUS);

            assertThatThrownBy(() -> useCase.start("STORE-1",
                    new StartSubscriptionRequest(SubscriberType.STORE, "sin-email", null, null, null)))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessage(Messages.INVALID_EMAIL);
        }

        @Test
        void exigePlanDisponible() {
            when(planRepository.findPaidPlanByType(SubscriberType.USER))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.start("USER-1",
                    new StartSubscriptionRequest(SubscriberType.USER, "a@b.com", null, null, null)))
                    .isInstanceOf(PlanNotFoundException.class);
        }

        @Test
        void rechazaPlanDistintoParaUser() {
            when(planRepository.findPaidPlanByType(SubscriberType.USER))
                    .thenReturn(Optional.of(TestFixtures.planRadarPlus()));

            assertThatThrownBy(() -> useCase.start("USER-1",
                    new StartSubscriptionRequest(SubscriberType.USER, "a@b.com", 3L, null, null)))
                    .isInstanceOf(PlanNotFoundException.class);
        }

        @Test
        void rechazaSubscriptionVigenteDeUser() {
            when(planRepository.findPaidPlanByType(SubscriberType.USER))
                    .thenReturn(Optional.of(TestFixtures.planRadarPlus()));
            when(subscriptionRepository.findActiveOrPending("USER-1"))
                    .thenReturn(Optional.of(TestFixtures.activeUser("USER-1",
                            TestFixtures.HOY.minusDays(10), TestFixtures.HOY.plusDays(20))));

            assertThatThrownBy(() -> useCase.start("USER-1",
                    new StartSubscriptionRequest(SubscriberType.USER, "a@b.com", null, null, null)))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage(Messages.RADAR_PLUS_ACTIVE);
        }

        @Test
        void exigeStoreIdParaSubscriptionDeStore() {
            when(planRepository.findPaidPlanByType(SubscriberType.STORE))
                    .thenReturn(Optional.of(TestFixtures.planStore()));
            when(subscriptionRepository.findActiveOrPending("STORE-1")).thenReturn(Optional.empty());
            when(subscriptionRepository.findLatestBySubscriber("STORE-1")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.start("STORE-1",
                    new StartSubscriptionRequest(SubscriberType.STORE,
                            "store@testuser.com", null, "900123456", null)))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessage(Messages.STORE_ID_REQUIRED);
        }

        @Test
        void rechazaSubscriptionVigenteDeStore() {
            when(planRepository.findPaidPlanByType(SubscriberType.STORE))
                    .thenReturn(Optional.of(TestFixtures.planStore()));
            when(subscriptionRepository.findActiveOrPending("STORE-1"))
                    .thenReturn(Optional.of(TestFixtures.activeStore("STORE-1", "900",
                            TestFixtures.HOY.minusDays(10), TestFixtures.HOY.plusDays(20))));

            assertThatThrownBy(() -> useCase.start("STORE-1",
                    new StartSubscriptionRequest(SubscriberType.STORE, "a@b.com", null, "900", null)))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage(Messages.STORE_ALREADY_SUBSCRIBED);
        }

        @Test
        void envuelveFalloDeMercadoPago() {
            when(planRepository.findPaidPlanByType(SubscriberType.USER))
                    .thenReturn(Optional.of(TestFixtures.planRadarPlus()));
            when(subscriptionRepository.findActiveOrPending("USER-1")).thenReturn(Optional.empty());
            when(subscriptionRepository.findLatestBySubscriber("USER-1")).thenReturn(Optional.empty());
            saveSubscriptionConId();
            when(mercadoPago.createPreapproval(any())).thenThrow(new IllegalStateException("MP ca‚Â­do"));

            assertThatThrownBy(() -> useCase.start("USER-1",
                    new StartSubscriptionRequest(SubscriberType.USER, "a@b.com", null, null, null)))
                    .isInstanceOf(PaymentProcessingException.class)
                    .hasMessage(Messages.PAYMENT_START_FAILED);
        }

        @Test
        void rechazaPreapprovalSinInitPoint() {
            when(planRepository.findPaidPlanByType(SubscriberType.USER))
                    .thenReturn(Optional.of(TestFixtures.planRadarPlus()));
            when(subscriptionRepository.findActiveOrPending("USER-1")).thenReturn(Optional.empty());
            when(subscriptionRepository.findLatestBySubscriber("USER-1")).thenReturn(Optional.empty());
            saveSubscriptionConId();
            when(mercadoPago.createPreapproval(any()))
                    .thenReturn(new MercadoPagoPort.PreapprovalCreated("PRE-1", " ", "pending"));

            assertThatThrownBy(() -> useCase.start("USER-1",
                    new StartSubscriptionRequest(SubscriberType.USER, "a@b.com", null, null, null)))
                    .isInstanceOf(PaymentProcessingException.class)
                    .hasMessage(Messages.PAYMENT_START_FAILED);
        }

        @Test
        void exigeSubscriberAutenticado() {
            assertThatThrownBy(() -> useCase.start(" ",
                    new StartSubscriptionRequest(SubscriberType.USER, "a@b.com", null, null, null)))
                    .isInstanceOf(UnauthorizedException.class);
        }
    }

    @Nested
    class Get {

        @Test
        void consultaPropiaConAccess() {
            when(subscriptionRepository.findLatestBySubscriber("USER-1"))
                    .thenReturn(Optional.of(TestFixtures.activeUser("USER-1",
                            TestFixtures.HOY.minusDays(10), TestFixtures.HOY.plusDays(20))));
            when(planRepository.findById(2L)).thenReturn(Optional.of(TestFixtures.planRadarPlus()));

            SubscriptionResponse response = useCase.get("USER-1", "USER-1", "USER");

            assertThat(response.access()).isTrue();
            assertThat(response.status()).isEqualTo(SubscriptionStatus.ACTIVE);
            assertThat(response.plan().name()).isEqualTo("Radar+");
            assertThat(response.message()).isNull();
        }

        @Test
        void administradorPuedeGetAjena() {
            when(subscriptionRepository.findLatestBySubscriber("USER-1"))
                    .thenReturn(Optional.of(TestFixtures.activeUser("USER-1",
                            TestFixtures.HOY.minusDays(10), TestFixtures.HOY.plusDays(20))));
            when(planRepository.findById(2L)).thenReturn(Optional.of(TestFixtures.planRadarPlus()));

            assertThat(useCase.get("USER-1", "ADMIN-1", "ADMIN").subscriberId())
                    .isEqualTo("USER-1");
        }

        @Test
        void userNoPuedeGetAjena() {
            when(subscriptionRepository.findLatestBySubscriber("USER-1"))
                    .thenReturn(Optional.of(TestFixtures.activeUser("USER-1",
                            TestFixtures.HOY.minusDays(10), TestFixtures.HOY.plusDays(20))));

            assertThatThrownBy(() -> useCase.get("USER-1", "USER-2", "USER"))
                    .isInstanceOf(OperationNotAllowedException.class)
                    .hasMessage(Messages.OWNER_ONLY);
        }

        @Test
        void exigeSolicitante() {
            when(subscriptionRepository.findLatestBySubscriber("USER-1"))
                    .thenReturn(Optional.of(TestFixtures.activeUser("USER-1",
                            TestFixtures.HOY.minusDays(10), TestFixtures.HOY.plusDays(20))));

            assertThatThrownBy(() -> useCase.get("USER-1", null, null))
                    .isInstanceOf(UnauthorizedException.class);
        }

        @Test
        void fallaSiNoExisteSubscription() {
            when(subscriptionRepository.findLatestBySubscriber("USER-1")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.get("USER-1", "USER-1", "USER"))
                    .isInstanceOf(SubscriptionNotFoundException.class);
        }

        @Test
        void fallaSiNoExistePlan() {
            when(subscriptionRepository.findLatestBySubscriber("USER-1"))
                    .thenReturn(Optional.of(TestFixtures.activeUser("USER-1",
                            TestFixtures.HOY.minusDays(10), TestFixtures.HOY.plusDays(20))));
            when(planRepository.findById(2L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.get("USER-1", "USER-1", "USER"))
                    .isInstanceOf(PlanNotFoundException.class);
        }

        @Test
        void informaPendingYSuspended() {
            when(subscriptionRepository.findLatestBySubscriber("USER-1"))
                    .thenReturn(Optional.of(TestFixtures.pendingUser("USER-1")));
            when(planRepository.findById(2L)).thenReturn(Optional.of(TestFixtures.planRadarPlus()));
            assertThat(useCase.get("USER-1", "USER-1", "USER").message())
                    .isEqualTo(Messages.PAYMENT_PENDING);

            when(subscriptionRepository.findLatestBySubscriber("USER-1"))
                    .thenReturn(Optional.of(TestFixtures.suspendedUser("USER-1")));
            assertThat(useCase.get("USER-1", "USER-1", "USER").message())
                    .isEqualTo(Messages.RADAR_PLUS_ENDED);
        }
    }

    @Nested
    class Cancel {

        @Test
        void cancelaRenovacionEnMercadoPagoYGuarda() {
            Subscription active = TestFixtures.activeUser("USER-1",
                    TestFixtures.HOY.minusDays(10), TestFixtures.HOY.plusDays(20));
            when(subscriptionRepository.findLatestBySubscriber("USER-1")).thenReturn(Optional.of(active));
            when(subscriptionRepository.save(any(Subscription.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            CancellationResponse response = useCase.cancel("USER-1", true, "USER-1", "USER");

            assertThat(response.autoRenewal()).isFalse();
            assertThat(response.status()).isEqualTo(SubscriptionStatus.ACTIVE);
            assertThat(response.endDate()).isEqualTo(TestFixtures.HOY.plusDays(20));
            assertThat(response.cancellationConfirmed()).isTrue();
            verify(mercadoPago).cancelPreapproval("PRE-100");
        }

        @Test
        void cancelaRenovacionDeStoreConMessagePropio() {
            Subscription active = TestFixtures.activeStore("STORE-1", "900",
                    TestFixtures.HOY.minusDays(10), TestFixtures.HOY.plusDays(20));
            when(subscriptionRepository.findLatestBySubscriber("STORE-1")).thenReturn(Optional.of(active));
            when(subscriptionRepository.save(any(Subscription.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            CancellationResponse response = useCase.cancel("STORE-1", true, "STORE-1", "STORE");

            assertThat(response.message()).contains("acceso");
        }

        @Test
        void esIdempotenteSiYaEstabaDesactiveda() {
            Subscription active = TestFixtures.activeUser("USER-1",
                    TestFixtures.HOY.minusDays(10), TestFixtures.HOY.plusDays(20));
            active.cancelRenewal();
            when(subscriptionRepository.findLatestBySubscriber("USER-1")).thenReturn(Optional.of(active));

            CancellationResponse response = useCase.cancel("USER-1", true, "USER-1", "USER");

            assertThat(response.autoRenewal()).isFalse();
            verify(mercadoPago, never()).cancelPreapproval(anyString());
        }

        @Test
        void exigeConfirmed() {
            assertThatThrownBy(() -> useCase.cancel("USER-1", false, "USER-1", "USER"))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("confirmar");
        }

        @Test
        void exigeSubscriptionActive() {
            when(subscriptionRepository.findLatestBySubscriber("USER-1"))
                    .thenReturn(Optional.of(TestFixtures.pendingUser("USER-1")));

            assertThatThrownBy(() -> useCase.cancel("USER-1", true, "USER-1", "USER"))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessage(Messages.SUBSCRIPTION_NOT_ACTIVE);
        }

        @Test
        void fallaSiNoExiste() {
            when(subscriptionRepository.findLatestBySubscriber("USER-1")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.cancel("USER-1", true, "USER-1", "USER"))
                    .isInstanceOf(SubscriptionNotFoundException.class);
        }

        @Test
        void envuelveFalloDeMercadoPagoParaUserYStore() {
            when(subscriptionRepository.findLatestBySubscriber("USER-1"))
                    .thenReturn(Optional.of(TestFixtures.activeUser("USER-1",
                            TestFixtures.HOY.minusDays(10), TestFixtures.HOY.plusDays(20))));
            org.mockito.Mockito.doThrow(new IllegalStateException("MP"))
                    .when(mercadoPago).cancelPreapproval("PRE-100");

            assertThatThrownBy(() -> useCase.cancel("USER-1", true, "USER-1", "USER"))
                    .isInstanceOf(PaymentProcessingException.class)
                    .hasMessage(Messages.RADAR_PLUS_RENEWAL_CANCEL_FAILED);

            when(subscriptionRepository.findLatestBySubscriber("STORE-1"))
                    .thenReturn(Optional.of(TestFixtures.activeStore("STORE-1", "900",
                            TestFixtures.HOY.minusDays(10), TestFixtures.HOY.plusDays(20))));
            org.mockito.Mockito.doThrow(new IllegalStateException("MP"))
                    .when(mercadoPago).cancelPreapproval("PRE-200");

            assertThatThrownBy(() -> useCase.cancel("STORE-1", true, "STORE-1", "STORE"))
                    .isInstanceOf(PaymentProcessingException.class)
                    .hasMessage(Messages.RENEWAL_CANCEL_FAILED);
        }
    }
}




