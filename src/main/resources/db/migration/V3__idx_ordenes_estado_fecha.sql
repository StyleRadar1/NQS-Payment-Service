CREATE INDEX IF NOT EXISTS idx_ordenes_pago_estado_fecha ON ordenes_pago (estado, fecha_creacion);
CREATE INDEX IF NOT EXISTS idx_suscripciones_estado_fecha_fin ON suscripciones (estado, fecha_fin);