package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.entity;

import edu.dosw.styleradar.pagos_suscripciones.domain.model.TipoSuscriptor;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Map;

@Entity
@Table(name = "planes")
@Data
@NoArgsConstructor
public class PlanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    @Enumerated(EnumType.STRING)
    private TipoSuscriptor tipoSuscriptor;
    private BigDecimal precio;
    private String moneda;
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> limites;
    private boolean activo;
}