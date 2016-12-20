-- -----------------------------------------------------
-- Table `xj`.`account`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `xj`.`account` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC))
  ENGINE = InnoDB;

-- -----------------------------------------------------
-- Table `xj`.`account_user`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `xj`.`account_user` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `type` VARCHAR(255) NOT NULL,
  `account_id` INT UNSIGNED NOT NULL,
  `user_id` INT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `account_user_fk_account_idx` (`account_id` ASC),
  INDEX `account_user_fk_user_idx` (`user_id` ASC),
  CONSTRAINT `account_user_fk_account`
  FOREIGN KEY (`account_id`)
  REFERENCES `xj`.`account` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `account_user_fk_user`
  FOREIGN KEY (`user_id`)
  REFERENCES `xj`.`user` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;
