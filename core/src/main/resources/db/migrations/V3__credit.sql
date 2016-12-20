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
