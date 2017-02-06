-- -----------------------------------------------------
-- Table `audio`
-- -----------------------------------------------------
ALTER TABLE `audio` MODIFY COLUMN
  `start` FLOAT UNSIGNED DEFAULT NULL;

ALTER TABLE `audio` MODIFY COLUMN
  `length` FLOAT UNSIGNED DEFAULT NULL;
