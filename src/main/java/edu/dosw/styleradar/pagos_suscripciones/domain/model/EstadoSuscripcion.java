package edu.dosw.styleradar.pagos_suscripciones.domain.model;

public enum EstadoSuscripcion {
    ACTIVA, VENCIDA, CANCELADA, 
    SUSPENDIDA // Cuando el pago falla y se le retiran los beneficios
}
