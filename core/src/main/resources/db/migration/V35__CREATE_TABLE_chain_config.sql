-- -----------------------------------------------------
-- Table `chain_config`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `chain_config` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `chain_id`   BIGINT UNSIGNED NOT NULL,
  `type`       VARCHAR(255)    NOT NULL,
  `value`      VARCHAR(32768)  NOT NULL,
  `created_at` TIMESTAMP                DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `chain_config_fk_chain_idx` (`chain_id` ASC),
  CONSTRAINT `chain_config_fk_chain`
  FOREIGN KEY (`chain_id`)
  REFERENCES `chain` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);
