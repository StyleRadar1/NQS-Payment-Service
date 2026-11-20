package edu.dosw.styleradar.payments.domain.exception;

public final class Messages {

    public static final String SESSION_REQUIRED =
            "Debes iniciar sesión para continuar.";
    public static final String INVALID_EMAIL =
            "Ingresa un correo electrónico válido para realizar el pago.";
    public static final String INVALID_EMAIL_RADAR_PLUS =
            "Ingresa un correo electrónico válido para activar Radar+.";
    public static final String STORE_ALREADY_SUBSCRIBED =
            "Tu almacén ya tiene una suscripción activa o un pago pendiente.";
    public static final String RADAR_PLUS_ACTIVE =
            "Ya tienes Radar+ activo.";
    public static final String PAYMENT_NOT_APPROVED =
            "El pago no fue aprobado. Puedes intentarlo nuevamente.";
    public static final String PAYMENT_NOT_APPROVED_SIMPLE =
            "El pago no fue aprobado.";
    public static final String PAYMENT_START_FAILED =
            "No fue posible iniciar el pago en este momento. Inténtalo nuevamente.";
    public static final String PAYMENT_PENDING =
            "Tu pago está pendiente de confirmación. Las funcionalidades se habilitarán cuando sea aprobado.";
    public static final String WAITING_CONFIRMATION =
            "Estamos esperando la confirmación de tu pago.";
    public static final String RENEWAL_CANCEL_FAILED =
            "No fue posible desactivar la renovación. Inténtalo nuevamente.";
    public static final String RADAR_PLUS_RENEWAL_CANCEL_FAILED =
            "No fue posible desactivar la renovación de Radar+.";
    public static final String RETRY_FAILED =
            "No fue posible generar un nuevo intento de pago.";
    public static final String NO_PAYMENTS =
            "Aún no tienes pagos de suscripción registrados.";
    public static final String NO_ADMIN_PAYMENTS =
            "No hay pagos de suscripciones registrados.";
    public static final String RADAR_PLUS_ENDED =
            "Tu plan Radar+ terminó. Podrás renovarlo cuando se confirme tu pago.";
    public static final String SUBSCRIPTION_REACTIVATED =
            "Tu suscripción fue reactivada.";
    public static final String SUBSCRIPTION_ACTIVE_NO_RETRY =
            "La suscripción ya está activa. No se requiere reintentar el pago.";
    public static final String ADMIN_ONLY =
            "Solo el administrador de StyleRadar puede realizar esta operación.";
    public static final String OWNER_ONLY =
            "Solo el titular de la cuenta puede consultar esta información.";
    public static final String SUBSCRIPTION_NOT_ACTIVE =
            "La suscripción no está vigente.";
    public static final String PLAN_NOT_AVAILABLE =
            "El plan solicitado no está disponible.";
    public static final String STORE_ID_REQUIRED =
            "El identificador del almacén es obligatorio.";
    public static final String TAX_ID_REQUIRED =
            "El NIT debe tener exactamente 9 dígitos.";
    public static final String ORDER_NOT_REJECTED =
            "Solo se pueden reintentar pagos rechazados.";
    public static final String SUBSCRIBER_TYPE_REQUIRED =
            "El tipo de suscriptor es obligatorio.";
    public static final String CONFIRM_CANCELLATION_REQUIRED =
            "Debes confirmar la cancelación de la renovación.";

    private Messages() {
    }
}
