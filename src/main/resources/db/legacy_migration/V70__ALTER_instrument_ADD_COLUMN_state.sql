-- -----------------------------------------------------
-- Table `instrument`
-- -----------------------------------------------------
ALTER TABLE `instrument`
  ADD COLUMN `state` VARCHAR(255) NOT NULL
  AFTER `id`;
