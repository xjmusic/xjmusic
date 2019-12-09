-- -----------------------------------------------------
-- Table `phase`
-- -----------------------------------------------------
ALTER TABLE `phase` MODIFY COLUMN
  `offset` BIGINT UNSIGNED NOT NULL;
ALTER TABLE `phase` MODIFY COLUMN
  `total` BIGINT UNSIGNED NOT NULL;

-- -----------------------------------------------------
-- Table `link`
-- -----------------------------------------------------
ALTER TABLE `link` MODIFY COLUMN
  `offset` BIGINT UNSIGNED NOT NULL;
ALTER TABLE `link` MODIFY COLUMN
  `total` BIGINT UNSIGNED NOT NULL;
