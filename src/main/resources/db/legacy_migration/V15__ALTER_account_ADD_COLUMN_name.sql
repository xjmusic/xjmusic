-- -----------------------------------------------------
-- Table `account`
-- -----------------------------------------------------
ALTER TABLE `account`
  ADD COLUMN `name` VARCHAR(255) NOT NULL
  AFTER `id`;
