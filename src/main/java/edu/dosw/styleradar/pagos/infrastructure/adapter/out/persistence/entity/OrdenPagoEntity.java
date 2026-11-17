package edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence.entity;

import edu.dosw.styleradar.pagos.domain.model.EstadoPago;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Representación JPA de la tabla {@code ordenes_pago}. */
@Entity
@Table(name = "ordenes_pago")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrdenPagoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "suscripcion_id", nullable = false)
    private Long suscripcionId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, length = 3)
    private String moneda;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPago estado;

    @Column(name = "id_pago_externo", length = 100, unique = true)
    private String idPagoExterno;

    @Column(name = "monto_reembolsado", precision = 10, scale = 2)
    private BigDecimal montoReembolsado;

    @Column(name = "motivo_reembolso", length = 255)
    private String motivoReembolso;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "suscripcion_id", insertable = false, updatable = false)
    private SuscripcionEntity suscripcion;
}
