-- -----------------------------------------------------
-- Table `user_auth`
-- -----------------------------------------------------
ALTER TABLE `user_auth` CHANGE `access_token` `external_access_token` VARCHAR(1023) NOT NULL;
ALTER TABLE `user_auth` CHANGE `refresh_token` `external_refresh_token` VARCHAR(1023); # null is okay here
