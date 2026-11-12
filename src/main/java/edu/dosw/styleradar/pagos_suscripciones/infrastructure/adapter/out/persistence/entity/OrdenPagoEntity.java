package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.entity;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.EstadoPago;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ordenes_pago")
@Data
@NoArgsConstructor
public class OrdenPagoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long suscripcionId;
    private BigDecimal monto;
    private String moneda;
    @Enumerated(EnumType.STRING)
    private EstadoPago estado;
    private String idPagoExterno;
    private BigDecimal montoReembolsado;
    private String motivoReembolso;
    private LocalDateTime fechaCreacion;
}