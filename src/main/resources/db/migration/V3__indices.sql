-- =============================================================================
-- StyleRadar · ms-pagos-suscripciones
-- V3 · Índices de consulta e invariantes de negocio
-- =============================================================================

CREATE INDEX IF NOT EXISTS idx_ordenes_pago_estado_fecha
    ON ordenes_pago (estado, fecha_creacion);

CREATE INDEX IF NOT EXISTS idx_suscripciones_estado_fecha_fin
    ON suscripciones (estado, fecha_fin);

CREATE INDEX IF NOT EXISTS idx_suscripciones_suscriptor
    ON suscripciones (suscriptor_id);

CREATE INDEX IF NOT EXISTS idx_ordenes_pago_suscripcion
    ON ordenes_pago (suscripcion_id);

-- SR-RF-67 RN-04 / SR-RF-100: un suscriptor no puede tener dos suscripciones
-- ACTIVAS o PENDIENTES al mismo tiempo. Las SUSPENDIDAS sí pueden coexistir
-- con una nueva activación (SR-RF-99 RN-03).
CREATE UNIQUE INDEX IF NOT EXISTS uq_suscripcion_activa_pendiente
    ON suscripciones (suscriptor_id)
    WHERE estado IN ('ACTIVA', 'PENDIENTE');
