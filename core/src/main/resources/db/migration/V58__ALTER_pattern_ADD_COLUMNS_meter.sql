---
--- [#159669804] Artist wants a step sequencer in order to compose rhythm patterns in a familiar way.
---
ALTER TABLE `pattern`
  ADD COLUMN `meter_super` INT DEFAULT NULL;

ALTER TABLE `pattern`
  ADD COLUMN `meter_sub` INT DEFAULT NULL;

ALTER TABLE `pattern`
  ADD COLUMN `meter_swing` INT DEFAULT NULL;
