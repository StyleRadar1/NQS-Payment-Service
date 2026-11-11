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

    public void activar(LocalDate nuevaFechaFin) {
        this.estado = EstadoSuscripcion.ACTIVA;
        this.fechaInicio = LocalDate.now();
        this.fechaFin = nuevaFechaFin;
        // Si había un cambio de plan programado, se aplica al renovar
        aplicarCambioPlan();
    }


    public void suspenderPorFaltaDePago() {
        this.estado = EstadoSuscripcion.SUSPENDIDA;
        this.autoRenovacion = false;
    }

    public void cancelarRenovacion() {
        this.autoRenovacion = false;
    }

    public void programarCambioPlan(Long nuevoPlanId) {
        this.planProgramadoId = nuevoPlanId;
    }

    public void aplicarCambioPlan() {
        if (planProgramadoId != null) {
            this.planId = this.planProgramadoId;
            this.planProgramadoId = null;
        }
    }

    public void revertirAPlanGratuito(Long planGratuitoId) {
        this.planId = planGratuitoId;
        this.estado = EstadoSuscripcion.SUSPENDIDA;
        this.autoRenovacion = false;
    }
    
    public boolean estaVencida() {
        return this.fechaFin != null && LocalDate.now().isAfter(this.fechaFin);
    }

    // Getters y Setters
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