--
--
-- [#157921430] Artist wants to define custom Sequence-Pattern mapping,
-- in which patterns are repeated and/or alternated between probabilistically
-- during the choice of any given main sequence.
--
--

-- -----------------------------------------------------
-- Create table `sequence_pattern` including `offset` column
-- -----------------------------------------------------
CREATE TABLE `sequence_pattern` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `sequence_id` bigint(20) unsigned NOT NULL,
  `pattern_id` bigint(20) unsigned NOT NULL,
  `offset` bigint(20) unsigned NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `sequence_pattern_fk_sequence` (`sequence_id`),
  CONSTRAINT `sequence_pattern_fk_sequence` FOREIGN KEY (`sequence_id`) REFERENCES `sequence` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  KEY `sequence_pattern_fk_pattern` (`pattern_id`),
  CONSTRAINT `sequence_pattern_fk_pattern` FOREIGN KEY (`pattern_id`) REFERENCES `pattern` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=latin1;

-- -----------------------------------------------------
-- Create `sequence_pattern` records based on original pattern offsets
-- -----------------------------------------------------
INSERT INTO sequence_pattern (sequence_id, pattern_id, offset)
SELECT DISTINCT sequence_id, id, offset FROM pattern;

-- -----------------------------------------------------
-- Deprecate `offset` column in `pattern`—
-- -----------------------------------------------------
ALTER TABLE `pattern` DROP COLUMN `offset`;

-- -----------------------------------------------------
-- Refactor choice table `pattern_offset` column to `sequence_pattern_offset`
-- -----------------------------------------------------
ALTER TABLE `choice`
  CHANGE `pattern_offset` `sequence_pattern_offset` BIGINT(20) UNSIGNED NOT NULL;
