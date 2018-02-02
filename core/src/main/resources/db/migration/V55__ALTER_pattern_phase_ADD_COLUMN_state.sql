--
-- [#154887174] PatternErase job erase a Pattern and all its Phases in the background, in order to keep the UI functioning at a reasonable speed.
--
ALTER TABLE `pattern`
  ADD COLUMN `state` VARCHAR(255) NULL;

UPDATE `pattern`
  SET `state` = "Published";

ALTER TABLE `pattern`
  MODIFY COLUMN `state` VARCHAR(255) NOT NULL;

--
-- [#153976888] PhaseErase job erase a Phase and all its Phases in the background, in order to keep the UI functioning at a reasonable speed.
--
ALTER TABLE `phase`
  ADD COLUMN `state` VARCHAR(255) NULL;

UPDATE `phase`
SET `state` = "Published";

ALTER TABLE `phase`
  MODIFY COLUMN `state` VARCHAR(255) NOT NULL;

