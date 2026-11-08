package edu.dosw.styleradar.pagos_suscripciones.domain.model;

import java.time.LocalDate;
import java.util.UUID;

public class Suscripcion {
    private final Long id;
    private final String suscriptorId; // ID genérico del usuario o NIT del almacén
    private final TipoSuscriptor tipoSuscriptor;
    private Long planId;
    private Long planProgramadoId; // Para el cambio de plan (SR-RF-101)
    private EstadoSuscripcion estado;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private boolean autoRenovacion;

    public Suscripcion(Long id, String suscriptorId, TipoSuscriptor tipoSuscriptor, Long planId) {
        this.id = id;
        this.suscriptorId = suscriptorId;
        this.tipoSuscriptor = tipoSuscriptor;
        this.planId = planId;
        this.estado = EstadoSuscripcion.ACTIVA; // Asumimos que nace activa tras un pago o se crea pendiente
        this.fechaInicio = LocalDate.now();
        this.fechaFin = LocalDate.now().plusMonths(1); // 1 mes por defecto
        this.autoRenovacion = true;
    }

    // Lógica de negocio pura
    public void activar(LocalDate nuevaFechaFin) {
        this.estado = EstadoSuscripcion.ACTIVA;
        this.fechaFin = nuevaFechaFin;
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

    public boolean estaVencida() {
        return LocalDate.now().isAfter(this.fechaFin);
    }

    // Getters
    public Long getId() { return id; }
    public String getSuscriptorId() { return suscriptorId; }
    public TipoSuscriptor getTipoSuscriptor() { return tipoSuscriptor; }
    public Long getPlanId() { return planId; }
    public Long getPlanProgramadoId() { return planProgramadoId; }
    public EstadoSuscripcion getEstado() { return estado; }
    public LocalDate getFechaInicio() { return fechaInicio; }
    public LocalDate getFechaFin() { return fechaFin; }
    public boolean isAutoRenovacion() { return autoRenovacion; }
}