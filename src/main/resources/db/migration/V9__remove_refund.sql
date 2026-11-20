-- V9 - remove the retired refund feature (SR-RF-98 is not part of v4)
-- Normalize historical refunded rows before retiring the REFUNDED state.
UPDATE payment_orders SET status = 'APPROVED' WHERE status = 'REFUNDED';

ALTER TABLE payment_orders DROP CONSTRAINT ck_payment_orders_refund;
ALTER TABLE payment_orders DROP CONSTRAINT ck_payment_orders_status;
ALTER TABLE payment_orders DROP COLUMN refunded_amount;
ALTER TABLE payment_orders DROP COLUMN refund_reason;

ALTER TABLE payment_orders
    ADD CONSTRAINT ck_payment_orders_status
    CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'));
