package edu.dosw.styleradar.pagos_suscripciones.domain.model;

import java.time.LocalDate;
import java.util.UUID;

public class Suscripcion {
    private final Long id;
    private final String suscriptorId; // ID genérico (Long o NIT)
    private final TipoSuscriptor tipoSuscriptor;
    private Long planId;
    private Long planProgramadoId; // Para el cambio de plan (SR-RF-101)
    private EstadoSuscripcion estado;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private boolean autoRenovacion;
    private int diasPeriodoGracia;

    public Suscripcion(Long id, String suscriptorId, TipoSuscriptor tipoSuscriptor, Long planId, int diasPeriodoGracia) {
        this.id = id;
        this.suscriptorId = suscriptorId;
        this.tipoSuscriptor = tipoSuscriptor;
        this.planId = planId;
        this.estado = EstadoSuscripcion.EN_PERIODO_GRACIA;
        this.fechaInicio = LocalDate.now();
        this.fechaFin = LocalDate.now().plusMonths(1);
        this.autoRenovacion = true;
        this.diasPeriodoGracia = diasPeriodoGracia;
    }

    public void activar() {
        this.estado = EstadoSuscripcion.ACTIVA;
        // Aquí puedes ajustar fechas si es una renovación
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
    // Otros métodos: suspender(), cancelarRenovacion()...
}
