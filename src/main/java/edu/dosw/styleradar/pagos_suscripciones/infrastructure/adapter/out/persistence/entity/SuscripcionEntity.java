package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.entity;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.EstadoSuscripcion;
import edu.dosw.styleradar.pagos_suscripciones.domain.model.TipoSuscriptor;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "suscripciones")
@Data
@NoArgsConstructor
public class SuscripcionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String suscriptorId;
    @Enumerated(EnumType.STRING)
    private TipoSuscriptor tipoSuscriptor;
    private Long planId;
    private Long planProgramadoId;
    @Enumerated(EnumType.STRING)
    private EstadoSuscripcion estado;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private boolean autoRenovacion;
    private String preapprovalIdMp;
}