-- -----------------------------------------------------
-- [#257] SQL usage during craft is unnecessarily heavy; refactor such that Morph and Point are not persisted in SQL, but simply virtual objects used during the craft process; MIGRATION pick can exist without morph and point
-- -----------------------------------------------------

DROP TABLE `point`;

ALTER TABLE `pick`
  DROP FOREIGN KEY `pick_fk_morph`,
  DROP INDEX `pick_fk_morph_idx`,
  DROP COLUMN `morph_id`;

DROP TABLE `morph`;
