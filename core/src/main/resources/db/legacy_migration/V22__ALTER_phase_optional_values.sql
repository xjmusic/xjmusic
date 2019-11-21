-- -----------------------------------------------------
-- Table `phase`
-- -----------------------------------------------------
ALTER TABLE `phase` MODIFY COLUMN
  `name` VARCHAR(255) DEFAULT NULL;

ALTER TABLE `phase` MODIFY COLUMN
  `density` FLOAT UNSIGNED DEFAULT NULL;

  ALTER TABLE `phase` MODIFY COLUMN
  `key` VARCHAR(255) DEFAULT NULL;

ALTER TABLE `phase` MODIFY COLUMN
  `tempo` FLOAT UNSIGNED DEFAULT NULL;
