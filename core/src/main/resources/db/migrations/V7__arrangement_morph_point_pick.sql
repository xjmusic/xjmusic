-- -----------------------------------------------------
-- Table `xj`.`arrangement`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `xj`.`arrangement` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `choice_id` INT UNSIGNED NOT NULL,
  `voice_id` INT UNSIGNED NOT NULL,
  `instrument_id` INT UNSIGNED NOT NULL,
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
  REFERENCES `xj`.`choice` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `arrangement_fk_voice`
  FOREIGN KEY (`voice_id`)
  REFERENCES `xj`.`voice` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `arrangement_fk_instrument`
  FOREIGN KEY (`instrument_id`)
  REFERENCES `xj`.`instrument` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `xj`.`morph`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `xj`.`morph` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `arrangement_id` INT UNSIGNED NOT NULL,
  `position` FLOAT UNSIGNED NOT NULL,
  `note` VARCHAR(63) NOT NULL,
  `duration` FLOAT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `morph_fk_arrangement_idx` (`arrangement_id` ASC),
  CONSTRAINT `morph_fk_arrangement`
  FOREIGN KEY (`arrangement_id`)
  REFERENCES `xj`.`arrangement` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;



-- -----------------------------------------------------
-- Table `xj`.`point`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `xj`.`point` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `morph_id` INT UNSIGNED NOT NULL,
  `voice_event_id` INT UNSIGNED NOT NULL,
  `position` FLOAT UNSIGNED NOT NULL,
  `note` VARCHAR(63) NOT NULL,
  `duration` FLOAT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `point_fk_morph_idx` (`morph_id` ASC),
  INDEX `point_fk_voice_event_idx` (`voice_event_id` ASC),
  CONSTRAINT `point_fk_morph`
  FOREIGN KEY (`morph_id`)
  REFERENCES `xj`.`morph` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `point_fk_voice_event`
  FOREIGN KEY (`voice_event_id`)
  REFERENCES `xj`.`voice_event` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;



-- -----------------------------------------------------
-- Table `xj`.`pick`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `xj`.`pick` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `morph_id` INT UNSIGNED NOT NULL,
  `audio_id` INT UNSIGNED NOT NULL,
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
  REFERENCES `xj`.`morph` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `pick_fk_audio`
  FOREIGN KEY (`audio_id`)
  REFERENCES `xj`.`audio` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;