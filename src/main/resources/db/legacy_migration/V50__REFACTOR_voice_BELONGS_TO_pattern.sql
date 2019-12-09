-- -----------------------------------------------------
-- Migrate voice (Part 1 of 2)
-- -----------------------------------------------------

-- Create new pattern_id column in voice (default null)
ALTER TABLE voice
  ADD COLUMN pattern_id BIGINT UNSIGNED DEFAULT NULL
  AFTER id;

-- Create foreign keys for pattern_id column in voice
ALTER TABLE voice
  ADD CONSTRAINT voice_fk_pattern
FOREIGN KEY (pattern_id)
REFERENCES pattern (id)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- Update pattern_id column for all existing voices using
-- a join for the pattern of the phase they belong to
UPDATE voice
  JOIN phase ON voice.phase_id = phase.id
SET voice.pattern_id = phase.pattern_id;

-- Update pattern_id column in voice (not null)
ALTER TABLE voice
  MODIFY COLUMN
  pattern_id BIGINT UNSIGNED NOT NULL;

-- -----------------------------------------------------
-- Migrate voice_event
-- -----------------------------------------------------

-- Create new phase_id column in voice_event (default null)
ALTER TABLE voice_event
  ADD COLUMN phase_id BIGINT UNSIGNED DEFAULT NULL
  AFTER id;

-- Create foreign keys for phase_id column in voice_event
ALTER TABLE voice_event
  ADD CONSTRAINT voice_event_fk_phase
FOREIGN KEY (phase_id)
REFERENCES phase (id)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- update phase_id column for all existing voice_events using
-- a join for the phase of the voice they belong to
UPDATE voice_event
  JOIN voice ON voice_event.voice_id = voice.id
SET voice_event.phase_id = voice.phase_id;

--  update phase_id column in voice_event (not null)
ALTER TABLE voice_event
  MODIFY COLUMN
  phase_id BIGINT UNSIGNED NOT NULL;

-- -----------------------------------------------------
-- Migrate voice (Part 2 of 2)
-- -----------------------------------------------------

-- Update phase_id column in voice (default null)
ALTER TABLE voice
  MODIFY COLUMN
  phase_id BIGINT UNSIGNED DEFAULT NULL;

-- Update phase_id=null for all rows in voice
UPDATE voice
SET phase_id = NULL;

-- Remove foreign keys for phase_id column in voice
-- Remove phase_id column in voice
ALTER TABLE `voice`
  DROP FOREIGN KEY `voice_fk_phase`,
  DROP INDEX `voice_fk_phase_idx`,
  DROP COLUMN `phase_id`;
