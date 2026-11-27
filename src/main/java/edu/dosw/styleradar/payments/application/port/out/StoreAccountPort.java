package edu.dosw.styleradar.payments.application.port.out;

/**
 * Consulta la tienda administrada por el usuario autenticado para comprobar
 * que el NIT enviado al iniciar una suscripción pertenece realmente a su tienda.
 */
public interface StoreAccountPort {

    /**
     * @param userId identificador del usuario autenticado
     * @param taxId NIT declarado en la solicitud
     * @param authorization cabecera Authorization del usuario; se reenvía a ms-tiendas
     * @return true solo si ms-tiendas confirma que el NIT pertenece a la tienda del usuario
     */
    boolean belongsToStore(String userId, String taxId, String authorization);
}
