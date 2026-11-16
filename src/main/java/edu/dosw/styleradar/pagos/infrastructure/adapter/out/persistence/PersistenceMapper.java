package edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence;

import edu.dosw.styleradar.pagos.domain.model.Monto;
import edu.dosw.styleradar.pagos.domain.model.OrdenPago;
import edu.dosw.styleradar.pagos.domain.model.Plan;
import edu.dosw.styleradar.pagos.domain.model.Suscripcion;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence.entity.OrdenPagoEntity;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence.entity.PlanEntity;
import edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence.entity.SuscripcionEntity;

import java.math.BigDecimal;

/** Traducción entre el dominio puro y las entidades JPA. */
public final class PersistenceMapper {

    private PersistenceMapper() {
    }

    public static Plan toDomain(PlanEntity entity) {
        return new Plan(
                entity.getId(),
                entity.getNombre(),
                entity.getTipoSuscriptor(),
                new Monto(entity.getPrecio(), entity.getMoneda()),
                entity.getLimites(),
                entity.isActivo());
    }

    public static Suscripcion toDomain(SuscripcionEntity entity) {
        return Suscripcion.rehidratar(
                entity.getId(),
                entity.getSuscriptorId(),
                entity.getTipoSuscriptor(),
                entity.getPlanId(),
                entity.getPlanProgramadoId(),
                entity.getEstado(),
                entity.getFechaInicio(),
                entity.getFechaFin(),
                entity.isAutoRenovacion(),
                entity.getPreapprovalIdMp(),
                entity.getNit(),
                entity.getAlmacenId(),
                entity.getFechaActualizacion());
    }

    public static SuscripcionEntity toEntity(Suscripcion suscripcion) {
        return SuscripcionEntity.builder()
                .id(suscripcion.getId())
                .planId(suscripcion.getPlanId())
                .planProgramadoId(suscripcion.getPlanProgramadoId())
                .suscriptorId(suscripcion.getSuscriptorId())
                .tipoSuscriptor(suscripcion.getTipoSuscriptor())
                .estado(suscripcion.getEstado())
                .fechaInicio(suscripcion.getFechaInicio())
                .fechaFin(suscripcion.getFechaFin())
                .autoRenovacion(suscripcion.isAutoRenovacion())
                .preapprovalIdMp(suscripcion.getPreapprovalIdMp())
                .nit(suscripcion.getNit())
                .almacenId(suscripcion.getAlmacenId())
                .fechaActualizacion(suscripcion.getFechaActualizacion())
                .build();
    }

    public static OrdenPago toDomain(OrdenPagoEntity entity) {
        BigDecimal reembolsado = entity.getMontoReembolsado() == null
                ? BigDecimal.ZERO
                : entity.getMontoReembolsado();
        return OrdenPago.rehidratar(
                entity.getId(),
                entity.getSuscripcionId(),
                new Monto(entity.getMonto(), entity.getMoneda()),
                entity.getEstado(),
                entity.getIdPagoExterno(),
                new Monto(reembolsado, entity.getMoneda()),
                entity.getMotivoReembolso(),
                entity.getFechaCreacion());
    }

    public static OrdenPagoEntity toEntity(OrdenPago orden) {
        return OrdenPagoEntity.builder()
                .id(orden.getId())
                .suscripcionId(orden.getSuscripcionId())
                .monto(orden.getMonto().valor())
                .moneda(orden.getMonto().moneda())
                .estado(orden.getEstado())
                .idPagoExterno(orden.getIdPagoExterno())
                .montoReembolsado(orden.getMontoReembolsado().valor())
                .motivoReembolso(orden.getMotivoReembolso())
                .fechaCreacion(orden.getFechaCreacion())
                .build();
    }
}
