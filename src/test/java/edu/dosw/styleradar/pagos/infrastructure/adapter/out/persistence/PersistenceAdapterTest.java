package edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.pagos.domain.model.EstadoPago;
import edu.dosw.styleradar.pagos.domain.model.EstadoSuscripcion;
import edu.dosw.styleradar.pagos.domain.model.Monto;
import edu.dosw.styleradar.pagos.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos.domain.model.Plan;
import edu.dosw.styleradar.pagos.domain.model.Suscripcion;
import edu.dosw.styleradar.pagos.domain.model.TipoSuscriptor;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence.entity.OrdenPagoEntity;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence.entity.PlanEntity;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence.entity.SuscripcionEntity;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence.repository.OrdenPagoJpaRepository;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence.repository.PlanJpaRepository;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence.repository.SuscripcionJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import({
        PlanPersistenceAdapter.class,
        SuscripcionPersistenceAdapter.class,
        OrdenPagoPersistenceAdapter.class,
        WebhookLogPersistenceAdapter.class
})
@DisplayName("Adaptadores JPA (SR-RF-67/75/97/98, SR-RNF-14)")
class PersistenceAdapterTest {

    @Autowired
    private PlanPersistenceAdapter planAdapter;
    @Autowired
    private SuscripcionPersistenceAdapter suscripcionAdapter;
    @Autowired
    private OrdenPagoPersistenceAdapter ordenAdapter;
    @Autowired
    private WebhookLogPersistenceAdapter webhookLogAdapter;
    @Autowired
    private PlanJpaRepository planRepository;
    @Autowired
    private SuscripcionJpaRepository suscripcionRepository;
    @Autowired
    private OrdenPagoJpaRepository ordenRepository;

    private Long planUsuarioId;
    private Long planAlmacenId;

    @BeforeEach
    void seedPlanes() {
        planUsuarioId = planRepository.save(PlanEntity.builder()
                .nombre("Radar+")
                .tipoSuscriptor(TipoSuscriptor.USUARIO)
                .precio(new BigDecimal("14900.00"))
                .moneda("COP")
                .limites(Map.of("max_wishlist", -1))
                .activo(true)
                .build()).getId();
        planAlmacenId = planRepository.save(PlanEntity.builder()
                .nombre("Almacén Básico")
                .tipoSuscriptor(TipoSuscriptor.ALMACEN)
                .precio(new BigDecimal("29900.00"))
                .moneda("COP")
                .limites(Map.of("max_productos", 50))
                .activo(true)
                .build()).getId();
        planRepository.save(PlanEntity.builder()
                .nombre("Radar")
                .tipoSuscriptor(TipoSuscriptor.USUARIO)
                .precio(BigDecimal.ZERO)
                .moneda("COP")
                .limites(Map.of())
                .activo(true)
                .build());
    }

    @Test
    void planAdapterListaYBuscaPlanes() {
        assertThat(planAdapter.listarActivos()).hasSize(3);
        assertThat(planAdapter.buscarPorId(planUsuarioId)).isPresent();
        assertThat(planAdapter.buscarPlanDePagoPorTipo(TipoSuscriptor.USUARIO))
                .get()
                .extracting(Plan::getNombre)
                .isEqualTo("Radar+");
        assertThat(planAdapter.buscarPlanDePagoPorTipo(TipoSuscriptor.ALMACEN))
                .get()
                .extracting(Plan::getNombre)
                .isEqualTo("Almacén Básico");
    }

    @Test
    void suscripcionAdapterGuardaYConsulta() {
        Suscripcion pendiente = Suscripcion.pendiente("USER-1", TipoSuscriptor.USUARIO,
                planUsuarioId, LocalDateTime.now());

        Suscripcion guardada = suscripcionAdapter.guardar(pendiente);

        assertThat(guardada.getId()).isNotNull();
        assertThat(suscripcionAdapter.buscarPorId(guardada.getId())).isPresent();
        assertThat(suscripcionAdapter.buscarUltimaPorSuscriptor("USER-1")).isPresent();
        assertThat(suscripcionAdapter.buscarActivaOPendiente("USER-1")).isPresent();
        assertThat(suscripcionAdapter.buscarActivaOPendiente("OTRO")).isEmpty();
    }

    @Test
    void suscripcionAdapterConsultaActivasVencidasYPreapproval() {
        Suscripcion activa = Suscripcion.rehidratar(null, "ALMACEN-1", TipoSuscriptor.ALMACEN,
                planAlmacenId, null, EstadoSuscripcion.ACTIVA,
                LocalDate.now().minusMonths(1), LocalDate.now().minusDays(1), true,
                "PRE-1", "900123", LocalDateTime.now());
        Suscripcion guardada = suscripcionAdapter.guardar(activa);

        assertThat(suscripcionAdapter.buscarPorPreapprovalId("PRE-1")).isPresent();
        assertThat(suscripcionAdapter.buscarActivasVencidas(LocalDate.now()))
                .extracting(Suscripcion::getId)
                .contains(guardada.getId());
        assertThat(suscripcionAdapter.buscarActivasVencidas(LocalDate.now().minusMonths(2))).isEmpty();
    }

    @Test
    void ordenAdapterGuardaYConsultaPorSuscriptor() {
        Suscripcion suscripcion = suscripcionAdapter.guardar(Suscripcion.pendiente(
                "USER-1", TipoSuscriptor.USUARIO, planUsuarioId, LocalDateTime.now()));
        OrdenPago pendiente = OrdenPago.pendiente(suscripcion.getId(),
                Monto.cop(new BigDecimal("14900.00")), LocalDateTime.now().minusDays(1));

        OrdenPago guardada = ordenAdapter.guardar(pendiente);

        assertThat(guardada.getId()).isNotNull();
        assertThat(ordenAdapter.buscarPorId(guardada.getId())).isPresent();
        assertThat(ordenAdapter.buscarPorSuscripcion(suscripcion.getId())).hasSize(1);
        assertThat(ordenAdapter.buscarPorSuscriptor("USER-1")).hasSize(1);
        assertThat(ordenAdapter.buscarPendientePorSuscripcion(suscripcion.getId())).isPresent();
        assertThat(ordenAdapter.buscarPendientesAntesDe(LocalDateTime.now())).hasSize(1);
        assertThat(ordenAdapter.listar(null)).hasSize(1);
        assertThat(ordenAdapter.listar(EstadoPago.PENDIENTE)).hasSize(1);
        assertThat(ordenAdapter.listar(EstadoPago.APROBADO)).isEmpty();
    }

    @Test
    void ordenAdapterBuscaPorPagoExternoYActualizaEstado() {
        Suscripcion suscripcion = suscripcionAdapter.guardar(Suscripcion.pendiente(
                "USER-2", TipoSuscriptor.USUARIO, planUsuarioId, LocalDateTime.now()));
        OrdenPago pendiente = OrdenPago.pendiente(suscripcion.getId(),
                Monto.cop(new BigDecimal("14900.00")), LocalDateTime.now());
        pendiente.actualizarEstado(EstadoPago.APROBADO, "MP-99");

        OrdenPago guardada = ordenAdapter.guardar(pendiente);

        assertThat(ordenAdapter.buscarPorIdPagoExterno("MP-99")).isPresent();
        assertThat(guardada.getEstado()).isEqualTo(EstadoPago.APROBADO);
    }

    @Test
    void webhookLogAdapterRegistraYConsulta() {
        assertThat(webhookLogAdapter.existe("MP-1")).isFalse();

        webhookLogAdapter.registrar("MP-1", Map.of("type", "payment"), "PROCESADO");

        assertThat(webhookLogAdapter.existe("MP-1")).isTrue();
    }

    @Test
    void ordenEntitySeMapeaConSuscripcion() {
        Suscripcion suscripcion = suscripcionAdapter.guardar(Suscripcion.pendiente(
                "USER-3", TipoSuscriptor.USUARIO, planUsuarioId, LocalDateTime.now()));
        OrdenPagoEntity entity = ordenRepository.save(OrdenPagoEntity.builder()
                .suscripcionId(suscripcion.getId())
                .monto(new BigDecimal("14900.00"))
                .moneda("COP")
                .estado(EstadoPago.PENDIENTE)
                .fechaCreacion(LocalDateTime.now())
                .build());

        assertThat(entity.getId()).isNotNull();
        assertThat(suscripcionRepository.findById(suscripcion.getId())).isPresent();
    }
}
