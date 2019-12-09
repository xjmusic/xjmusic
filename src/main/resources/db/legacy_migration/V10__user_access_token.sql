-- -----------------------------------------------------
-- Table `user_access_token`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `user_access_token` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_auth_id` BIGINT UNSIGNED NOT NULL,
  `user_id` BIGINT UNSIGNED NOT NULL,
  `access_token` TEXT(2048) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `user_access_token_fk_user_idx` (`user_id` ASC),
  INDEX `user_access_token_fk_user_auth_idx` (`user_auth_id` ASC),
  CONSTRAINT `user_access_token_fk_user`
  FOREIGN KEY (`user_id`)
  REFERENCES `user` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `user_access_token_fk_user_auth`
  FOREIGN KEY (`user_auth_id`)
  REFERENCES `user_auth` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;
