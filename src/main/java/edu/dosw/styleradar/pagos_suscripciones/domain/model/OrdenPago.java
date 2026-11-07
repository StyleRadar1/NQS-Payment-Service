package edu.dosw.styleradar.pagos_suscripciones.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrdenPago {
    private final Long id;
    private final Long suscripcionId;
    private final Monto monto;
    private EstadoPago estado;
    private String idPagoExterno;
    private Monto montoReembolsado;
    private String motivoReembolso;
    private final LocalDateTime fechaCreacion;

    public OrdenPago(Long id, Long suscripcionId, Monto monto, String idPagoExterno) {
        this.id = id;
        this.suscripcionId = suscripcionId;
        this.monto = monto;
        this.idPagoExterno = idPagoExterno;
        this.estado = EstadoPago.PENDIENTE;
        this.montoReembolsado = new Monto(BigDecimal.ZERO, monto.moneda());
        this.fechaCreacion = LocalDateTime.now();
    }

    public void aprobar() { this.estado = EstadoPago.APROBADO; }
    public void rechazar() { this.estado = EstadoPago.RECHAZADO; }
    
    public void reembolsar(Monto montoReembolso, String motivo) {
        if (this.estado != EstadoPago.APROBADO) throw new IllegalStateException("Solo se reembolsan pagos aprobados");
        this.montoReembolsado = montoReembolso;
        this.motivoReembolso = motivo;
        this.estado = EstadoPago.REEMBOLSADO;
    }
}