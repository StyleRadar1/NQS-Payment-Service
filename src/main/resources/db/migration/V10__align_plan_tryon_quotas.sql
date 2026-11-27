-- V10 · Align virtual try-on quotas for Radar (10/month) and Radar+ (50/month)
UPDATE plans
SET limits = jsonb_set(
        jsonb_set(COALESCE(limits, '{}'::jsonb), '{try_on_photo}', 'true'::jsonb, true),
        '{try_on_uses_per_month}',
        '10'::jsonb,
        true
    )
WHERE id = 1;

UPDATE plans
SET limits = jsonb_set(
        jsonb_set(COALESCE(limits, '{}'::jsonb), '{try_on_photo}', 'true'::jsonb, true),
        '{try_on_uses_per_month}',
        '50'::jsonb,
        true
    )
WHERE id = 2;
