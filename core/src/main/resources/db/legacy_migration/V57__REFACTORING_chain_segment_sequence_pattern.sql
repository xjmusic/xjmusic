-- --------------------------------------------------
--
-- [#156410528] Deprecate Link in favor of ChainSegment
--
-- [#155730614] Artist wants Sequence and Phase to be named according to musical norms, in order to make optimal sense of XJ as a musical instrument.
--
-- --------------------------------------------------

--
--
--
--
--
--
--
--
--
--
--
--
--
--
-- delete foreign keys and indexes
ALTER TABLE `chain_pattern`
  DROP FOREIGN KEY `chain_idea_fk_chain`,
  DROP FOREIGN KEY `chain_pattern_fk_pattern`,
  DROP INDEX `chain_idea_fk_chain_idx`,
  DROP INDEX `chain_pattern_fk_pattern`;

ALTER TABLE `choice`
  DROP FOREIGN KEY `choice_fk_pattern`,
  DROP FOREIGN KEY `choice_fk_link`,
  DROP INDEX `choice_fk_link_idx`,
  DROP INDEX `choice_fk_pattern`;

ALTER TABLE `link`
  DROP FOREIGN KEY `link_fk_chain`,
  DROP INDEX `link_fk_chain_idx`;

ALTER TABLE `link_chord`
  DROP FOREIGN KEY `chord_fk_link`,
  DROP INDEX `chord_fk_link_idx`;

ALTER TABLE `link_meme`
  DROP FOREIGN KEY `meme_fk_link`,
  DROP INDEX `meme_fk_link_idx`;

ALTER TABLE `link_message`
  DROP FOREIGN KEY `message_fk_link`,
  DROP INDEX `message_fk_link_idx`;

ALTER TABLE `pattern`
  DROP FOREIGN KEY `pattern_fk_user`,
  DROP FOREIGN KEY `pattern_fk_library`,
  DROP INDEX `pattern_fk_user`,
  DROP INDEX `pattern_fk_library`;

ALTER TABLE `pattern_meme`
  DROP FOREIGN KEY `pattern_meme_fk_pattern`,
  DROP INDEX `pattern_meme_fk_pattern`;

ALTER TABLE `phase`
  DROP FOREIGN KEY `phase_fk_pattern`,
  DROP INDEX `phase_fk_pattern`;

ALTER TABLE `phase_chord`
  DROP FOREIGN KEY `chord_fk_phase`,
  DROP INDEX `chord_fk_phase_idx`;

ALTER TABLE `phase_meme`
  DROP FOREIGN KEY `meme_fk_phase`,
  DROP INDEX `meme_fk_phase_idx`;

ALTER TABLE `voice`
  DROP FOREIGN KEY `voice_fk_pattern`,
  DROP INDEX `voice_fk_pattern`;

ALTER TABLE `phase_event`
  DROP FOREIGN KEY `event_fk_voice`,
  DROP FOREIGN KEY `voice_event_fk_phase`,
  DROP INDEX `event_fk_voice_idx`,
  DROP INDEX `voice_event_fk_phase`;

--
--
--
--
--
--
--
--
--
--
--
--
--
--
-- rename reference columns
ALTER TABLE `chain_pattern`
  CHANGE `pattern_id` `sequence_id` BIGINT(20) UNSIGNED NOT NULL;

ALTER TABLE `choice`
  CHANGE `link_id` `segment_id` BIGINT(20) UNSIGNED NOT NULL;

ALTER TABLE `choice`
  CHANGE `pattern_id` `sequence_id` BIGINT(20) UNSIGNED NOT NULL;

ALTER TABLE `choice`
  CHANGE `phase_offset` `pattern_offset` BIGINT(20) UNSIGNED NOT NULL;

ALTER TABLE `link_chord`
  CHANGE `link_id` `segment_id` BIGINT(20) UNSIGNED NOT NULL;

ALTER TABLE `link_meme`
  CHANGE `link_id` `segment_id` BIGINT(20) UNSIGNED NOT NULL;

ALTER TABLE `link_message`
  CHANGE `link_id` `segment_id` BIGINT(20) UNSIGNED NOT NULL;

ALTER TABLE `pattern_meme`
  CHANGE `pattern_id` `sequence_id` BIGINT(20) UNSIGNED NOT NULL;

ALTER TABLE `phase`
  CHANGE `pattern_id` `sequence_id` BIGINT(20) UNSIGNED NOT NULL;

ALTER TABLE `phase_chord`
  CHANGE `phase_id` `pattern_id` BIGINT(20) UNSIGNED NOT NULL;

ALTER TABLE `phase_meme`
  CHANGE `phase_id` `pattern_id` BIGINT(20) UNSIGNED NOT NULL;

ALTER TABLE `voice`
  CHANGE `pattern_id` `sequence_id` BIGINT(20) UNSIGNED NOT NULL;

ALTER TABLE `phase_event`
  CHANGE `phase_id` `pattern_id` BIGINT(20) UNSIGNED NOT NULL;

--
--
--
--
--
--
--
--
--
--
--
--
--
--
-- rename tables

RENAME TABLE
    `link` TO `segment`;

RENAME TABLE
    `link_chord` TO `segment_chord`;

RENAME TABLE
    `link_meme` TO `segment_meme`;

RENAME TABLE
    `link_message` TO `segment_message`;

RENAME TABLE
    `chain_pattern` TO `chain_sequence`;

RENAME TABLE
    `pattern` TO `sequence`;

RENAME TABLE
    `pattern_meme` TO `sequence_meme`;

RENAME TABLE
    `phase` TO `pattern`;

RENAME TABLE
    `phase_chord` TO `pattern_chord`;

RENAME TABLE
    `phase_meme` TO `pattern_meme`;

RENAME TABLE
    `phase_event` TO `pattern_event`;

--
--
--
--
--
-- recreate foreign keys and indexes

-- chain_sequence > chain
ALTER TABLE `chain_sequence`
  ADD CONSTRAINT `chain_sequence_fk_chain`
FOREIGN KEY (`chain_id`)
REFERENCES `chain` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- chain_sequence > sequence
ALTER TABLE `chain_sequence`
  ADD CONSTRAINT `chain_sequence_fk_sequence`
FOREIGN KEY (`sequence_id`)
REFERENCES `sequence` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- choice > segment
ALTER TABLE `choice`
  ADD CONSTRAINT `choice_fk_segment`
FOREIGN KEY (`segment_id`)
REFERENCES `segment` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- choice > sequence
ALTER TABLE `choice`
  ADD CONSTRAINT `choice_fk_sequence`
FOREIGN KEY (`sequence_id`)
REFERENCES `sequence` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- segment > chord
ALTER TABLE `segment_chord`
  ADD CONSTRAINT `chord_fk_segment`
FOREIGN KEY (`segment_id`)
REFERENCES `segment` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- segment > meme
ALTER TABLE `segment_meme`
  ADD CONSTRAINT `meme_fk_segment`
FOREIGN KEY (`segment_id`)
REFERENCES `segment` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- segment > message
ALTER TABLE `segment_message`
  ADD CONSTRAINT `message_fk_segment`
FOREIGN KEY (`segment_id`)
REFERENCES `segment` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- sequence > user
ALTER TABLE `sequence`
  ADD CONSTRAINT `sequence_fk_user`
FOREIGN KEY (`user_id`)
REFERENCES `user` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- sequence > library
ALTER TABLE `sequence`
  ADD CONSTRAINT `sequence_fk_library`
FOREIGN KEY (`library_id`)
REFERENCES `library` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- sequence_meme > sequence
ALTER TABLE `sequence_meme`
  ADD CONSTRAINT `sequence_meme_fk_sequence`
FOREIGN KEY (`sequence_id`)
REFERENCES `sequence` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- pattern > sequence
ALTER TABLE `pattern`
  ADD CONSTRAINT `pattern_fk_sequence`
FOREIGN KEY (`sequence_id`)
REFERENCES `sequence` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- pattern_chord > pattern
ALTER TABLE `pattern_chord`
  ADD CONSTRAINT `pattern_chord_fk_pattern`
FOREIGN KEY (`pattern_id`)
REFERENCES `pattern` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- pattern_meme > pattern
ALTER TABLE `pattern_meme`
  ADD CONSTRAINT `pattern_meme_fk_pattern`
FOREIGN KEY (`pattern_id`)
REFERENCES `pattern` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- voice > sequence
ALTER TABLE `voice`
  ADD CONSTRAINT `voice_fk_sequence`
FOREIGN KEY (`sequence_id`)
REFERENCES `sequence` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- pattern_event > pattern
ALTER TABLE `pattern_event`
  ADD CONSTRAINT `pattern_event_fk_pattern`
FOREIGN KEY (`pattern_id`)
REFERENCES `pattern` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- pattern_event > voice
ALTER TABLE `pattern_event`
  ADD CONSTRAINT `pattern_event_fk_voice`
FOREIGN KEY (`voice_id`)
REFERENCES `voice` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- segment > chain
ALTER TABLE `segment`
  ADD CONSTRAINT `segment_fk_chain`
FOREIGN KEY (`chain_id`)
REFERENCES `chain` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

