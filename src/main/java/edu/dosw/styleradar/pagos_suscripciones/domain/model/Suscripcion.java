package edu.dosw.styleradar.pagos_suscripciones.domain.model;

import java.time.LocalDate;

public class Suscripcion {
    private final Long id;
    private final String suscriptorId;
    private final TipoSuscriptor tipoSuscriptor;
    private Long planId;
    private Long planProgramadoId;
    private EstadoSuscripcion estado;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private boolean autoRenovacion;
    private String preapprovalIdMp;

    public Suscripcion(Long id, String suscriptorId, TipoSuscriptor tipoSuscriptor, Long planId) {
        this.id = id;
        this.suscriptorId = suscriptorId;
        this.tipoSuscriptor = tipoSuscriptor;
        this.planId = planId;
        this.estado = EstadoSuscripcion.PENDIENTE;
        this.autoRenovacion = true;
    }

    /** Activación inicial. No reinicia fechaInicio si ya existía. */
    public void activar(LocalDate nuevaFechaFin) {
        this.estado = EstadoSuscripcion.ACTIVA;
        if (this.fechaInicio == null) {
            this.fechaInicio = LocalDate.now();
        }
        this.fechaFin = nuevaFechaFin;
        aplicarCambioPlan();
    }

    /** SR-RF-95: renueva extendiendo desde fechaFin si sigue vigente. */
    public void renovar() {
        LocalDate hoy = LocalDate.now();
        LocalDate base = (this.fechaFin != null && !hoy.isAfter(this.fechaFin))
                ? this.fechaFin : hoy;
        this.estado = EstadoSuscripcion.ACTIVA;
        if (this.fechaInicio == null) {
            this.fechaInicio = hoy;
        }
        this.fechaFin = base.plusMonths(1);
        aplicarCambioPlan();
    }

    /** Fix B1: rehidratación pura (sin reglas de negocio). */
    public void rehidratar(EstadoSuscripcion estado,
                           LocalDate fechaInicio,
                           LocalDate fechaFin,
                           boolean autoRenovacion,
                           String preapprovalIdMp,
                           Long planProgramadoId) {
        this.estado = estado;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.autoRenovacion = autoRenovacion;
        this.preapprovalIdMp = preapprovalIdMp;
        this.planProgramadoId = planProgramadoId;
    }

    public void suspenderPorFaltaDePago() {
        this.estado = EstadoSuscripcion.SUSPENDIDA;
        this.autoRenovacion = false;
    }

    /** SR-RF-99/103: trazabilidad antes de suspender/degradar. */
    public void marcarVencida() {
        this.estado = EstadoSuscripcion.VENCIDA;
    }

    /** FA-02 (SR-RF-68): idempotente. */
    public void cancelarRenovacion() {
        if (!this.autoRenovacion) {
            return;
        }
        this.autoRenovacion = false;
    }

    /**
     * SR-RF-101 (v29/10/2026) ELIMINÓ el "cambio de plan".
     * El requerimiento ahora solo exige "cancelar plan de usuario".
     *
     * @deprecated Fuera de alcance vigente. Se mantiene para reactivación futura (Sprint 3).
     *             Ver README §Desviaciones.
     */
    @Deprecated(since = "2026-11-11")
    public void programarCambioPlan(Long nuevoPlanId) {
        this.planProgramadoId = nuevoPlanId;
    }

    /**
     * Aplica el plan programado en el momento de activación/renovación.
     * Se mantiene por compatibilidad con {@link #programarCambioPlan(Long)}.
     */
    public void aplicarCambioPlan() {
        if (planProgramadoId != null) {
            this.planId = this.planProgramadoId;
            this.planProgramadoId = null;
        }
    }

    /** SR-RF-103. */
    public void revertirAPlanGratuito(Long planGratuitoId) {
        this.planId = planGratuitoId;
        this.estado = EstadoSuscripcion.SUSPENDIDA;
        this.autoRenovacion = false;
        this.fechaFin = LocalDate.now();
    }

    public boolean estaVencida() {
        return this.fechaFin != null && LocalDate.now().isAfter(this.fechaFin);
    }

    public boolean estaActiva() {
        return this.estado == EstadoSuscripcion.ACTIVA;
    }

    public boolean estaPendiente() {
        return this.estado == EstadoSuscripcion.PENDIENTE;
    }

    // Getters / Setters (mantenidos por compatibilidad con adapters/controladores)
    public Long getId() { return id; }
    public String getSuscriptorId() { return suscriptorId; }
    public TipoSuscriptor getTipoSuscriptor() { return tipoSuscriptor; }
    public Long getPlanId() { return planId; }
    public Long getPlanProgramadoId() { return planProgramadoId; }
    public EstadoSuscripcion getEstado() { return estado; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public boolean isAutoRenovacion() { return autoRenovacion; }
    public String getPreapprovalIdMp() { return preapprovalIdMp; }

    public void setEstado(EstadoSuscripcion estado) { this.estado = estado; }
    public void setPreapprovalIdMp(String preapprovalIdMp) { this.preapprovalIdMp = preapprovalIdMp; }
    public void setPlanId(Long planId) { this.planId = planId; }
}