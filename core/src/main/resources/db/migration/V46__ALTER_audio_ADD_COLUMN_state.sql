--
-- [#294] Eraseworker finds Links and Audio in deleted state and actually deletes the records, child entities and S3 objects
--
ALTER TABLE `audio`
  ADD COLUMN `state` VARCHAR(255) NULL;

UPDATE `audio`
  SET `state` = "Published";

ALTER TABLE `audio`
  MODIFY COLUMN `state` VARCHAR(255) NOT NULL;
