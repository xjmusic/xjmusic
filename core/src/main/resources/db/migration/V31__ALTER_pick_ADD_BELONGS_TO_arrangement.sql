-- -----------------------------------------------------
-- Table `pick`
-- -----------------------------------------------------
ALTER TABLE `pick`
  ADD COLUMN `arrangement_id` BIGINT UNSIGNED NOT NULL
  AFTER `id`;

ALTER TABLE `pick`
  ADD INDEX `pick_fk_arrangement_idx` (`arrangement_id` ASC);

ALTER TABLE `pick`
  ADD CONSTRAINT `pick_fk_arrangement`
  FOREIGN KEY (`arrangement_id`)
  REFERENCES `arrangement` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;




