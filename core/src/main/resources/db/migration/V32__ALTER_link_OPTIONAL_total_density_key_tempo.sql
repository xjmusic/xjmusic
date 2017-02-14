-- -----------------------------------------------------
-- Table `link`
-- -----------------------------------------------------
ALTER TABLE `link` MODIFY COLUMN
  `total` bigint(20) unsigned null default null;

ALTER TABLE `link` MODIFY COLUMN
  `density` float unsigned null default null;

ALTER TABLE `link` MODIFY COLUMN
  `key` varchar(255) null default null;

ALTER TABLE `link` MODIFY COLUMN
  `tempo` float unsigned null default null;

