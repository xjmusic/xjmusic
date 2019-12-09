-- -----------------------------------------------------
-- Table `arrangement`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `arrangement` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `choice_id` BIGINT UNSIGNED NOT NULL,
  `voice_id` BIGINT UNSIGNED NOT NULL,
  `instrument_id` BIGINT UNSIGNED NOT NULL,
  `name` VARCHAR(255) NOT NULL,
  `density` FLOAT UNSIGNED NOT NULL,
  `tempo` FLOAT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `arrangement_fk_choice_idx` (`choice_id` ASC),
  INDEX `arrangement_fk_voice_idx` (`voice_id` ASC),
  INDEX `arrangement_fk_instrument_idx` (`instrument_id` ASC),
  CONSTRAINT `arrangement_fk_choice`
  FOREIGN KEY (`choice_id`)
  REFERENCES `choice` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `arrangement_fk_voice`
  FOREIGN KEY (`voice_id`)
  REFERENCES `voice` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `arrangement_fk_instrument`
  FOREIGN KEY (`instrument_id`)
  REFERENCES `instrument` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `morph`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `morph` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `arrangement_id` BIGINT UNSIGNED NOT NULL,
  `position` FLOAT UNSIGNED NOT NULL,
  `note` VARCHAR(63) NOT NULL,
  `duration` FLOAT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `morph_fk_arrangement_idx` (`arrangement_id` ASC),
  CONSTRAINT `morph_fk_arrangement`
  FOREIGN KEY (`arrangement_id`)
  REFERENCES `arrangement` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;



-- -----------------------------------------------------
-- Table `point`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `point` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `morph_id` BIGINT UNSIGNED NOT NULL,
  `voice_event_id` BIGINT UNSIGNED NOT NULL,
  `position` FLOAT UNSIGNED NOT NULL,
  `note` VARCHAR(63) NOT NULL,
  `duration` FLOAT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `point_fk_morph_idx` (`morph_id` ASC),
  INDEX `point_fk_voice_event_idx` (`voice_event_id` ASC),
  CONSTRAINT `point_fk_morph`
  FOREIGN KEY (`morph_id`)
  REFERENCES `morph` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `point_fk_voice_event`
  FOREIGN KEY (`voice_event_id`)
  REFERENCES `voice_event` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;



-- -----------------------------------------------------
-- Table `pick`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `pick` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `morph_id` BIGINT UNSIGNED NOT NULL,
  `audio_id` BIGINT UNSIGNED NOT NULL,
  `start` BIGINT UNSIGNED NOT NULL,
  `length` BIGINT UNSIGNED NOT NULL,
  `amplitude` FLOAT UNSIGNED NOT NULL,
  `pitch` FLOAT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `pick_fk_morph_idx` (`morph_id` ASC),
  INDEX `pick_fk_audio_idx` (`audio_id` ASC),
  CONSTRAINT `pick_fk_morph`
  FOREIGN KEY (`morph_id`)
  REFERENCES `morph` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `pick_fk_audio`
  FOREIGN KEY (`audio_id`)
  REFERENCES `audio` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;
