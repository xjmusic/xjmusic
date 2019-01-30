--
-- [#163158036] memes bound to pattern order (not pattern)
--
CREATE TABLE `sequence_pattern_meme` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `sequence_pattern_id` bigint(20) unsigned NOT NULL,
  `name` varchar(255) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `sequence_pattern_meme_fk_pattern` (`sequence_pattern_id`),
  CONSTRAINT `sequence_pattern_meme_fk_pattern` FOREIGN KEY (`sequence_pattern_id`) REFERENCES `sequence_pattern` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=96 DEFAULT CHARSET=latin1;

--
-- Insert records transmogrified from legacy records
--
INSERT INTO `sequence_pattern_meme` (sequence_pattern_id, name)
  SELECT sequence_pattern.id, pattern_meme.name
  FROM sequence_pattern
  JOIN pattern_meme
  ON pattern_meme.pattern_id = sequence_pattern.pattern_id;

--
-- Destroy legacy table`
--

DROP TABLE IF EXISTS `pattern_meme`;
