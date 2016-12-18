-- -----------------------------------------------------
-- Table `xj`.`instrument`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `xj`.`instrument` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `library_id` BIGINT UNSIGNED NOT NULL,
  `credit_id` BIGINT UNSIGNED NOT NULL,
  `type` VARCHAR(255) NOT NULL,
  `description` VARCHAR(1023) NOT NULL,
  `density` FLOAT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `instrument_fk_library_idx` (`library_id` ASC),
  INDEX `instrument_fk_credit_idx` (`credit_id` ASC),
  CONSTRAINT `instrument_fk_library`
  FOREIGN KEY (`library_id`)
  REFERENCES `xj`.`library` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `instrument_fk_credit`
  FOREIGN KEY (`credit_id`)
  REFERENCES `xj`.`credit` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `xj`.`instrument_meme`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `xj`.`instrument_meme` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `instrument_id` BIGINT UNSIGNED NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `order` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `meme_fk_instrument_idx` (`instrument_id` ASC),
  CONSTRAINT `meme_fk_instrument`
  FOREIGN KEY (`instrument_id`)
  REFERENCES `xj`.`instrument` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `xj`.`audio`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `xj`.`audio` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `instrument_id` BIGINT UNSIGNED NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `waveform` VARCHAR(1023) NOT NULL,
  `start` BIGINT UNSIGNED NOT NULL,
  `length` BIGINT UNSIGNED NOT NULL,
  `tempo` FLOAT UNSIGNED NOT NULL,
  `pitch` FLOAT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `audio_fk_instrument_idx` (`instrument_id` ASC),
  CONSTRAINT `audio_fk_instrument`
  FOREIGN KEY (`instrument_id`)
  REFERENCES `xj`.`instrument` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `xj`.`audio_event`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `xj`.`audio_event` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `audio_id` BIGINT UNSIGNED NOT NULL,
  `velocity` FLOAT UNSIGNED NOT NULL,
  `tonality` FLOAT UNSIGNED NOT NULL,
  `inflection` VARCHAR(63) NOT NULL,
  `position` FLOAT NOT NULL,
  `duration` FLOAT UNSIGNED NOT NULL,
  `note` VARCHAR(63) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `event_fk_audio_idx` (`audio_id` ASC),
  CONSTRAINT `event_fk_audio`
  FOREIGN KEY (`audio_id`)
  REFERENCES `xj`.`audio` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `xj`.`audio_chord`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `xj`.`audio_chord` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `audio_id` BIGINT UNSIGNED NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `position` FLOAT NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `chord_fk_audio_idx` (`audio_id` ASC),
  CONSTRAINT `chord_fk_audio`
  FOREIGN KEY (`audio_id`)
  REFERENCES `xj`.`audio` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;
