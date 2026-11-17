-- =============================================================================
-- StyleRadar · ms-pagos-suscripciones
-- V2 · Planes oficiales (SR-RF-67 RN-01, SR-RF-100 RN-02, SR-RF-103)
-- IDs fijos: 1 = Radar (gratuito), 2 = Radar+, 3 = Almacén Básico
-- =============================================================================

INSERT INTO planes (id, nombre, tipo_suscriptor, precio, moneda, limites, activo) VALUES
    (1, 'Radar',           'USUARIO', 0.00,     'COP', '{"max_wishlist": 20, "probador_foto": false, "probador_usos_mes": 0, "alertas_disponibilidad": false}', TRUE),
    (2, 'Radar+',          'USUARIO', 14900.00, 'COP', '{"max_wishlist": -1, "probador_foto": true, "probador_usos_mes": 30, "alertas_disponibilidad": true}', TRUE),
    (3, 'Almacén Básico',  'ALMACEN', 29900.00, 'COP', '{"max_productos": 50}', TRUE);

SELECT setval(pg_get_serial_sequence('planes', 'id'), (SELECT MAX(id) FROM planes));
