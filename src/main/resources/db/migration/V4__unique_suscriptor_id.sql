-- Garantiza UNA suscripción por suscriptor (fix integridad de datos).
-- Falla a propósito si existen duplicados → limpiar manualmente y reintentar.

DO $$
DECLARE
    v_dups INT;
BEGIN
    SELECT COUNT(*) INTO v_dups FROM (
        SELECT suscriptor_id FROM suscripciones GROUP BY suscriptor_id HAVING COUNT(*) > 1
    ) t;
    IF v_dups > 0 THEN
        RAISE EXCEPTION 'Hay % suscriptor(es) con múltiples suscripciones. Consolidar antes de aplicar el constraint.', v_dups;
    END IF;
END $$;

ALTER TABLE suscripciones ADD CONSTRAINT uq_suscripciones_suscriptor UNIQUE (suscriptor_id);