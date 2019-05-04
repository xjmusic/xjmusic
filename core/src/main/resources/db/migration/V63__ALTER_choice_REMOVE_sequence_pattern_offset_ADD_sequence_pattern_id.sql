-- -----------------------------------------------------
-- [#165956363] Choice has `sequence_pattern_id` (NOT sequence pattern offset, which is ambiguous)
-- -----------------------------------------------------
ALTER TABLE `choice` DROP COLUMN `sequence_pattern_offset`;

-- Create new sequence_pattern_id column in choice (default null)
ALTER TABLE `choice`
  ADD COLUMN sequence_pattern_id BIGINT UNSIGNED DEFAULT NULL
  AFTER sequence_id;

-- Create foreign keys for sequence_pattern_id column in voice
ALTER TABLE `choice`
  ADD CONSTRAINT choice_fk_sequence_pattern
FOREIGN KEY (sequence_pattern_id)
REFERENCES sequence_pattern (id)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- Now sequence_id is not required, in fact, it should be null if there's a sequence_pattern_id
ALTER TABLE `choice` MODIFY COLUMN
  `sequence_id` BIGINT UNSIGNED NULL DEFAULT NULL;
