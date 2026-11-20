-- V7 · Store plan price aligned with v4
UPDATE plans
SET price = 89000.00
WHERE id = 3 AND subscriber_type = 'STORE';
