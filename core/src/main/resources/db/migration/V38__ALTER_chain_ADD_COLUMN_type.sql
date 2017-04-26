-- -----------------------------------------------------
-- Table `chain`
-- -----------------------------------------------------
ALTER TABLE `chain`
  ADD COLUMN `type` VARCHAR(255) NOT NULL
  AFTER `id`;
