-- -----------------------------------------------------
-- Table `user_auth`
-- -----------------------------------------------------
ALTER TABLE `user_auth` CHANGE `account` `external_account` VARCHAR(1023) NOT NULL;
