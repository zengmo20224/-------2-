-- Phase 6: Product order delivery method support (pickup / express)
-- Adds delivery_method, address fields to product_order so orders can be
-- fulfilled either by store pickup or by express delivery to an address.
-- Run against the dev MySQL database. Idempotent-ish: guarded by column checks
-- where possible, but ALTER ... ADD COLUMN is not fully idempotent in MySQL —
-- re-running after success will error on duplicate column, which is fine.

-- 1. delivery_method: PICKUP (default, backward compatible) or EXPRESS
ALTER TABLE `product_order`
  ADD COLUMN `delivery_method` VARCHAR(16) NOT NULL DEFAULT 'PICKUP';

-- 2. address_id: FK-ish reference to user_address, used when delivery_method = EXPRESS
ALTER TABLE `product_order`
  ADD COLUMN `address_id` BIGINT DEFAULT NULL;

-- 3. address_snapshot: denormalized address text captured at order time,
--    so later edits/deletes of user_address do not affect historical orders
ALTER TABLE `product_order`
  ADD COLUMN `address_snapshot` VARCHAR(500) DEFAULT NULL;

-- 4. store_id becomes nullable (express orders have no store).
--    MySQL allows widening NOT NULL -> NULL in place.
ALTER TABLE `product_order`
  MODIFY COLUMN `store_id` BIGINT NULL;
