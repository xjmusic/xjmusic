-- -----------------------------------------------------
-- Table `chain`
-- -----------------------------------------------------
ALTER TABLE `chain`
  ADD COLUMN `name` VARCHAR(255) NOT NULL
  AFTER `id`;

ALTER TABLE `chain`
  ADD COLUMN `state` VARCHAR(255) NOT NULL;

ALTER TABLE `chain`
  ADD COLUMN `start_at` DATETIME NOT NULL;

ALTER TABLE `chain`
  ADD COLUMN `stop_at` DATETIME DEFAULT NULL;
