-- -----------------------------------------------------
-- Table `xj`.`user_role`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `xj`.`user_role` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `type` VARCHAR(255) NOT NULL,
  `user_id` BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC),
  INDEX `user_role_fk_user_idx` (`user_id` ASC),
  CONSTRAINT `user_role_fk_user`
  FOREIGN KEY (`user_id`)
  REFERENCES `xj`.`user` (`id`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
  ENGINE = InnoDB;
