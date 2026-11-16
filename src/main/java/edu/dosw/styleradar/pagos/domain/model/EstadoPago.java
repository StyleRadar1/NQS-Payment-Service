package edu.dosw.styleradar.pagos.domain.model;

/**
 * Estados de un pago de suscripción.
 *
 * <p>SR-RF-75 RN-03 y SR-RF-97: los únicos estados son
 * PENDIENTE, APROBADO, RECHAZADO y REEMBOLSADO.</p>
 */
public enum EstadoPago {
    PENDIENTE,
    APROBADO,
    RECHAZADO,
    REEMBOLSADO
}
