-- -----------------------------------------------------
-- [#166708597] Instrument model handles all of its own entities
-- -----------------------------------------------------

ALTER TABLE `instrument`
  ADD COLUMN `content` json NOT NULL;

-- ---------------------------------------------------------------------
-- [#166708597] Instrument doesn't actually have density property; it's computed by averaging the density of all its sub entities
-- ---------------------------------------------------------------------
ALTER TABLE `instrument`
  DROP COLUMN `density`;

-- ---------------------------------------------------------------------
-- [#166690830] Program doesn't actually have density property; it's computed by averaging the density of all its sub entities
-- ---------------------------------------------------------------------
ALTER TABLE `program`
  DROP COLUMN `density`;

--
-- Legacy child entities need their extra keys dropped, so tests on new entities can run
--
ALTER TABLE `audio`
  DROP INDEX `audio_fk_instrument_idx`,
  DROP FOREIGN KEY `audio_fk_instrument`;
