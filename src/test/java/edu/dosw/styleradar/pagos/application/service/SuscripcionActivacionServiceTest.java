package edu.dosw.styleradar.pagos.application.service;

import edu.dosw.styleradar.pagos.TestFixtures;
import edu.dosw.styleradar.pagos.application.port.out.SuscripcionRepositoryPort;
import edu.dosw.styleradar.pagos.domain.model.EstadoPago;
import edu.dosw.styleradar.pagos.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos.domain.model.Suscripcion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SuscripcionActivacionService (SR-RF-94/95)")
class SuscripcionActivacionServiceTest {

    @Mock
    private SuscripcionRepositoryPort repositorio;

    private SuscripcionActivacionService servicio;

    @BeforeEach
    void setUp() {
        servicio = new SuscripcionActivacionService(repositorio);
    }

    @Test
    void activaYGuardaConPagoAprobado() {
        Suscripcion suscripcion = TestFixtures.pendienteUsuario("USER-1");
        OrdenPago orden = TestFixtures.aprobada(100L, "14900");
        OrdenPago ordenConSuscripcion = TestFixtures.conIdOrden(orden, 10L);
        // La suscripción de la orden debe coincidir con el id de la suscripción.
        Suscripcion conId = TestFixtures.conIdSuscripcion(suscripcion, 100L);
        OrdenPago ordenValida = OrdenPago.rehidratar(10L, 100L, ordenConSuscripcion.getMonto(),
                EstadoPago.APROBADO, "MP-1", ordenConSuscripcion.getMontoReembolsado(), null,
                TestFixtures.AHORA);
        when(repositorio.guardar(any(Suscripcion.class))).thenAnswer(inv -> inv.getArgument(0));

        Suscripcion activada = servicio.activar(conId, ordenValida, TestFixtures.HOY);

        assertThat(activada.getEstado().name()).isEqualTo("ACTIVA");
        assertThat(activada.getFechaFin()).isEqualTo(TestFixtures.HOY.plusMonths(1));
        verify(repositorio).guardar(conId);
    }

    @Test
    void exigePagoAprobado() {
        Suscripcion suscripcion = TestFixtures.pendienteUsuario("USER-1");
        OrdenPago pendiente = TestFixtures.pendiente(100L, "14900");

        assertThatThrownBy(() -> servicio.activar(suscripcion, pendiente, LocalDate.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("aprobado");
        verify(repositorio, never()).guardar(any());
    }

    @Test
    void exigeQueLaOrdenPertenezcaALaSuscripcion() {
        Suscripcion suscripcion = TestFixtures.conIdSuscripcion(
                TestFixtures.pendienteUsuario("USER-1"), 100L);
        OrdenPago orden = TestFixtures.aprobada(999L, "14900");

        assertThatThrownBy(() -> servicio.activar(suscripcion, orden, LocalDate.now()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no pertenece");
    }
}
