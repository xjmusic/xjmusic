-- -----------------------------------------------------
-- [#166109389] Segments have no child SQL entities, only basis JSON
-- -----------------------------------------------------

ALTER TABLE `segment_message`
  DROP FOREIGN KEY `message_fk_segment`,
  DROP INDEX `message_fk_segment`;
DROP TABLE `segment_message`;
