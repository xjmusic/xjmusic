-- -----------------------------------------------------
-- Table `xj`.`library`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `xj`.`library` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `account_id` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `library_fk_account_idx` (`account_id` ASC),
  CONSTRAINT `library_fk_account`
  FOREIGN KEY (`account_id`)
  REFERENCES `xj`.`account` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `xj`.`idea`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `xj`.`idea` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `library_id` INT UNSIGNED NOT NULL,
  `credit_id` INT UNSIGNED NOT NULL,
  `type` VARCHAR(255) NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `density` FLOAT UNSIGNED NOT NULL,
  `key` VARCHAR(255) NOT NULL,
  `tempo` FLOAT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `idea_fk_library_idx` (`library_id` ASC),
  INDEX `idea_fk_credit_idx` (`credit_id` ASC),
  CONSTRAINT `idea_fk_library`
  FOREIGN KEY (`library_id`)
  REFERENCES `xj`.`library` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `idea_fk_credit`
  FOREIGN KEY (`credit_id`)
  REFERENCES `xj`.`credit` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `xj`.`idea_meme`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `xj`.`idea_meme` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `idea_id` INT UNSIGNED NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `order` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `meme_fk_idea_idx` (`idea_id` ASC),
  CONSTRAINT `meme_fk_idea`
  FOREIGN KEY (`idea_id`)
  REFERENCES `xj`.`idea` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `xj`.`phase`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `xj`.`phase` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `idea_id` INT UNSIGNED NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `offset` INT UNSIGNED NOT NULL,
  `total` INT UNSIGNED NOT NULL,
  `density` FLOAT UNSIGNED NOT NULL,
  `key` VARCHAR(255) NOT NULL,
  `tempo` FLOAT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `phase_fk_idea_idx` (`idea_id` ASC),
  CONSTRAINT `phase_fk_idea`
  FOREIGN KEY (`idea_id`)
  REFERENCES `xj`.`idea` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `xj`.`phase_meme`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `xj`.`phase_meme` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `phase_id` INT UNSIGNED NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `order` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `meme_fk_phase_idx` (`phase_id` ASC),
  CONSTRAINT `meme_fk_phase`
  FOREIGN KEY (`phase_id`)
  REFERENCES `xj`.`phase` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;



-- -----------------------------------------------------
-- Table `xj`.`phase_chord`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `xj`.`phase_chord` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `phase_id` INT UNSIGNED NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `position` FLOAT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `chord_fk_phase_idx` (`phase_id` ASC),
  CONSTRAINT `chord_fk_phase`
  FOREIGN KEY (`phase_id`)
  REFERENCES `xj`.`phase` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `xj`.`voice`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `xj`.`voice` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `phase_id` INT UNSIGNED NOT NULL,
  `type` VARCHAR(255) NOT NULL,
  `description` VARCHAR(1023) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `voice_fk_phase_idx` (`phase_id` ASC),
  CONSTRAINT `voice_fk_phase`
  FOREIGN KEY (`phase_id`)
  REFERENCES `xj`.`phase` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `xj`.`voice_event`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `xj`.`voice_event` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `voice_id` INT UNSIGNED NOT NULL,
  `velocity` FLOAT UNSIGNED NOT NULL,
  `tonality` FLOAT UNSIGNED NOT NULL,
  `inflection` VARCHAR(63) NOT NULL,
  `position` FLOAT NOT NULL,
  `duration` FLOAT UNSIGNED NOT NULL,
  `note` VARCHAR(63) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `event_fk_voice_idx` (`voice_id` ASC),
  CONSTRAINT `event_fk_voice`
  FOREIGN KEY (`voice_id`)
  REFERENCES `xj`.`voice` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;

