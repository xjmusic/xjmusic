-- -----------------------------------------------------
-- Table `xj`.`user`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `xj`.`user` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `admin` BOOLEAN DEFAULT false,
  `name` VARCHAR(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC))
  ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `xj`.`user_auth`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `xj`.`user_auth` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `type` VARCHAR(255) NOT NULL,
  `access_token` VARCHAR(1023) NOT NULL,
  `refresh_token` VARCHAR(1023) NOT NULL,
  `account_number` INT UNSIGNED NOT NULL,
  `verified_email` VARCHAR(1023),
  `user_id` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `user_auth_fk_user_idx` (`user_id` ASC),
  CONSTRAINT `user_auth_fk_user`
  FOREIGN KEY (`user_id`)
  REFERENCES `xj`.`user` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;
