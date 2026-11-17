package edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.pagos.domain.model.EstadoPago;
import edu.dosw.styleradar.pagos.domain.model.Monto;
import edu.dosw.styleradar.pagos.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos.domain.model.Plan;
import edu.dosw.styleradar.pagos.domain.model.Suscripcion;
import edu.dosw.styleradar.pagos.domain.model.TipoSuscriptor;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence.entity.OrdenPagoEntity;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence.entity.PlanEntity;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence.entity.SuscripcionEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PersistenceMapper")
class PersistenceMapperTest {

    @Test
    void mapeaPlan() {
        PlanEntity entity = PlanEntity.builder()
                .id(2L)
                .nombre("Radar+")
                .tipoSuscriptor(TipoSuscriptor.USUARIO)
                .precio(new BigDecimal("14900.00"))
                .moneda("COP")
                .limites(Map.of("max_wishlist", -1))
                .activo(true)
                .build();

        Plan plan = PersistenceMapper.toDomain(entity);

        assertThat(plan.getId()).isEqualTo(2L);
        assertThat(plan.getNombre()).isEqualTo("Radar+");
        assertThat(plan.getTipoSuscriptor()).isEqualTo(TipoSuscriptor.USUARIO);
        assertThat(plan.getPrecio()).isEqualTo(new Monto(new BigDecimal("14900.00"), "COP"));
        assertThat(plan.getLimites()).containsEntry("max_wishlist", -1);
        assertThat(plan.isActivo()).isTrue();
    }

    @Test
    void mapeaSuscripcionEnAmbasDirecciones() {
        LocalDateTime ahora = LocalDateTime.of(2026, 11, 14, 10, 0);
        SuscripcionEntity entity = SuscripcionEntity.builder()
                .id(10L)
                .planId(3L)
                .planProgramadoId(2L)
                .suscriptorId("ALMACEN-1")
                .tipoSuscriptor(TipoSuscriptor.ALMACEN)
                .estado(edu.dosw.styleradar.pagos.domain.model.EstadoSuscripcion.ACTIVA)
                .fechaInicio(LocalDate.of(2026, 11, 1))
                .fechaFin(LocalDate.of(2026, 12, 1))
                .autoRenovacion(true)
                .preapprovalIdMp("PRE-1")
                .nit("900123456-7")
                .fechaActualizacion(ahora)
                .build();

        Suscripcion dominio = PersistenceMapper.toDomain(entity);
        SuscripcionEntity vuelta = PersistenceMapper.toEntity(dominio);

        assertThat(dominio.getId()).isEqualTo(10L);
        assertThat(dominio.getNit()).isEqualTo("900123456-7");
        assertThat(dominio.getPreapprovalIdMp()).isEqualTo("PRE-1");
        assertThat(vuelta.getSuscriptorId()).isEqualTo("ALMACEN-1");
        assertThat(vuelta.getPlanId()).isEqualTo(3L);
        assertThat(vuelta.getPlanProgramadoId()).isEqualTo(2L);
        assertThat(vuelta.getNit()).isEqualTo("900123456-7");
        assertThat(vuelta.getFechaInicio()).isEqualTo(LocalDate.of(2026, 11, 1));
    }

    @Test
    void mapeaOrdenConReembolsoNuloYCompleto() {
        LocalDateTime ahora = LocalDateTime.of(2026, 11, 14, 10, 0);
        OrdenPagoEntity entity = OrdenPagoEntity.builder()
                .id(1L)
                .suscripcionId(10L)
                .monto(new BigDecimal("100.00"))
                .moneda("COP")
                .estado(EstadoPago.APROBADO)
                .idPagoExterno("MP-1")
                .montoReembolsado(null)
                .fechaCreacion(ahora)
                .build();

        OrdenPago dominio = PersistenceMapper.toDomain(entity);
        OrdenPagoEntity vuelta = PersistenceMapper.toEntity(dominio);

        assertThat(dominio.getMontoReembolsado().esCero()).isTrue();
        assertThat(dominio.getEstado()).isEqualTo(EstadoPago.APROBADO);
        assertThat(vuelta.getSuscripcionId()).isEqualTo(10L);
        assertThat(vuelta.getMonto()).isEqualByComparingTo("100.00");
        assertThat(vuelta.getMoneda()).isEqualTo("COP");
        assertThat(vuelta.getIdPagoExterno()).isEqualTo("MP-1");
    }
}
