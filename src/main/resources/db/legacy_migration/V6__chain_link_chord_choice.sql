-- -----------------------------------------------------
-- Table `chain`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `chain` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC))
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `link`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `link` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `chain_id` BIGINT UNSIGNED NOT NULL,
  `offset` INT UNSIGNED NOT NULL,
  `state` VARCHAR(255) NOT NULL,
  `start` BIGINT UNSIGNED NOT NULL,
  `finish` BIGINT UNSIGNED NOT NULL,
  `total` INT UNSIGNED NOT NULL,
  `density` FLOAT UNSIGNED NOT NULL,
  `key` VARCHAR(255) NOT NULL,
  `tempo` FLOAT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `link_fk_chain_idx` (`chain_id` ASC),
  CONSTRAINT `link_fk_chain`
  FOREIGN KEY (`chain_id`)
  REFERENCES `chain` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `link_chord`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `link_chord` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `link_id` BIGINT UNSIGNED NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `position` FLOAT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `chord_fk_link_idx` (`link_id` ASC),
  CONSTRAINT `chord_fk_link`
  FOREIGN KEY (`link_id`)
  REFERENCES `link` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `choice`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `choice` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `link_id` BIGINT UNSIGNED NOT NULL,
  `idea_id` BIGINT UNSIGNED NOT NULL,
  `type` VARCHAR(255) NOT NULL,
  `transpose` INT NOT NULL,
  `phase_offset` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `choice_fk_link_idx` (`link_id` ASC),
  INDEX `choice_fk_idea_idx` (`idea_id` ASC),
  CONSTRAINT `choice_fk_link`
  FOREIGN KEY (`link_id`)
  REFERENCES `link` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `choice_fk_idea`
  FOREIGN KEY (`idea_id`)
  REFERENCES `idea` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;
