-- -----------------------------------------------------
-- Table `library`
-- -----------------------------------------------------
ALTER TABLE `library`
  ADD COLUMN `name` VARCHAR(255) NOT NULL
  AFTER `id`;
