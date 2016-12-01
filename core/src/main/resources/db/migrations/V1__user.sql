-- -----------------------------------------------------
-- Table `xj`.`user`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `xj`.`user` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `google_token` VARCHAR(5000) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC))
  ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `xj`.`credit`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `xj`.`credit` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `credit_fk_user_idx` (`user_id` ASC),
  CONSTRAINT `credit_fk_user`
  FOREIGN KEY (`user_id`)
  REFERENCES `xj`.`user` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;
