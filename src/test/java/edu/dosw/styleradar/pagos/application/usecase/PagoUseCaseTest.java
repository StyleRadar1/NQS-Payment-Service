package edu.dosw.styleradar.pagos.application.usecase;

import edu.dosw.styleradar.pagos.TestFixtures;
import edu.dosw.styleradar.pagos.application.dto.response.PagoResponse;
import edu.dosw.styleradar.pagos.application.dto.response.ReembolsoResponse;
import edu.dosw.styleradar.pagos.application.dto.response.ReintentoResponse;
import edu.dosw.styleradar.pagos.application.port.out.MercadoPagoPort;
import edu.dosw.styleradar.pagos.application.port.out.OrdenPagoRepositoryPort;
import edu.dosw.styleradar.pagos.application.port.out.SuscripcionRepositoryPort;
import edu.dosw.styleradar.pagos.domain.exception.Mensajes;
import edu.dosw.styleradar.pagos.domain.exception.NoAutorizadoException;
import edu.dosw.styleradar.pagos.domain.exception.OperacionNoPermitidaException;
import edu.dosw.styleradar.pagos.domain.exception.OrdenPagoNoEncontradaException;
import edu.dosw.styleradar.pagos.domain.exception.PaymentProcessingException;
import edu.dosw.styleradar.pagos.domain.exception.SolicitudInvalidaException;
import edu.dosw.styleradar.pagos.domain.exception.SuscripcionNoEncontradaException;
import edu.dosw.styleradar.pagos.domain.model.EstadoPago;
import edu.dosw.styleradar.pagos.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos.domain.model.Suscripcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
@DisplayName("PagoUseCase (SR-RF-96/97/98)")
class PagoUseCaseTest {

    @Mock
    private OrdenPagoRepositoryPort ordenPagoRepository;
    @Mock
    private SuscripcionRepositoryPort suscripcionRepository;
    @Mock
    private MercadoPagoPort mercadoPago;

    private PagoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new PagoUseCase(ordenPagoRepository, suscripcionRepository, mercadoPago,
                TestFixtures.CLOCK, "https://app/back");
    }

    @Test
    void historialMapeaPagosYExigeIdentidad() {
        OrdenPago orden = TestFixtures.conIdOrden(TestFixtures.aprobada(100L, "14900"), 1L);
        when(ordenPagoRepository.buscarPorSuscriptor("USER-1")).thenReturn(List.of(orden));

        List<PagoResponse> historial = useCase.historial("USER-1");

        assertThat(historial).hasSize(1);
        assertThat(historial.get(0).id()).isEqualTo(1L);
        assertThat(historial.get(0).estado()).isEqualTo(EstadoPago.APROBADO);
        assertThatThrownBy(() -> useCase.historial(" "))
                .isInstanceOf(NoAutorizadoException.class);
    }

    @Nested
    @DisplayName("Reintentar pago rechazado (SR-RF-96)")
    class Reintentar {

        private OrdenPago rechazada() {
            OrdenPago orden = TestFixtures.pendiente(100L, "14900");
            orden.actualizarEstado(EstadoPago.RECHAZADO, "MP-1");
            return TestFixtures.conIdOrden(orden, 10L);
        }

        private Suscripcion pendienteConPreapproval() {
            Suscripcion suscripcion = TestFixtures.pendienteUsuario("USER-1");
            suscripcion.asignarPreapproval("PRE-OLD");
            return TestFixtures.conIdSuscripcion(suscripcion, 100L);
        }

        @Test
        void creaNuevaOrdenYEnlace() {
            OrdenPago rechazada = rechazada();
            Suscripcion suscripcion = pendienteConPreapproval();
            when(ordenPagoRepository.buscarPorId(10L)).thenReturn(Optional.of(rechazada));
            when(suscripcionRepository.buscarPorId(100L)).thenReturn(Optional.of(suscripcion));
            when(ordenPagoRepository.guardar(any(OrdenPago.class)))
                    .thenAnswer(inv -> TestFixtures.conIdOrden(inv.getArgument(0), 11L));
            when(mercadoPago.crearPreapproval(any()))
                    .thenReturn(new MercadoPagoPort.PreapprovalCreado("PRE-NEW", "https://mp/new", "pending"));

            ReintentoResponse response = useCase.reintentar(10L, "pagador@testuser.com", "USER-1");

            assertThat(response.ordenId()).isEqualTo(11L);
            assertThat(response.estado()).isEqualTo(EstadoPago.PENDIENTE);
            assertThat(response.initPoint()).isEqualTo("https://mp/new");
            verify(suscripcionRepository).guardar(suscripcion);
            verify(mercadoPago).cancelarPreapproval("PRE-OLD");
            assertThat(rechazada.getEstado()).isEqualTo(EstadoPago.RECHAZADO);
        }

        @Test
        void fallaSiNoExisteOrdenOSuscripcion() {
            when(ordenPagoRepository.buscarPorId(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> useCase.reintentar(99L, "a@b.com", "USER-1"))
                    .isInstanceOf(OrdenPagoNoEncontradaException.class);

            when(ordenPagoRepository.buscarPorId(10L)).thenReturn(Optional.of(rechazada()));
            when(suscripcionRepository.buscarPorId(100L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> useCase.reintentar(10L, "a@b.com", "USER-1"))
                    .isInstanceOf(SuscripcionNoEncontradaException.class);
        }

        @Test
        void exigeTitularidadYCorreoValido() {
            when(ordenPagoRepository.buscarPorId(10L)).thenReturn(Optional.of(rechazada()));
            when(suscripcionRepository.buscarPorId(100L)).thenReturn(Optional.of(pendienteConPreapproval()));

            assertThatThrownBy(() -> useCase.reintentar(10L, "a@b.com", "USER-2"))
                    .isInstanceOf(OperacionNoPermitidaException.class)
                    .hasMessage(Mensajes.SOLO_TITULAR);
            assertThatThrownBy(() -> useCase.reintentar(10L, "a@b.com", null))
                    .isInstanceOf(NoAutorizadoException.class);
            assertThatThrownBy(() -> useCase.reintentar(10L, "sin-correo", "USER-1"))
                    .isInstanceOf(SolicitudInvalidaException.class)
                    .hasMessage(Mensajes.CORREO_INVALIDO);
        }

        @Test
        void noReintentaSiLaSuscripcionEstaActivaONoEstaRechazada() {
            Suscripcion activa = TestFixtures.activaUsuario("USER-1",
                    TestFixtures.HOY.minusDays(10), TestFixtures.HOY.plusDays(20));
            when(ordenPagoRepository.buscarPorId(10L)).thenReturn(Optional.of(rechazada()));
            when(suscripcionRepository.buscarPorId(100L)).thenReturn(Optional.of(activa));

            assertThatThrownBy(() -> useCase.reintentar(10L, "a@b.com", "USER-1"))
                    .isInstanceOf(SolicitudInvalidaException.class)
                    .hasMessage(Mensajes.SUSCRIPCION_ACTIVA_NO_REINTENTO);

            Suscripcion pendiente = TestFixtures.pendienteUsuario("USER-1");
            pendiente.asignarPreapproval("PRE-OLD");
            OrdenPago aprobada = TestFixtures.conIdOrden(TestFixtures.aprobada(100L, "14900"), 10L);
            when(ordenPagoRepository.buscarPorId(10L)).thenReturn(Optional.of(aprobada));
            when(suscripcionRepository.buscarPorId(100L)).thenReturn(Optional.of(pendiente));

            assertThatThrownBy(() -> useCase.reintentar(10L, "a@b.com", "USER-1"))
                    .isInstanceOf(SolicitudInvalidaException.class)
                    .hasMessage(Mensajes.ORDEN_NO_RECHAZADA);
        }

        @Test
        void envuelveFalloDeMercadoPago() {
            when(ordenPagoRepository.buscarPorId(10L)).thenReturn(Optional.of(rechazada()));
            when(suscripcionRepository.buscarPorId(100L)).thenReturn(Optional.of(pendienteConPreapproval()));
            when(ordenPagoRepository.guardar(any(OrdenPago.class)))
                    .thenAnswer(inv -> TestFixtures.conIdOrden(inv.getArgument(0), 11L));
            when(mercadoPago.crearPreapproval(any())).thenThrow(new IllegalStateException("MP"));

            assertThatThrownBy(() -> useCase.reintentar(10L, "a@b.com", "USER-1"))
                    .isInstanceOf(PaymentProcessingException.class)
                    .hasMessage(Mensajes.NO_REINTENTO);
        }

        @Test
        void rechazaPreapprovalSinInitPoint() {
            when(ordenPagoRepository.buscarPorId(10L)).thenReturn(Optional.of(rechazada()));
            when(suscripcionRepository.buscarPorId(100L)).thenReturn(Optional.of(pendienteConPreapproval()));
            when(ordenPagoRepository.guardar(any(OrdenPago.class)))
                    .thenAnswer(inv -> TestFixtures.conIdOrden(inv.getArgument(0), 11L));
            when(mercadoPago.crearPreapproval(any()))
                    .thenReturn(new MercadoPagoPort.PreapprovalCreado("PRE-NEW", null, "pending"));

            assertThatThrownBy(() -> useCase.reintentar(10L, "a@b.com", "USER-1"))
                    .isInstanceOf(PaymentProcessingException.class)
                    .hasMessage(Mensajes.NO_REINTENTO);
        }
    }

    @Nested
    @DisplayName("Reembolsar pago (SR-RF-98)")
    class Reembolsar {

        private OrdenPago aprobada() {
            return TestFixtures.conIdOrden(TestFixtures.aprobada(100L, "100.00"), 10L);
        }

        private Suscripcion activa() {
            return TestFixtures.activaUsuario("USER-1",
                    TestFixtures.HOY.minusDays(10), TestFixtures.HOY.plusDays(20));
        }

        @Test
        void reembolsoTotalCancelaRenovacion() {
            OrdenPago orden = aprobada();
            Suscripcion suscripcion = activa();
            when(ordenPagoRepository.buscarPorId(10L)).thenReturn(Optional.of(orden));
            when(ordenPagoRepository.guardar(any(OrdenPago.class))).thenAnswer(inv -> inv.getArgument(0));
            when(suscripcionRepository.buscarPorId(100L)).thenReturn(Optional.of(suscripcion));
            when(suscripcionRepository.guardar(any(Suscripcion.class))).thenAnswer(inv -> inv.getArgument(0));
            when(mercadoPago.reembolsar("MP-1", new BigDecimal("100.00")))
                    .thenReturn(new MercadoPagoPort.ReembolsoMp("REF-1", new BigDecimal("100.00")));

            ReembolsoResponse response = useCase.reembolsar(10L, new BigDecimal("100.00"),
                    "Devolución total", "ADMIN");

            assertThat(response.estado()).isEqualTo(EstadoPago.REEMBOLSADO);
            assertThat(response.reembolsoParcial()).isFalse();
            assertThat(response.saldoReembolsable().valor()).isEqualByComparingTo("0.00");
            assertThat(response.renovacionCancelada()).isTrue();
            assertThat(suscripcion.isAutoRenovacion()).isFalse();
            verify(mercadoPago).cancelarPreapproval("PRE-100");
        }

        @Test
        void reembolsoParcialConservaAprobado() {
            OrdenPago orden = aprobada();
            Suscripcion suscripcion = activa();
            when(ordenPagoRepository.buscarPorId(10L)).thenReturn(Optional.of(orden));
            when(ordenPagoRepository.guardar(any(OrdenPago.class))).thenAnswer(inv -> inv.getArgument(0));
            when(suscripcionRepository.buscarPorId(100L)).thenReturn(Optional.of(suscripcion));
            when(suscripcionRepository.guardar(any(Suscripcion.class))).thenAnswer(inv -> inv.getArgument(0));
            when(mercadoPago.reembolsar("MP-1", new BigDecimal("40.00")))
                    .thenReturn(new MercadoPagoPort.ReembolsoMp("REF-1", new BigDecimal("40.00")));

            ReembolsoResponse response = useCase.reembolsar(10L, new BigDecimal("40.00"),
                    "Parte", "ADMIN");

            assertThat(response.estado()).isEqualTo(EstadoPago.APROBADO);
            assertThat(response.reembolsoParcial()).isTrue();
            assertThat(response.montoReembolsado().valor()).isEqualByComparingTo("40.00");
            assertThat(response.saldoReembolsable().valor()).isEqualByComparingTo("60.00");
        }

        @Test
        void exigeAdministrador() {
            assertThatThrownBy(() -> useCase.reembolsar(10L, BigDecimal.ONE, "Motivo", "USUARIO"))
                    .isInstanceOf(OperacionNoPermitidaException.class)
                    .hasMessage(Mensajes.SOLO_ADMIN);
            verify(ordenPagoRepository, never()).buscarPorId(any());
        }

        @Test
        void exigeOrdenConPagoExterno() {
            OrdenPago sinPago = TestFixtures.conIdOrden(TestFixtures.pendiente(100L, "100"), 10L);
            when(ordenPagoRepository.buscarPorId(10L)).thenReturn(Optional.of(sinPago));

            assertThatThrownBy(() -> useCase.reembolsar(10L, BigDecimal.ONE, "Motivo", "ADMIN"))
                    .isInstanceOf(PaymentProcessingException.class)
                    .hasMessage(Mensajes.NO_REEMBOLSO);
        }

        @Test
        void fallaSiNoExisteOrdenOSuscripcion() {
            when(ordenPagoRepository.buscarPorId(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> useCase.reembolsar(99L, BigDecimal.ONE, "Motivo", "ADMIN"))
                    .isInstanceOf(OrdenPagoNoEncontradaException.class);

            OrdenPago orden = aprobada();
            when(ordenPagoRepository.buscarPorId(10L)).thenReturn(Optional.of(orden));
            when(ordenPagoRepository.guardar(any(OrdenPago.class))).thenAnswer(inv -> inv.getArgument(0));
            when(suscripcionRepository.buscarPorId(100L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> useCase.reembolsar(10L, BigDecimal.ONE, "Motivo", "ADMIN"))
                    .isInstanceOf(SuscripcionNoEncontradaException.class);
        }

        @Test
        void envuelveFalloDeMercadoPagoSinCambiarEstado() {
            OrdenPago orden = aprobada();
            when(ordenPagoRepository.buscarPorId(10L)).thenReturn(Optional.of(orden));
            when(mercadoPago.reembolsar(anyString(), any())).thenThrow(new IllegalStateException("MP"));

            assertThatThrownBy(() -> useCase.reembolsar(10L, BigDecimal.ONE, "Motivo", "ADMIN"))
                    .isInstanceOf(PaymentProcessingException.class)
                    .hasMessage(Mensajes.NO_REEMBOLSO);
        }

        @Test
        void mantieneLaRenovacionSiMercadoPagoNoPermiteCancelarla() {
            OrdenPago orden = aprobada();
            Suscripcion suscripcion = activa();
            when(ordenPagoRepository.buscarPorId(10L)).thenReturn(Optional.of(orden));
            when(ordenPagoRepository.guardar(any(OrdenPago.class))).thenAnswer(inv -> inv.getArgument(0));
            when(suscripcionRepository.buscarPorId(100L)).thenReturn(Optional.of(suscripcion));
            when(suscripcionRepository.guardar(any(Suscripcion.class))).thenAnswer(inv -> inv.getArgument(0));
            org.mockito.Mockito.doThrow(new IllegalStateException("MP"))
                    .when(mercadoPago).cancelarPreapproval("PRE-100");

            ReembolsoResponse response = useCase.reembolsar(10L, new BigDecimal("100.00"),
                    "Total", "ADMIN");

            assertThat(response.renovacionCancelada()).isFalse();
            assertThat(suscripcion.isAutoRenovacion()).isFalse();
        }

        @Test
        void validaMontoYmotivoAntesDeLlamarAMercadoPago() {
            OrdenPago orden = aprobada();
            when(ordenPagoRepository.buscarPorId(10L)).thenReturn(Optional.of(orden));

            assertThatThrownBy(() -> useCase.reembolsar(10L, new BigDecimal("101.00"), "Motivo", "ADMIN"))
                    .isInstanceOf(SolicitudInvalidaException.class)
                    .hasMessage(Mensajes.MONTO_SUPERA_SALDO);
            assertThatThrownBy(() -> useCase.reembolsar(10L, BigDecimal.ONE, " ", "ADMIN"))
                    .isInstanceOf(SolicitudInvalidaException.class)
                    .hasMessage(Mensajes.MOTIVO_REEMBOLSO);
            verify(mercadoPago, never()).reembolsar(anyString(), any());
        }
    }
}
