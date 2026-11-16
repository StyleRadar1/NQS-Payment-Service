-- =============================================================================
-- StyleRadar · ms-pagos-suscripciones
-- V4 · NIT del almacén para correlacionar la suspensión del catálogo
-- SR-RF-99: ms-item-catalogo puede ocultar el catálogo por NIT sin consultar
-- la base de datos de ms-tiendas (evita acoplamiento y código adicional).
-- =============================================================================

ALTER TABLE suscripciones ADD COLUMN IF NOT EXISTS nit VARCHAR(30);
