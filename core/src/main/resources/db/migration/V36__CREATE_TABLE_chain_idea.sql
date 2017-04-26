-- -----------------------------------------------------
-- Table `chain_idea`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `chain_idea` (
  `id`         BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `chain_id`   BIGINT UNSIGNED NOT NULL,
  `idea_id`    BIGINT UNSIGNED NOT NULL,
  `created_at` TIMESTAMP                DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP                DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `chain_idea_fk_chain_idx` (`chain_id` ASC),
  INDEX `chain_idea_fk_idea_idx` (`idea_id` ASC),
  CONSTRAINT `chain_idea_fk_chain`
  FOREIGN KEY (`chain_id`)
  REFERENCES `chain` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `chain_idea_fk_idea`
  FOREIGN KEY (`idea_id`)
  REFERENCES `idea` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION
);
