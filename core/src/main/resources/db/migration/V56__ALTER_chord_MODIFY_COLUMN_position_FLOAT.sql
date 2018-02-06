--
-- [#154951783] Artist wants Chord position to have resolution finer than 1 beat, in order to accurately portray styles of music.
--

-- -----------------------------------------------------
-- Table `audio_chord`
-- -----------------------------------------------------
ALTER TABLE `audio_chord`
  MODIFY COLUMN `position` FLOAT NOT NULL;

-- -----------------------------------------------------
-- Table `link_chord`
-- -----------------------------------------------------
ALTER TABLE `link_chord`
  MODIFY COLUMN `position` FLOAT NOT NULL;

-- -----------------------------------------------------
-- Table `phase_chord`
-- -----------------------------------------------------
ALTER TABLE `phase_chord`
  MODIFY COLUMN `position` FLOAT NOT NULL;
