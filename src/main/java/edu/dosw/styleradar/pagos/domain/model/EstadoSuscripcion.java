package edu.dosw.styleradar.pagos.domain.model;

/**
 * Estados del ciclo de vida de una suscripción.
 *
 * <p>SR-RF-67: la suscripción nace PENDIENTE y pasa a ACTIVA únicamente
 * cuando Mercado Pago confirma un pago APROBADO.
 * SR-RF-99 y SR-RF-103: al vencer sin pago aprobado pasa a SUSPENDIDA,
 * sin periodo de gracia.</p>
 */
public enum EstadoSuscripcion {
    PENDIENTE,
    ACTIVA,
    SUSPENDIDA
}
