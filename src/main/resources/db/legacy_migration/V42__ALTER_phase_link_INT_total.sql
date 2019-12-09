-- -----------------------------------------------------
-- Table `phase` - total will never be larger than an INTEGER value
-- -----------------------------------------------------
ALTER TABLE `phase` MODIFY COLUMN
  `total` INTEGER UNSIGNED NULL DEFAULT NULL;

-- -----------------------------------------------------
-- Table `link` - total will never be larger than an INTEGER value
-- -----------------------------------------------------
ALTER TABLE `link` MODIFY COLUMN
  `total` INTEGER  UNSIGNED NULL DEFAULT NULL;
