package edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence.entity;

import edu.dosw.styleradar.pagos.domain.model.EstadoSuscripcion;
import edu.dosw.styleradar.pagos.domain.model.TipoSuscriptor;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Representación JPA de la tabla {@code suscripciones}. */
@Entity
@Table(name = "suscripciones")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuscripcionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "plan_id", nullable = false)
    private Long planId;

    @Column(name = "plan_programado_id")
    private Long planProgramadoId;

    @Column(name = "suscriptor_id", nullable = false, length = 50)
    private String suscriptorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_suscriptor", nullable = false, length = 20)
    private TipoSuscriptor tipoSuscriptor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoSuscripcion estado;

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "auto_renovacion", nullable = false)
    private boolean autoRenovacion;

    @Column(name = "preapproval_id_mp", length = 100)
    private String preapprovalIdMp;

    @Column(name = "nit", length = 30)
    private String nit;

    @Column(name = "almacen_id")
    private java.util.UUID almacenId;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}
