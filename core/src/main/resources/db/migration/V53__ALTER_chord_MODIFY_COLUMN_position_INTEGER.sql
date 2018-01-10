--
-- [#154111303] Architect wants to force unique integer for chord position in phase or audio, in order to segment any phase or audio into discreet blocks for analysis.
--

-- -----------------------------------------------------
-- Table `audio_chord`
-- -----------------------------------------------------
ALTER TABLE `audio_chord`
  MODIFY COLUMN `position` INT NOT NULL;

-- -----------------------------------------------------
-- Table `link_chord`
-- -----------------------------------------------------
ALTER TABLE `link_chord`
  MODIFY COLUMN `position` INT NOT NULL;

-- -----------------------------------------------------
-- Table `phase_chord`
-- -----------------------------------------------------
ALTER TABLE `phase_chord`
  MODIFY COLUMN `position` INT NOT NULL;
