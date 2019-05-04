--
-- [#166194554] Segment Content deprecates concept of "basis" entirely
--

ALTER TABLE segment
  DROP COLUMN `basis`,
  ADD COLUMN `content` JSON NOT NULL;
