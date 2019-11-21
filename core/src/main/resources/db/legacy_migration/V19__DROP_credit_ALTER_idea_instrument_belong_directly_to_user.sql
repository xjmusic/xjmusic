-- [#70] Developer wants to deprecate `credit` for simplicity sake, and let objects belong directly to user.

-- -----------------------------------------------------
-- Table `idea`
-- -----------------------------------------------------

-- Remove Credit reference of Idea.
ALTER TABLE `idea` DROP FOREIGN KEY `idea_fk_credit`;
DROP INDEX `idea_fk_credit_idx` ON `idea`;
ALTER TABLE `idea` DROP COLUMN `credit_id`;

-- Add User reference to Idea.
ALTER TABLE `idea`
  ADD COLUMN `user_id` BIGINT UNSIGNED NOT NULL
  AFTER `id`;
ALTER TABLE `idea`
  ADD CONSTRAINT `idea_fk_user`
FOREIGN KEY (`user_id`)
REFERENCES `user` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- -----------------------------------------------------
-- Table `instrument`
-- -----------------------------------------------------

-- Remove Credit reference of Instrument.
ALTER TABLE `instrument` DROP FOREIGN KEY `instrument_fk_credit`;
DROP INDEX `instrument_fk_credit_idx` ON `instrument`;
ALTER TABLE `instrument` DROP COLUMN `credit_id`;

-- Add User reference to Instrument.
ALTER TABLE `instrument`
  ADD COLUMN `user_id` BIGINT UNSIGNED NOT NULL
  AFTER `id`;
ALTER TABLE `instrument`
  ADD CONSTRAINT `instrument_fk_user`
FOREIGN KEY (`user_id`)
REFERENCES `user` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

--
-- DROP Table `credit`
--

DROP TABLE `credit`;
