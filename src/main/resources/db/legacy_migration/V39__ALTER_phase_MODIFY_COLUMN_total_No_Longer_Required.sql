-- -----------------------------------------------------
-- [#199] Macro-type Idea `total` not required; still is required for other types of Idea
-- -----------------------------------------------------
ALTER TABLE `phase` MODIFY COLUMN
  `total` BIGINT(20) UNSIGNED NULL DEFAULT NULL;
