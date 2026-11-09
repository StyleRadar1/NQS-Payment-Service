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

    public void actualizarEstado(String nuevoEstado) {
        this.estado = EstadoPago.valueOf(nuevoEstado.toUpperCase());
    }

    public void aprobar() {
        if (this.estado != EstadoPago.PENDIENTE) {
            throw new IllegalStateException("Solo se pueden aprobar pagos pendientes");
        }
        this.estado = EstadoPago.APROBADO;
    }

    public void rechazar() {
        this.estado = EstadoPago.RECHAZADO;
    }

    public void reembolsar(Monto montoReembolso, String motivo) {
        if (this.estado != EstadoPago.APROBADO) {
            throw new IllegalStateException("Solo se reembolsan pagos aprobados");
        }
        if (montoReembolso.valor().compareTo(this.monto.valor()) > 0) {
            throw new IllegalArgumentException("El monto a reembolsar supera el monto original");
        }
        this.montoReembolsado = montoReembolso;
        this.motivoReembolso = motivo;
        this.estado = EstadoPago.REEMBOLSADO;
    }

    public Long getId() { return id; }
    public Long getSuscripcionId() { return suscripcionId; }
    public Monto getMonto() { return monto; }
    public EstadoPago getEstado() { return estado; }
    public String getIdPagoExterno() { return idPagoExterno; }
    public Monto getMontoReembolsado() { return montoReembolsado; }
    public String getMotivoReembolso() { return motivoReembolso; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
}