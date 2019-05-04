-- -----------------------------------------------------
-- [#166109389] Segments have no child SQL entities, only basis JSON
-- -----------------------------------------------------

ALTER TABLE `arrangement`
  DROP FOREIGN KEY `arrangement_fk_choice`,
  DROP FOREIGN KEY `arrangement_fk_instrument`,
  DROP FOREIGN KEY `arrangement_fk_voice`,
  DROP INDEX `arrangement_fk_choice_idx`,
  DROP INDEX `arrangement_fk_instrument_idx`,
  DROP INDEX `arrangement_fk_voice_idx`;
DROP TABLE `arrangement`;

ALTER TABLE `choice`
  DROP FOREIGN KEY `choice_fk_segment`,
  DROP FOREIGN KEY `choice_fk_sequence`,
  DROP FOREIGN KEY `choice_fk_sequence_pattern`,
  DROP INDEX `choice_fk_segment`,
  DROP INDEX `choice_fk_sequence`,
  DROP INDEX `choice_fk_sequence_pattern`;
DROP TABLE `choice`;

ALTER TABLE `segment_chord`
  DROP FOREIGN KEY `chord_fk_segment`,
  DROP INDEX `chord_fk_segment`;
DROP TABLE `segment_chord`;

ALTER TABLE `segment_meme`
  DROP FOREIGN KEY `meme_fk_segment`,
  DROP INDEX `meme_fk_segment`;
DROP TABLE `segment_meme`;
