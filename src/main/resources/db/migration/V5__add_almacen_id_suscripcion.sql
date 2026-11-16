-- =============================================================================
-- StyleRadar · ms-pagos-suscripciones
-- V5 · UUID del almacén en catálogo/geolocalización
-- SR-RF-99: ms-item-catalogo suspende el catálogo con almacen_id sin consultar
-- la base de datos de ms-tiendas ni cambiar su código.
-- =============================================================================

ALTER TABLE suscripciones ADD COLUMN IF NOT EXISTS almacen_id UUID;
