CREATE TABLE planes (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    tipo_suscriptor VARCHAR(20) NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    moneda VARCHAR(3) NOT NULL DEFAULT 'COP',
    limites JSONB,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE suscripciones (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL,
    plan_programado_id BIGINT,
    suscriptor_id VARCHAR(50) NOT NULL,
    tipo_suscriptor VARCHAR(20) NOT NULL,
    estado VARCHAR(30) NOT NULL,
    fecha_inicio DATE NOT NULL,
    fecha_fin DATE NOT NULL,
    auto_renovacion BOOLEAN NOT NULL DEFAULT TRUE,
    dias_periodo_gracia INT NOT NULL DEFAULT 3,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_plan FOREIGN KEY (plan_id) REFERENCES planes(id) ON DELETE RESTRICT
);

CREATE TABLE ordenes_pago (
    id BIGSERIAL PRIMARY KEY,
    suscripcion_id BIGINT NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    moneda VARCHAR(3) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    id_pago_externo VARCHAR(100) UNIQUE,
    monto_reembolsado DECIMAL(10,2) DEFAULT 0.00,
    motivo_reembolso VARCHAR(255),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_suscripcion FOREIGN KEY (suscripcion_id) REFERENCES suscripciones(id) ON DELETE CASCADE
);

CREATE TABLE webhooks_log (
    id BIGSERIAL PRIMARY KEY,
    id_pago_externo VARCHAR(100) UNIQUE NOT NULL,
    payload_recibido JSONB,
    estado_procesamiento VARCHAR(50) NOT NULL,
    fecha_recepcion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);