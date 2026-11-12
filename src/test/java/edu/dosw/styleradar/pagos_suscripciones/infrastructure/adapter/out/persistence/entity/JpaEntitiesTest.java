package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.entity;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.EstadoPago;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.EstadoSuscripcion;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.TipoSuscriptor;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JpaEntitiesTest {

    @Test
    void debeProbarOrdenPagoEntity() {
        OrdenPagoEntity e = new OrdenPagoEntity();
        e.setId(1L); e.setSuscripcionId(1L); e.setMonto(BigDecimal.TEN); e.setMoneda("COP");
        e.setEstado(EstadoPago.PENDIENTE); e.setIdPagoExterno("MP-1"); e.setMontoReembolsado(BigDecimal.ZERO);
        e.setMotivoReembolso("M"); e.setFechaCreacion(LocalDateTime.now());

        assertEquals(1L, e.getId()); assertEquals(1L, e.getSuscripcionId());
        assertEquals("MP-1", e.getIdPagoExterno());
    }

    @Test
    void debeProbarSuscripcionEntity() {
        SuscripcionEntity e = new SuscripcionEntity();
        e.setId(1L); e.setSuscriptorId("USER-1"); e.setTipoSuscriptor(TipoSuscriptor.USUARIO);
        e.setPlanId(1L); e.setPlanProgramadoId(2L); e.setEstado(EstadoSuscripcion.ACTIVA);
        e.setFechaInicio(LocalDate.now()); e.setFechaFin(LocalDate.now()); e.setAutoRenovacion(true);
        e.setPreapprovalIdMp("PRE-1");

        assertEquals("USER-1", e.getSuscriptorId());
        assertEquals("PRE-1", e.getPreapprovalIdMp());
    }

    @Test
    void debeProbarPlanEntity() {
        PlanEntity e = new PlanEntity();
        e.setId(1L); e.setNombre("Radar"); e.setTipoSuscriptor(TipoSuscriptor.USUARIO);
        e.setPrecio(BigDecimal.TEN); e.setMoneda("COP"); e.setLimites(Map.of()); e.setActivo(true);

        assertEquals("Radar", e.getNombre());
        assertEquals(BigDecimal.TEN, e.getPrecio());
    }
}