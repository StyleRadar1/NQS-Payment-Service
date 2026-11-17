package edu.dosw.styleradar.pagos.domain.model;

/**
 * Tipo de cuenta que puede tener una suscripción en StyleRadar.
 *
 * <p>SR-RF-67: el almacén tiene una única suscripción obligatoria.
 * SR-RF-100: el usuario comprador solo puede contratar Radar+.</p>
 */
public enum TipoSuscriptor {
    USUARIO,
    ALMACEN
}
