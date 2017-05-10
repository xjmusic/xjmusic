-- -----------------------------------------------------
-- choice table: change column `phase offset` to support ULong
-- -----------------------------------------------------
ALTER TABLE `choice` MODIFY COLUMN
  `phase_offset` BIGINT(20) UNSIGNED NOT NULL;
