package edu.dosw.styleradar.pagos.domain.model;

import edu.dosw.styleradar.pagos.domain.exception.Mensajes;
import edu.dosw.styleradar.pagos.domain.exception.PagoYaProcesadoException;
import edu.dosw.styleradar.pagos.domain.exception.SolicitudInvalidaException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Orden de pago asociada a una suscripción.
 *
 * <p>SR-RF-93/94: nace PENDIENTE y su estado definitivo lo determina Mercado Pago.
 * SR-RF-98: admite reembolsos parciales acumulados; el pago pasa a REEMBOLSADO
 * únicamente cuando la suma cubre el valor original.</p>
 */
public final class OrdenPago {

    private Long id;
    private final Long suscripcionId;
    private final Monto monto;
    private EstadoPago estado;
    private String idPagoExterno;
    private Monto montoReembolsado;
    private String motivoReembolso;
    private final LocalDateTime fechaCreacion;

    private OrdenPago(Long id,
                      Long suscripcionId,
                      Monto monto,
                      EstadoPago estado,
                      String idPagoExterno,
                      Monto montoReembolsado,
                      String motivoReembolso,
                      LocalDateTime fechaCreacion) {
        this.id = id;
        this.suscripcionId = Objects.requireNonNull(suscripcionId, "La suscripción de la orden es obligatoria.");
        this.monto = Objects.requireNonNull(monto, "El monto de la orden es obligatorio.");
        this.estado = Objects.requireNonNull(estado, "El estado de la orden es obligatorio.");
        this.idPagoExterno = idPagoExterno;
        this.montoReembolsado = montoReembolsado == null ? Monto.cero(monto.moneda()) : montoReembolsado;
        this.motivoReembolso = motivoReembolso;
        this.fechaCreacion = fechaCreacion == null ? LocalDateTime.now() : fechaCreacion;
    }

    public static OrdenPago pendiente(Long suscripcionId, Monto monto, LocalDateTime ahora) {
        return new OrdenPago(null, suscripcionId, monto, EstadoPago.PENDIENTE, null, null, null, ahora);
    }

    public static OrdenPago rehidratar(Long id,
                                       Long suscripcionId,
                                       Monto monto,
                                       EstadoPago estado,
                                       String idPagoExterno,
                                       Monto montoReembolsado,
                                       String motivoReembolso,
                                       LocalDateTime fechaCreacion) {
        return new OrdenPago(id, suscripcionId, monto, estado, idPagoExterno,
                montoReembolsado, motivoReembolso, fechaCreacion);
    }

    /**
     * Actualiza el estado con el resultado confirmado por Mercado Pago.
     * SR-RF-94 RN-03: la pasarela es la fuente de verdad.
     *
     * <p>Si la orden ya está REEMBOLSADA se ignoran estados posteriores para
     * no revivir un pago devuelto.</p>
     */
    public void actualizarEstado(EstadoPago nuevoEstado, String idPagoExterno) {
        Objects.requireNonNull(nuevoEstado, "El nuevo estado es obligatorio.");
        if (estado == EstadoPago.REEMBOLSADO && nuevoEstado != EstadoPago.REEMBOLSADO) {
            return;
        }
        if (idPagoExterno != null && !idPagoExterno.isBlank()) {
            this.idPagoExterno = idPagoExterno.trim();
        }
        if (nuevoEstado == EstadoPago.REEMBOLSADO && estado != EstadoPago.REEMBOLSADO) {
            this.montoReembolsado = this.monto;
        }
        this.estado = nuevoEstado;
    }

    /**
     * Registra un reembolso total o parcial.
     *
     * <p>SR-RF-98 RN-02/RN-03/RN-04: solo pagos aprobados, motivo obligatorio,
     * monto mayor que cero y sin superar el saldo pendiente. El estado pasa a
     * REEMBOLSADO solo cuando el acumulado iguala el valor original.</p>
     */
    public void reembolsar(Monto montoReembolso, String motivo) {
        if (estado == EstadoPago.REEMBOLSADO) {
            throw new PagoYaProcesadoException(Mensajes.PAGO_YA_REEMBOLSADO);
        }
        if (estado != EstadoPago.APROBADO) {
            throw new SolicitudInvalidaException(Mensajes.PAGO_NO_APROBADO_REEMBOLSO);
        }
        if (montoReembolso == null || montoReembolso.valor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new SolicitudInvalidaException(Mensajes.MONTO_INVALIDO);
        }
        if (motivo == null || motivo.isBlank()) {
            throw new SolicitudInvalidaException(Mensajes.MOTIVO_REEMBOLSO);
        }
        if (montoReembolso.esMayorQue(saldoReembolsable())) {
            throw new SolicitudInvalidaException(Mensajes.MONTO_SUPERA_SALDO);
        }
        this.montoReembolsado = this.montoReembolsado.sumar(montoReembolso);
        this.motivoReembolso = motivo.trim();
        if (this.montoReembolsado.esIgualA(this.monto)) {
            this.estado = EstadoPago.REEMBOLSADO;
        }
    }

    public Monto saldoReembolsable() {
        return monto.restar(montoReembolsado);
    }

    public boolean fueReembolsadoTotalmente() {
        return estado == EstadoPago.REEMBOLSADO || montoReembolsado.esIgualA(monto);
    }

    public boolean esReembolsoParcial() {
        return montoReembolsado.valor().compareTo(BigDecimal.ZERO) > 0
                && montoReembolsado.esMenorQue(monto);
    }

    public boolean estaPendiente() {
        return estado == EstadoPago.PENDIENTE;
    }

    public boolean estaAprobada() {
        return estado == EstadoPago.APROBADO;
    }

    public boolean estaRechazada() {
        return estado == EstadoPago.RECHAZADO;
    }

    public Long getId() {
        return id;
    }

    public Long getSuscripcionId() {
        return suscripcionId;
    }

    public Monto getMonto() {
        return monto;
    }

    public EstadoPago getEstado() {
        return estado;
    }

    public String getIdPagoExterno() {
        return idPagoExterno;
    }

    public Monto getMontoReembolsado() {
        return montoReembolsado;
    }

    public String getMotivoReembolso() {
        return motivoReembolso;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}
