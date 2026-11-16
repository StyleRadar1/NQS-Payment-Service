package edu.dosw.styleradar.pagos.application.usecase;

import edu.dosw.styleradar.pagos.TestFixtures;
import edu.dosw.styleradar.pagos.application.dto.request.IniciarSuscripcionRequest;
import edu.dosw.styleradar.pagos.application.dto.response.CancelacionResponse;
import edu.dosw.styleradar.pagos.application.dto.response.IniciarSuscripcionResponse;
import edu.dosw.styleradar.pagos.application.dto.response.SuscripcionResponse;
import edu.dosw.styleradar.pagos.application.port.out.MercadoPagoPort;
import edu.dosw.styleradar.pagos.application.port.out.OrdenPagoRepositoryPort;
import edu.dosw.styleradar.pagos.application.port.out.PlanRepositoryPort;
import edu.dosw.styleradar.pagos.application.port.out.SuscripcionRepositoryPort;
import edu.dosw.styleradar.pagos.domain.exception.Mensajes;
import edu.dosw.styleradar.pagos.domain.exception.NoAutorizadoException;
import edu.dosw.styleradar.pagos.domain.exception.OperacionNoPermitidaException;
import edu.dosw.styleradar.pagos.domain.exception.PaymentProcessingException;
import edu.dosw.styleradar.pagos.domain.exception.PlanNotFoundException;
import edu.dosw.styleradar.pagos.domain.exception.SolicitudInvalidaException;
import edu.dosw.styleradar.pagos.domain.exception.SuscripcionNoEncontradaException;
import edu.dosw.styleradar.pagos.domain.model.EstadoSuscripcion;
import edu.dosw.styleradar.pagos.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos.domain.model.Suscripcion;
import edu.dosw.styleradar.pagos.domain.model.TipoSuscriptor;
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
@DisplayName("SuscripcionUseCase (SR-RF-67/68/93/100/101)")
class SuscripcionUseCaseTest {

    @Mock
    private PlanRepositoryPort planRepository;
    @Mock
    private SuscripcionRepositoryPort suscripcionRepository;
    @Mock
    private OrdenPagoRepositoryPort ordenPagoRepository;
    @Mock
    private MercadoPagoPort mercadoPago;

    private SuscripcionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new SuscripcionUseCase(planRepository, suscripcionRepository,
                ordenPagoRepository, mercadoPago, TestFixtures.CLOCK, "https://app/back");
    }

    private void guardarSuscripcionConId() {
        when(suscripcionRepository.guardar(any(Suscripcion.class)))
                .thenAnswer(inv -> TestFixtures.conIdSuscripcion(inv.getArgument(0), 100L));
    }

    private void guardarOrdenConId() {
        when(ordenPagoRepository.guardar(any(OrdenPago.class)))
                .thenAnswer(inv -> TestFixtures.conIdOrden(inv.getArgument(0), 200L));
    }

    private MercadoPagoPort.PreapprovalCreado preapproval() {
        return new MercadoPagoPort.PreapprovalCreado("PRE-1",
                "https://www.mercadopago.com.co/subscriptions/checkout?preapproval_id=PRE-1", "pending");
    }

    @Nested
    @DisplayName("Iniciar pago")
    class Iniciar {

        @Test
        void iniciaSuscripcionDeUsuarioConRadarPlus() {
            when(planRepository.buscarPlanDePagoPorTipo(TipoSuscriptor.USUARIO))
                    .thenReturn(Optional.of(TestFixtures.planRadarPlus()));
            when(suscripcionRepository.buscarActivaOPendiente("USER-1")).thenReturn(Optional.empty());
            when(suscripcionRepository.buscarUltimaPorSuscriptor("USER-1")).thenReturn(Optional.empty());
            guardarSuscripcionConId();
            guardarOrdenConId();
            when(mercadoPago.crearPreapproval(any())).thenReturn(preapproval());

            IniciarSuscripcionResponse response = useCase.iniciar("USER-1",
                    new IniciarSuscripcionRequest(TipoSuscriptor.USUARIO,
                            "pagador@testuser.com", 2L, null, null));

            assertThat(response.suscripcionId()).isEqualTo(100L);
            assertThat(response.ordenId()).isEqualTo(200L);
            assertThat(response.estado()).isEqualTo(EstadoSuscripcion.PENDIENTE);
            assertThat(response.initPoint()).contains("preapproval_id=PRE-1");
            assertThat(response.mensaje()).contains("Radar+");

            ArgumentCaptor<MercadoPagoPort.PreapprovalSolicitud> solicitud =
                    ArgumentCaptor.forClass(MercadoPagoPort.PreapprovalSolicitud.class);
            verify(mercadoPago).crearPreapproval(solicitud.capture());
            assertThat(solicitud.getValue().referenciaExterna()).isEqualTo("SUS-100");
            assertThat(solicitud.getValue().correoPagador()).isEqualTo("pagador@testuser.com");
            assertThat(solicitud.getValue().monto().valor()).isEqualByComparingTo("14900.00");
            assertThat(solicitud.getValue().urlRetorno()).isEqualTo("https://app/back");

            ArgumentCaptor<Suscripcion> guardadas = ArgumentCaptor.forClass(Suscripcion.class);
            verify(suscripcionRepository, org.mockito.Mockito.times(2)).guardar(guardadas.capture());
            assertThat(guardadas.getAllValues().get(1).getPreapprovalIdMp()).isEqualTo("PRE-1");
        }

        @Test
        void iniciaSuscripcionDeAlmacenConNitYPlanUnico() {
            when(planRepository.buscarPlanDePagoPorTipo(TipoSuscriptor.ALMACEN))
                    .thenReturn(Optional.of(TestFixtures.planAlmacen()));
            when(suscripcionRepository.buscarActivaOPendiente("ALMACEN-1")).thenReturn(Optional.empty());
            when(suscripcionRepository.buscarUltimaPorSuscriptor("ALMACEN-1")).thenReturn(Optional.empty());
            guardarSuscripcionConId();
            guardarOrdenConId();
            when(mercadoPago.crearPreapproval(any())).thenReturn(preapproval());

            IniciarSuscripcionResponse response = useCase.iniciar("ALMACEN-1",
                    new IniciarSuscripcionRequest(TipoSuscriptor.ALMACEN,
                            "almacen@testuser.com", 2L, "900123456-7", java.util.UUID.fromString("a1111111-1111-1111-1111-111111111111")));

            assertThat(response.mensaje()).contains("almacén");
            ArgumentCaptor<Suscripcion> guardadas = ArgumentCaptor.forClass(Suscripcion.class);
            verify(suscripcionRepository, org.mockito.Mockito.times(2)).guardar(guardadas.capture());
            assertThat(guardadas.getAllValues().get(0).getNit()).isEqualTo("900123456-7");
            assertThat(guardadas.getAllValues().get(0).getPlanId()).isEqualTo(3L);
        }

        @Test
        void reutilizaSuscripcionSuspendidaDelMismoPlan() {
            when(planRepository.buscarPlanDePagoPorTipo(TipoSuscriptor.USUARIO))
                    .thenReturn(Optional.of(TestFixtures.planRadarPlus()));
            when(suscripcionRepository.buscarActivaOPendiente("USER-1")).thenReturn(Optional.empty());
            when(suscripcionRepository.buscarUltimaPorSuscriptor("USER-1"))
                    .thenReturn(Optional.of(TestFixtures.suspendidaUsuario("USER-1")));
            guardarSuscripcionConId();
            guardarOrdenConId();
            when(mercadoPago.crearPreapproval(any())).thenReturn(preapproval());

            useCase.iniciar("USER-1", new IniciarSuscripcionRequest(TipoSuscriptor.USUARIO,
                    "pagador@testuser.com", 2L, null, null));

            ArgumentCaptor<Suscripcion> guardadas = ArgumentCaptor.forClass(Suscripcion.class);
            verify(suscripcionRepository, org.mockito.Mockito.times(2)).guardar(guardadas.capture());
            assertThat(guardadas.getAllValues().get(0).getId()).isEqualTo(101L);
        }

        @Test
        void validaCorreoDeUsuarioYAlmacen() {
            assertThatThrownBy(() -> useCase.iniciar("USER-1",
                    new IniciarSuscripcionRequest(TipoSuscriptor.USUARIO, "a@b", null, null, null)))
                    .isInstanceOf(SolicitudInvalidaException.class)
                    .hasMessage(Mensajes.CORREO_INVALIDO_RADAR_PLUS);

            assertThatThrownBy(() -> useCase.iniciar("ALMACEN-1",
                    new IniciarSuscripcionRequest(TipoSuscriptor.ALMACEN, "sin-correo", null, null, null)))
                    .isInstanceOf(SolicitudInvalidaException.class)
                    .hasMessage(Mensajes.CORREO_INVALIDO);
        }

        @Test
        void exigePlanDisponible() {
            when(planRepository.buscarPlanDePagoPorTipo(TipoSuscriptor.USUARIO))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.iniciar("USER-1",
                    new IniciarSuscripcionRequest(TipoSuscriptor.USUARIO, "a@b.com", null, null, null)))
                    .isInstanceOf(PlanNotFoundException.class);
        }

        @Test
        void rechazaPlanDistintoParaUsuario() {
            when(planRepository.buscarPlanDePagoPorTipo(TipoSuscriptor.USUARIO))
                    .thenReturn(Optional.of(TestFixtures.planRadarPlus()));

            assertThatThrownBy(() -> useCase.iniciar("USER-1",
                    new IniciarSuscripcionRequest(TipoSuscriptor.USUARIO, "a@b.com", 3L, null, null)))
                    .isInstanceOf(PlanNotFoundException.class);
        }

        @Test
        void rechazaSuscripcionVigenteDeUsuario() {
            when(planRepository.buscarPlanDePagoPorTipo(TipoSuscriptor.USUARIO))
                    .thenReturn(Optional.of(TestFixtures.planRadarPlus()));
            when(suscripcionRepository.buscarActivaOPendiente("USER-1"))
                    .thenReturn(Optional.of(TestFixtures.activaUsuario("USER-1",
                            TestFixtures.HOY.minusDays(10), TestFixtures.HOY.plusDays(20))));

            assertThatThrownBy(() -> useCase.iniciar("USER-1",
                    new IniciarSuscripcionRequest(TipoSuscriptor.USUARIO, "a@b.com", null, null, null)))
                    .isInstanceOf(OperacionNoPermitidaException.class)
                    .hasMessage(Mensajes.RADAR_PLUS_ACTIVO);
        }

        @Test
        void exigeAlmacenIdParaSuscripcionDeAlmacen() {
            when(planRepository.buscarPlanDePagoPorTipo(TipoSuscriptor.ALMACEN))
                    .thenReturn(Optional.of(TestFixtures.planAlmacen()));
            when(suscripcionRepository.buscarActivaOPendiente("ALMACEN-1")).thenReturn(Optional.empty());
            when(suscripcionRepository.buscarUltimaPorSuscriptor("ALMACEN-1")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.iniciar("ALMACEN-1",
                    new IniciarSuscripcionRequest(TipoSuscriptor.ALMACEN,
                            "almacen@testuser.com", null, "900123456-7", null)))
                    .isInstanceOf(SolicitudInvalidaException.class)
                    .hasMessage(Mensajes.ALMACEN_ID_REQUERIDO);
        }

        @Test
        void rechazaSuscripcionVigenteDeAlmacen() {
            when(planRepository.buscarPlanDePagoPorTipo(TipoSuscriptor.ALMACEN))
                    .thenReturn(Optional.of(TestFixtures.planAlmacen()));
            when(suscripcionRepository.buscarActivaOPendiente("ALMACEN-1"))
                    .thenReturn(Optional.of(TestFixtures.activaAlmacen("ALMACEN-1", "900",
                            TestFixtures.HOY.minusDays(10), TestFixtures.HOY.plusDays(20))));

            assertThatThrownBy(() -> useCase.iniciar("ALMACEN-1",
                    new IniciarSuscripcionRequest(TipoSuscriptor.ALMACEN, "a@b.com", null, "900", null)))
                    .isInstanceOf(OperacionNoPermitidaException.class)
                    .hasMessage(Mensajes.ALMACEN_YA_SUSCRITO);
        }

        @Test
        void envuelveFalloDeMercadoPago() {
            when(planRepository.buscarPlanDePagoPorTipo(TipoSuscriptor.USUARIO))
                    .thenReturn(Optional.of(TestFixtures.planRadarPlus()));
            when(suscripcionRepository.buscarActivaOPendiente("USER-1")).thenReturn(Optional.empty());
            when(suscripcionRepository.buscarUltimaPorSuscriptor("USER-1")).thenReturn(Optional.empty());
            guardarSuscripcionConId();
            when(mercadoPago.crearPreapproval(any())).thenThrow(new IllegalStateException("MP caído"));

            assertThatThrownBy(() -> useCase.iniciar("USER-1",
                    new IniciarSuscripcionRequest(TipoSuscriptor.USUARIO, "a@b.com", null, null, null)))
                    .isInstanceOf(PaymentProcessingException.class)
                    .hasMessage(Mensajes.NO_INICIAR_PAGO);
        }

        @Test
        void rechazaPreapprovalSinInitPoint() {
            when(planRepository.buscarPlanDePagoPorTipo(TipoSuscriptor.USUARIO))
                    .thenReturn(Optional.of(TestFixtures.planRadarPlus()));
            when(suscripcionRepository.buscarActivaOPendiente("USER-1")).thenReturn(Optional.empty());
            when(suscripcionRepository.buscarUltimaPorSuscriptor("USER-1")).thenReturn(Optional.empty());
            guardarSuscripcionConId();
            when(mercadoPago.crearPreapproval(any()))
                    .thenReturn(new MercadoPagoPort.PreapprovalCreado("PRE-1", " ", "pending"));

            assertThatThrownBy(() -> useCase.iniciar("USER-1",
                    new IniciarSuscripcionRequest(TipoSuscriptor.USUARIO, "a@b.com", null, null, null)))
                    .isInstanceOf(PaymentProcessingException.class)
                    .hasMessage(Mensajes.NO_INICIAR_PAGO);
        }

        @Test
        void exigeSuscriptorAutenticado() {
            assertThatThrownBy(() -> useCase.iniciar(" ",
                    new IniciarSuscripcionRequest(TipoSuscriptor.USUARIO, "a@b.com", null, null, null)))
                    .isInstanceOf(NoAutorizadoException.class);
        }
    }

    @Nested
    @DisplayName("Consultar suscripción")
    class Consultar {

        @Test
        void consultaPropiaConAcceso() {
            when(suscripcionRepository.buscarUltimaPorSuscriptor("USER-1"))
                    .thenReturn(Optional.of(TestFixtures.activaUsuario("USER-1",
                            TestFixtures.HOY.minusDays(10), TestFixtures.HOY.plusDays(20))));
            when(planRepository.buscarPorId(2L)).thenReturn(Optional.of(TestFixtures.planRadarPlus()));

            SuscripcionResponse response = useCase.consultar("USER-1", "USER-1", "USUARIO");

            assertThat(response.acceso()).isTrue();
            assertThat(response.estado()).isEqualTo(EstadoSuscripcion.ACTIVA);
            assertThat(response.plan().nombre()).isEqualTo("Radar+");
            assertThat(response.mensaje()).isNull();
        }

        @Test
        void administradorPuedeConsultarAjena() {
            when(suscripcionRepository.buscarUltimaPorSuscriptor("USER-1"))
                    .thenReturn(Optional.of(TestFixtures.activaUsuario("USER-1",
                            TestFixtures.HOY.minusDays(10), TestFixtures.HOY.plusDays(20))));
            when(planRepository.buscarPorId(2L)).thenReturn(Optional.of(TestFixtures.planRadarPlus()));

            assertThat(useCase.consultar("USER-1", "ADMIN-1", "ADMIN").suscriptorId())
                    .isEqualTo("USER-1");
        }

        @Test
        void usuarioNoPuedeConsultarAjena() {
            when(suscripcionRepository.buscarUltimaPorSuscriptor("USER-1"))
                    .thenReturn(Optional.of(TestFixtures.activaUsuario("USER-1",
                            TestFixtures.HOY.minusDays(10), TestFixtures.HOY.plusDays(20))));

            assertThatThrownBy(() -> useCase.consultar("USER-1", "USER-2", "USUARIO"))
                    .isInstanceOf(OperacionNoPermitidaException.class)
                    .hasMessage(Mensajes.SOLO_TITULAR);
        }

        @Test
        void exigeSolicitante() {
            when(suscripcionRepository.buscarUltimaPorSuscriptor("USER-1"))
                    .thenReturn(Optional.of(TestFixtures.activaUsuario("USER-1",
                            TestFixtures.HOY.minusDays(10), TestFixtures.HOY.plusDays(20))));

            assertThatThrownBy(() -> useCase.consultar("USER-1", null, null))
                    .isInstanceOf(NoAutorizadoException.class);
        }

        @Test
        void fallaSiNoExisteSuscripcion() {
            when(suscripcionRepository.buscarUltimaPorSuscriptor("USER-1")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.consultar("USER-1", "USER-1", "USUARIO"))
                    .isInstanceOf(SuscripcionNoEncontradaException.class);
        }

        @Test
        void fallaSiNoExistePlan() {
            when(suscripcionRepository.buscarUltimaPorSuscriptor("USER-1"))
                    .thenReturn(Optional.of(TestFixtures.activaUsuario("USER-1",
                            TestFixtures.HOY.minusDays(10), TestFixtures.HOY.plusDays(20))));
            when(planRepository.buscarPorId(2L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.consultar("USER-1", "USER-1", "USUARIO"))
                    .isInstanceOf(PlanNotFoundException.class);
        }

        @Test
        void informaPendienteYSuspendida() {
            when(suscripcionRepository.buscarUltimaPorSuscriptor("USER-1"))
                    .thenReturn(Optional.of(TestFixtures.pendienteUsuario("USER-1")));
            when(planRepository.buscarPorId(2L)).thenReturn(Optional.of(TestFixtures.planRadarPlus()));
            assertThat(useCase.consultar("USER-1", "USER-1", "USUARIO").mensaje())
                    .isEqualTo(Mensajes.PAGO_PENDIENTE);

            when(suscripcionRepository.buscarUltimaPorSuscriptor("USER-1"))
                    .thenReturn(Optional.of(TestFixtures.suspendidaUsuario("USER-1")));
            assertThat(useCase.consultar("USER-1", "USER-1", "USUARIO").mensaje())
                    .isEqualTo(Mensajes.RADAR_PLUS_TERMINO);
        }
    }

    @Nested
    @DisplayName("Cancelar renovación")
    class Cancelar {

        @Test
        void cancelaRenovacionEnMercadoPagoYGuarda() {
            Suscripcion activa = TestFixtures.activaUsuario("USER-1",
                    TestFixtures.HOY.minusDays(10), TestFixtures.HOY.plusDays(20));
            when(suscripcionRepository.buscarUltimaPorSuscriptor("USER-1")).thenReturn(Optional.of(activa));
            when(suscripcionRepository.guardar(any(Suscripcion.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            CancelacionResponse response = useCase.cancelar("USER-1", true, "USER-1", "USUARIO");

            assertThat(response.autoRenovacion()).isFalse();
            assertThat(response.estado()).isEqualTo(EstadoSuscripcion.ACTIVA);
            assertThat(response.fechaFin()).isEqualTo(TestFixtures.HOY.plusDays(20));
            assertThat(response.cancelacionConfirmada()).isTrue();
            verify(mercadoPago).cancelarPreapproval("PRE-100");
        }

        @Test
        void cancelaRenovacionDeAlmacenConMensajePropio() {
            Suscripcion activa = TestFixtures.activaAlmacen("ALMACEN-1", "900",
                    TestFixtures.HOY.minusDays(10), TestFixtures.HOY.plusDays(20));
            when(suscripcionRepository.buscarUltimaPorSuscriptor("ALMACEN-1")).thenReturn(Optional.of(activa));
            when(suscripcionRepository.guardar(any(Suscripcion.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            CancelacionResponse response = useCase.cancelar("ALMACEN-1", true, "ALMACEN-1", "ALMACEN");

            assertThat(response.mensaje()).contains("acceso");
        }

        @Test
        void esIdempotenteSiYaEstabaDesactivada() {
            Suscripcion activa = TestFixtures.activaUsuario("USER-1",
                    TestFixtures.HOY.minusDays(10), TestFixtures.HOY.plusDays(20));
            activa.cancelarRenovacion();
            when(suscripcionRepository.buscarUltimaPorSuscriptor("USER-1")).thenReturn(Optional.of(activa));

            CancelacionResponse response = useCase.cancelar("USER-1", true, "USER-1", "USUARIO");

            assertThat(response.autoRenovacion()).isFalse();
            verify(mercadoPago, never()).cancelarPreapproval(anyString());
        }

        @Test
        void exigeConfirmacion() {
            assertThatThrownBy(() -> useCase.cancelar("USER-1", false, "USER-1", "USUARIO"))
                    .isInstanceOf(SolicitudInvalidaException.class)
                    .hasMessageContaining("confirmar");
        }

        @Test
        void exigeSuscripcionActiva() {
            when(suscripcionRepository.buscarUltimaPorSuscriptor("USER-1"))
                    .thenReturn(Optional.of(TestFixtures.pendienteUsuario("USER-1")));

            assertThatThrownBy(() -> useCase.cancelar("USER-1", true, "USER-1", "USUARIO"))
                    .isInstanceOf(SolicitudInvalidaException.class)
                    .hasMessage(Mensajes.SUSCRIPCION_NO_VIGENTE);
        }

        @Test
        void fallaSiNoExiste() {
            when(suscripcionRepository.buscarUltimaPorSuscriptor("USER-1")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.cancelar("USER-1", true, "USER-1", "USUARIO"))
                    .isInstanceOf(SuscripcionNoEncontradaException.class);
        }

        @Test
        void envuelveFalloDeMercadoPagoParaUsuarioYAlmacen() {
            when(suscripcionRepository.buscarUltimaPorSuscriptor("USER-1"))
                    .thenReturn(Optional.of(TestFixtures.activaUsuario("USER-1",
                            TestFixtures.HOY.minusDays(10), TestFixtures.HOY.plusDays(20))));
            org.mockito.Mockito.doThrow(new IllegalStateException("MP"))
                    .when(mercadoPago).cancelarPreapproval("PRE-100");

            assertThatThrownBy(() -> useCase.cancelar("USER-1", true, "USER-1", "USUARIO"))
                    .isInstanceOf(PaymentProcessingException.class)
                    .hasMessage(Mensajes.NO_CANCELAR_RADAR_PLUS);

            when(suscripcionRepository.buscarUltimaPorSuscriptor("ALMACEN-1"))
                    .thenReturn(Optional.of(TestFixtures.activaAlmacen("ALMACEN-1", "900",
                            TestFixtures.HOY.minusDays(10), TestFixtures.HOY.plusDays(20))));
            org.mockito.Mockito.doThrow(new IllegalStateException("MP"))
                    .when(mercadoPago).cancelarPreapproval("PRE-200");

            assertThatThrownBy(() -> useCase.cancelar("ALMACEN-1", true, "ALMACEN-1", "ALMACEN"))
                    .isInstanceOf(PaymentProcessingException.class)
                    .hasMessage(Mensajes.NO_CANCELAR_RENOVACION);
        }
    }
}
