-- =============================================================================
-- StyleRadar · ms-pagos-suscripciones
-- V1 · Esquema base (SR-RNF-14, SR-RF-67/93/94/97/98)
-- =============================================================================

CREATE TABLE planes (
    id                BIGSERIAL PRIMARY KEY,
    nombre            VARCHAR(100)  NOT NULL,
    tipo_suscriptor   VARCHAR(20)   NOT NULL,
    precio            DECIMAL(10,2) NOT NULL,
    moneda            VARCHAR(3)    NOT NULL DEFAULT 'COP',
    limites           JSONB,
    activo            BOOLEAN       NOT NULL DEFAULT TRUE,
    fecha_creacion    TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_planes_tipo CHECK (tipo_suscriptor IN ('USUARIO', 'ALMACEN')),
    CONSTRAINT ck_planes_precio CHECK (precio >= 0)
);

CREATE TABLE suscripciones (
    id                    BIGSERIAL PRIMARY KEY,
    plan_id               BIGINT       NOT NULL,
    plan_programado_id    BIGINT,
    suscriptor_id         VARCHAR(50)  NOT NULL,
    tipo_suscriptor       VARCHAR(20)  NOT NULL,
    estado                VARCHAR(30)  NOT NULL,
    fecha_inicio          DATE,
    fecha_fin             DATE,
    auto_renovacion       BOOLEAN      NOT NULL DEFAULT TRUE,
    preapproval_id_mp     VARCHAR(100),
    fecha_actualizacion   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_suscripcion_plan FOREIGN KEY (plan_id) REFERENCES planes(id) ON DELETE RESTRICT,
    CONSTRAINT ck_suscripciones_tipo CHECK (tipo_suscriptor IN ('USUARIO', 'ALMACEN')),
    CONSTRAINT ck_suscripciones_estado CHECK (estado IN ('PENDIENTE', 'ACTIVA', 'SUSPENDIDA')),
    CONSTRAINT ck_suscripciones_fechas CHECK (fecha_fin IS NULL OR fecha_inicio IS NULL OR fecha_fin >= fecha_inicio)
);

CREATE TABLE ordenes_pago (
    id                  BIGSERIAL PRIMARY KEY,
    suscripcion_id      BIGINT        NOT NULL,
    monto               DECIMAL(10,2) NOT NULL,
    moneda              VARCHAR(3)    NOT NULL,
    estado              VARCHAR(20)   NOT NULL,
    id_pago_externo     VARCHAR(100)  UNIQUE,
    monto_reembolsado   DECIMAL(10,2) DEFAULT 0.00,
    motivo_reembolso    VARCHAR(255),
    fecha_creacion      TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orden_suscripcion FOREIGN KEY (suscripcion_id) REFERENCES suscripciones(id) ON DELETE CASCADE,
    CONSTRAINT ck_ordenes_estado CHECK (estado IN ('PENDIENTE', 'APROBADO', 'RECHAZADO', 'REEMBOLSADO')),
    CONSTRAINT ck_ordenes_monto CHECK (monto >= 0),
    CONSTRAINT ck_ordenes_reembolso CHECK (monto_reembolsado >= 0 AND monto_reembolsado <= monto)
);

CREATE TABLE webhooks_log (
    id                      BIGSERIAL PRIMARY KEY,
    id_pago_externo         VARCHAR(100) NOT NULL UNIQUE,
    payload_recibido        JSONB,
    estado_procesamiento    VARCHAR(50)  NOT NULL,
    fecha_recepcion         TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
