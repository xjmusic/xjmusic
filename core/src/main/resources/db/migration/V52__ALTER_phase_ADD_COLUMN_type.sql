-- -----------------------------------------------------
-- Add `type` column to `phase` table
-- for now, optional (DEFAULT NULL) but we will then
-- set its values based on a join with its pattern type
-- and finally set the `type` column to required (NOT NULL)
-- -----------------------------------------------------

ALTER TABLE `phase`
  ADD COLUMN `type` VARCHAR(255) DEFAULT NULL
  AFTER `id`;

UPDATE `phase`
  JOIN `pattern` ON `pattern`.`id` = `phase`.`pattern_id`
SET `phase`.`type` = `pattern`.`type`
WHERE `pattern`.`type` IN ('Macro','Main');

UPDATE `phase`
  JOIN `pattern` ON `pattern`.`id` = `phase`.`pattern_id`
SET `phase`.`type` = 'Loop'
WHERE `pattern`.`type` IN ('Rhythm','Detail');

ALTER TABLE `phase`
  MODIFY COLUMN `type` VARCHAR(255) NOT NULL;
