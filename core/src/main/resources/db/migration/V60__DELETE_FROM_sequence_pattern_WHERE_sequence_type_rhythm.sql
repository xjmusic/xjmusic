--
--
-- [#161076729] Artist wants rhythm patterns to require no sequence-pattern bindings, to keep things simple
--
--

-- -----------------------------------------------------
-- Delete sequence pattern records for sequences of type rhythm
-- -----------------------------------------------------
DELETE FROM sequence_pattern
  WHERE sequence_pattern.sequence_id IN
    (SELECT id FROM sequence WHERE sequence.type LIKE "Rhythm");
