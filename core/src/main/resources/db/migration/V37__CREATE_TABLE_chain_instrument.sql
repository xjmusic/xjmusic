-- -----------------------------------------------------
-- Table `chain_instrument`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `chain_instrument` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `chain_id`   BIGINT UNSIGNED NOT NULL,
  `instrument_id`    BIGINT UNSIGNED NOT NULL,
  `created_at` TIMESTAMP                DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `chain_instrument_fk_chain_idx` (`chain_id` ASC),
  INDEX `chain_instrument_fk_instrument_idx` (`instrument_id` ASC),
  CONSTRAINT `chain_instrument_fk_chain`
  FOREIGN KEY (`chain_id`)
  REFERENCES `chain` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `chain_instrument_fk_instrument`
  FOREIGN KEY (`instrument_id`)
  REFERENCES `instrument` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);
