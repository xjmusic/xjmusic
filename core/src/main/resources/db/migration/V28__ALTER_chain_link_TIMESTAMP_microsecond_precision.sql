-- -----------------------------------------------------
-- Table `chain`
-- -----------------------------------------------------
ALTER TABLE `chain` MODIFY COLUMN `start_at` TIMESTAMP(6) NULL DEFAULT NULL;
ALTER TABLE `chain` MODIFY COLUMN `stop_at` TIMESTAMP(6) NULL DEFAULT NULL;

-- -----------------------------------------------------
-- Table `link`
-- -----------------------------------------------------
ALTER TABLE `link` CHANGE `start` `begin_at` TIMESTAMP(6) NULL DEFAULT NULL;
ALTER TABLE `link` CHANGE `finish` `end_at` TIMESTAMP(6) NULL DEFAULT NULL;
