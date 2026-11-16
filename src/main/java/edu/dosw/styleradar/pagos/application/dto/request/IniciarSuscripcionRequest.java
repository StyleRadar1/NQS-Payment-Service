package edu.dosw.styleradar.pagos.application.dto.request;

import edu.dosw.styleradar.pagos.domain.model.TipoSuscriptor;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Solicitud para iniciar el pago de una suscripción.
 *
 * <p>SR-RF-67 y SR-RF-100: el suscriptor se obtiene de la sesión
 * ({@code X-User-Id}); el correo del pagador es obligatorio. El {@code planId}
 * es opcional porque el almacén tiene un único plan y el usuario solo puede
 * contratar Radar+ (no hay selección ni comparación de planes).</p>
 */
public record IniciarSuscripcionRequest(
        @NotNull(message = "El tipo de suscriptor es obligatorio.")
        TipoSuscriptor tipoSuscriptor,

        @NotBlank(message = "Ingresa un correo electrónico válido para realizar el pago.")
        String emailPayer,

        Long planId,

        /**
         * NIT del almacén cuando {@code tipoSuscriptor=ALMACEN}. Permite que
         * tiendas y facturación correlacionen la suscripción. Se ignora para USUARIO.
         */
        String nit,

        /**
         * UUID del almacén en el catálogo/geolocalización. Obligatorio para
         * ALMACEN: es el identificador que ms-item-catalogo usa para suspender
         * el catálogo (SR-RF-99) sin consultar la base de datos de ms-tiendas.
         */
        java.util.UUID almacenId) {
}
