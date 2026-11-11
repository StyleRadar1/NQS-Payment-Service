package edu.dosw.styleradar.pagos_suscripciones.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "webhooks_log")
@Data
@NoArgsConstructor
public class WebhookLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "id_pago_externo", unique = true, nullable = false)
    private String idPagoExterno;
    
    @Column(name = "estado_procesamiento")
    private String estadoProcesamiento;
    
    @Column(name = "fecha_recepcion")
    private LocalDateTime fechaRecepcion;
}