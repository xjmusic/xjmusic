-- -----------------------------------------------------
-- Table `chain`
-- -----------------------------------------------------
ALTER TABLE `chain`
  ADD COLUMN `account_id` BIGINT UNSIGNED NOT NULL
  AFTER `id`;

ALTER TABLE `chain`
  ADD CONSTRAINT `chain_fk_account`
  FOREIGN KEY (`account_id`)
  REFERENCES `account` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION;

-- -----------------------------------------------------
-- Table `chain_library`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `chain_library` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `chain_id` BIGINT UNSIGNED NOT NULL,
  `library_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `chain_library_fk_chain_idx` (`chain_id` ASC),
  INDEX `chain_library_fk_library_idx` (`library_id` ASC),
  CONSTRAINT `chain_library_fk_chain`
  FOREIGN KEY (`chain_id`)
  REFERENCES `chain` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `chain_library_fk_library`
  FOREIGN KEY (`library_id`)
  REFERENCES `library` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;
