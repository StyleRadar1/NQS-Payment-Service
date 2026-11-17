package edu.dosw.styleradar.pagos.domain.exception;

/**
 * Mensajes literales definidos en el análisis de requerimientos.
 *
 * <p>Se centralizan para que controladores, casos de uso y pruebas usen
 * exactamente el mismo texto (SR-RF-67, 68, 93, 96, 97, 98, 100, 101, 103).</p>
 */
public final class Mensajes {

    public static final String CORREO_INVALIDO =
            "Ingresa un correo electrónico válido para realizar el pago.";
    public static final String CORREO_INVALIDO_RADAR_PLUS =
            "Ingresa un correo electrónico válido para activar Radar+.";
    public static final String ALMACEN_YA_SUSCRITO =
            "Tu almacén ya tiene una suscripción activa o un pago pendiente.";
    public static final String RADAR_PLUS_ACTIVO =
            "Ya tienes Radar+ activo.";
    public static final String PAGO_NO_APROBADO =
            "El pago no fue aprobado. Puedes intentarlo nuevamente.";
    public static final String PAGO_NO_APROBADO_SIMPLE =
            "El pago no fue aprobado";
    public static final String NO_INICIAR_PAGO =
            "No fue posible iniciar el pago en este momento. Inténtalo nuevamente.";
    public static final String PAGO_PENDIENTE =
            "Tu pago está pendiente de confirmación. Las funcionalidades se habilitarán cuando sea aprobado.";
    public static final String ESPERANDO_CONFIRMACION =
            "Estamos esperando la confirmación de tu pago.";
    public static final String NO_CANCELAR_RENOVACION =
            "No fue posible cancelar la renovación. Inténtalo nuevamente.";
    public static final String NO_CANCELAR_RADAR_PLUS =
            "No fue posible cancelar la renovación de Radar+.";
    public static final String MONTO_SUPERA_SALDO =
            "El monto solicitado supera el saldo disponible para reembolsar.";
    public static final String NO_REEMBOLSO =
            "No fue posible procesar el reembolso. Inténtalo nuevamente.";
    public static final String MOTIVO_REEMBOLSO =
            "Debes indicar el motivo del reembolso.";
    public static final String NO_REINTENTO =
            "No fue posible generar un nuevo intento de pago.";
    public static final String SIN_PAGOS =
            "Aún no tienes pagos de suscripción registrados.";
    public static final String SIN_PAGOS_ADMIN =
            "No hay pagos de suscripciones registrados.";
    public static final String RADAR_PLUS_TERMINO =
            "Tu plan Radar+ terminó. Podrás renovarlo cuando se confirme tu pago.";
    public static final String REACTIVADA =
            "Tu suscripción fue reactivada.";
    public static final String SUSCRIPCION_ACTIVA_NO_REINTENTO =
            "La suscripción ya está activa. No se requiere reintentar el pago.";
    public static final String SOLO_ADMIN =
            "Solo el administrador de StyleRadar puede realizar esta operación.";
    public static final String SOLO_TITULAR =
            "Solo el titular de la cuenta puede consultar esta información.";
    public static final String SUSCRIPCION_NO_VIGENTE =
            "La suscripción no está vigente.";
    public static final String PLAN_NO_DISPONIBLE =
            "El plan solicitado no está disponible.";
    public static final String ALMACEN_ID_REQUERIDO =
            "El identificador del almacén es obligatorio.";
    public static final String MONTO_INVALIDO =
            "El monto del reembolso debe ser mayor que cero.";
    public static final String PAGO_NO_APROBADO_REEMBOLSO =
            "Solo se pueden reembolsar pagos aprobados.";
    public static final String PAGO_YA_REEMBOLSADO =
            "El pago ya fue reembolsado en su totalidad.";
    public static final String ORDEN_NO_RECHAZADA =
            "Solo se pueden reintentar pagos rechazados.";

    private Mensajes() {
    }
}
