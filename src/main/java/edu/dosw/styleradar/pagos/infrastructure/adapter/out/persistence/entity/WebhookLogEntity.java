package edu.dosw.styleradar.pagos.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;

/** Representación JPA de la tabla {@code webhooks_log} (SR-RNF-14). */
@Entity
@Table(name = "webhooks_log")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_pago_externo", nullable = false, unique = true, length = 100)
    private String idPagoExterno;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_recibido")
    private Map<String, Object> payloadRecibido;

    @Column(name = "estado_procesamiento", nullable = false, length = 50)
    private String estadoProcesamiento;

    @Column(name = "fecha_recepcion")
    private LocalDateTime fechaRecepcion;
}
