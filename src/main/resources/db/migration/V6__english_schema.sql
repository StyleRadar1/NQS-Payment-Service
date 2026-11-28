-- =============================================================================
-- StyleRadar · ms-payments-subscriptions
-- V6 · English schema, enum values and plan limits
-- Keeps V1-V5 immutable: existing databases migrate in place.
-- =============================================================================

-- 1. Tables
ALTER TABLE planes RENAME TO plans;
ALTER TABLE suscripciones RENAME TO subscriptions;
ALTER TABLE ordenes_pago RENAME TO payment_orders;
ALTER TABLE webhooks_log RENAME TO webhook_logs;

-- 2. Columns: plans
ALTER TABLE plans RENAME COLUMN nombre TO name;
ALTER TABLE plans RENAME COLUMN tipo_suscriptor TO subscriber_type;
ALTER TABLE plans RENAME COLUMN precio TO price;
ALTER TABLE plans RENAME COLUMN moneda TO currency;
ALTER TABLE plans RENAME COLUMN limites TO limits;
ALTER TABLE plans RENAME COLUMN activo TO active;
ALTER TABLE plans RENAME COLUMN fecha_creacion TO created_at;

-- 3. Columns: subscriptions
ALTER TABLE subscriptions RENAME COLUMN plan_programado_id TO scheduled_plan_id;
ALTER TABLE subscriptions RENAME COLUMN suscriptor_id TO subscriber_id;
ALTER TABLE subscriptions RENAME COLUMN tipo_suscriptor TO subscriber_type;
ALTER TABLE subscriptions RENAME COLUMN estado TO status;
ALTER TABLE subscriptions RENAME COLUMN fecha_inicio TO start_date;
ALTER TABLE subscriptions RENAME COLUMN fecha_fin TO end_date;
ALTER TABLE subscriptions RENAME COLUMN auto_renovacion TO auto_renewal;
ALTER TABLE subscriptions RENAME COLUMN preapproval_id_mp TO mercado_pago_preapproval_id;
ALTER TABLE subscriptions RENAME COLUMN nit TO tax_id;
ALTER TABLE subscriptions RENAME COLUMN almacen_id TO store_id;
ALTER TABLE subscriptions RENAME COLUMN fecha_actualizacion TO updated_at;

-- 4. Columns: payment_orders
ALTER TABLE payment_orders RENAME COLUMN suscripcion_id TO subscription_id;
ALTER TABLE payment_orders RENAME COLUMN monto TO amount;
ALTER TABLE payment_orders RENAME COLUMN moneda TO currency;
ALTER TABLE payment_orders RENAME COLUMN estado TO status;
ALTER TABLE payment_orders RENAME COLUMN id_pago_externo TO external_payment_id;
ALTER TABLE payment_orders RENAME COLUMN monto_reembolsado TO refunded_amount;
ALTER TABLE payment_orders RENAME COLUMN motivo_reembolso TO refund_reason;
ALTER TABLE payment_orders RENAME COLUMN fecha_creacion TO created_at;

-- 5. Columns: webhook_logs
ALTER TABLE webhook_logs RENAME COLUMN id_pago_externo TO external_payment_id;
ALTER TABLE webhook_logs RENAME COLUMN payload_recibido TO received_payload;
ALTER TABLE webhook_logs RENAME COLUMN estado_procesamiento TO processing_status;
ALTER TABLE webhook_logs RENAME COLUMN fecha_recepcion TO received_at;

-- 6. Drop legacy checks before translating values
ALTER TABLE plans DROP CONSTRAINT ck_planes_tipo;
ALTER TABLE plans DROP CONSTRAINT ck_planes_precio;
ALTER TABLE subscriptions DROP CONSTRAINT ck_suscripciones_tipo;
ALTER TABLE subscriptions DROP CONSTRAINT ck_suscripciones_estado;
ALTER TABLE subscriptions DROP CONSTRAINT ck_suscripciones_fechas;
ALTER TABLE payment_orders DROP CONSTRAINT ck_ordenes_estado;
ALTER TABLE payment_orders DROP CONSTRAINT ck_ordenes_monto;
ALTER TABLE payment_orders DROP CONSTRAINT ck_ordenes_reembolso;

-- 7. Translate enum values
UPDATE subscriptions
SET subscriber_type = CASE subscriber_type
        WHEN 'USUARIO' THEN 'USER'
        WHEN 'ALMACEN' THEN 'STORE'
        ELSE subscriber_type
    END,
    status = CASE status
        WHEN 'PENDIENTE' THEN 'PENDING'
        WHEN 'ACTIVA' THEN 'ACTIVE'
        WHEN 'SUSPENDIDA' THEN 'SUSPENDED'
        ELSE status
    END;

UPDATE payment_orders
SET status = CASE status
        WHEN 'PENDIENTE' THEN 'PENDING'
        WHEN 'APROBADO' THEN 'APPROVED'
        WHEN 'RECHAZADO' THEN 'REJECTED'
        WHEN 'REEMBOLSADO' THEN 'REFUNDED'
        ELSE status
    END;

UPDATE plans
SET subscriber_type = CASE subscriber_type
        WHEN 'USUARIO' THEN 'USER'
        WHEN 'ALMACEN' THEN 'STORE'
        ELSE subscriber_type
    END,
    name = CASE id
        WHEN 3 THEN 'Store Basic'
        ELSE name
    END;

-- 8. Translate plan limits JSON keys
UPDATE plans
SET limits = jsonb_build_object(
        'max_wishlist', COALESCE((limits->>'max_wishlist')::int, 0),
        'try_on_photo', COALESCE((limits->>'probador_foto')::boolean, false),
        'try_on_uses_per_month', COALESCE((limits->>'probador_usos_mes')::int, 0),
        'availability_alerts', COALESCE((limits->>'alertas_disponibilidad')::boolean, false)
    )
WHERE id IN (1, 2);

UPDATE plans
SET limits = jsonb_build_object(
        'max_products', COALESCE((limits->>'max_productos')::int, 50)
    )
WHERE id = 3;

-- 9. New checks in English
ALTER TABLE plans
    ADD CONSTRAINT ck_plans_subscriber_type CHECK (subscriber_type IN ('USER', 'STORE')),
    ADD CONSTRAINT ck_plans_price CHECK (price >= 0);

ALTER TABLE subscriptions
    ADD CONSTRAINT ck_subscriptions_subscriber_type CHECK (subscriber_type IN ('USER', 'STORE')),
    ADD CONSTRAINT ck_subscriptions_status CHECK (status IN ('PENDING', 'ACTIVE', 'SUSPENDED')),
    ADD CONSTRAINT ck_subscriptions_dates CHECK (end_date IS NULL OR start_date IS NULL OR end_date >= start_date);

ALTER TABLE payment_orders
    ADD CONSTRAINT ck_payment_orders_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'REFUNDED')),
    ADD CONSTRAINT ck_payment_orders_amount CHECK (amount >= 0),
    ADD CONSTRAINT ck_payment_orders_refund CHECK (refunded_amount >= 0 AND refunded_amount <= amount);

-- 10. Rename foreign keys and indexes
ALTER TABLE subscriptions RENAME CONSTRAINT fk_suscripcion_plan TO fk_subscriptions_plan;
ALTER TABLE payment_orders RENAME CONSTRAINT fk_orden_suscripcion TO fk_payment_orders_subscription;

ALTER INDEX idx_ordenes_pago_estado_fecha RENAME TO idx_payment_orders_status_created_at;
ALTER INDEX idx_suscripciones_estado_fecha_fin RENAME TO idx_subscriptions_status_end_date;
ALTER INDEX idx_suscripciones_suscriptor RENAME TO idx_subscriptions_subscriber;
ALTER INDEX idx_ordenes_pago_suscripcion RENAME TO idx_payment_orders_subscription;

-- The partial unique index must use the new English status values.
DROP INDEX uq_suscripcion_activa_pendiente;
CREATE UNIQUE INDEX uq_active_pending_subscription
    ON subscriptions (subscriber_id)
    WHERE status IN ('ACTIVE', 'PENDING');

