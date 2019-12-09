-- Rename ChainIdea to ChainPattern
RENAME TABLE `chain_idea` TO `chain_pattern`;

-- Remove idea foreign keys of ChainPattern
ALTER TABLE `chain_pattern`
  DROP FOREIGN KEY `chain_idea_fk_idea`;
DROP INDEX `chain_idea_fk_idea_idx`
ON `chain_pattern`;

-- Rename reference column in ChainPattern
ALTER TABLE `chain_pattern`
  CHANGE `idea_id` `pattern_id` BIGINT UNSIGNED NOT NULL;

-- (wait until after we rename 'idea' to 'pattern' to of new keys

--

-- Rename ChainIdea to PatternMeme
RENAME TABLE `idea_meme` TO `pattern_meme`;

-- Remove idea foreign keys of PatternMeme
ALTER TABLE `pattern_meme`
  DROP FOREIGN KEY `meme_fk_idea`;
DROP INDEX `meme_fk_idea_idx`
ON `pattern_meme`;

-- Rename reference column in PatternMeme
ALTER TABLE `pattern_meme`
  CHANGE `idea_id` `pattern_id` BIGINT UNSIGNED NOT NULL;

-- (wait until after we rename 'idea' to 'pattern' to of new keys

--

-- Remove Idea reference of Phase.
ALTER TABLE `phase`
  DROP FOREIGN KEY `phase_fk_idea`;
DROP INDEX `phase_fk_idea_idx`
ON `phase`;

-- Rename reference column in Phase
ALTER TABLE `phase`
  CHANGE `idea_id` `pattern_id` BIGINT UNSIGNED NOT NULL;

-- (wait until after we rename 'idea' to 'pattern' to of new keys

--

-- Remove Idea reference of Choice.
ALTER TABLE `choice`
  DROP FOREIGN KEY `choice_fk_idea`;
DROP INDEX `choice_fk_idea_idx`
ON `choice`;

-- Rename reference column in Choice
ALTER TABLE `choice`
  CHANGE `idea_id` `pattern_id` BIGINT UNSIGNED NOT NULL;

-- (wait until after we rename 'idea' to 'pattern' to of new keys

--

-- Rename table 'idea' to 'pattern'
RENAME TABLE `idea` TO `pattern`;

-- Rename 'idea_fx_user' to 'pattern_fk_user'
ALTER TABLE `pattern`
  DROP FOREIGN KEY `idea_fk_user`;
DROP INDEX `idea_fk_user`
ON `pattern`;
ALTER TABLE `pattern`
  ADD CONSTRAINT `pattern_fk_user`
FOREIGN KEY (`user_id`)
REFERENCES `user` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- Rename 'idea_fx_library' to 'pattern_fk_library'
ALTER TABLE `pattern`
  DROP FOREIGN KEY `idea_fk_library`;
DROP INDEX `idea_fk_library_idx`
ON `pattern`;
ALTER TABLE `pattern`
  ADD CONSTRAINT `pattern_fk_library`
FOREIGN KEY (`library_id`)
REFERENCES `library` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

--

-- Create new keys referencing the new pattern table

-- Add 'pattern' keys to ChainPattern.
ALTER TABLE `chain_pattern`
  ADD CONSTRAINT `chain_pattern_fk_pattern`
FOREIGN KEY (`pattern_id`)
REFERENCES `pattern` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- Add 'pattern' keys to PatternMeme
ALTER TABLE `pattern_meme`
  ADD CONSTRAINT `pattern_meme_fk_pattern`
FOREIGN KEY (`pattern_id`)
REFERENCES `pattern` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- Add 'pattern' keys to Phase
ALTER TABLE `phase`
  ADD CONSTRAINT `phase_fk_pattern`
FOREIGN KEY (`pattern_id`)
REFERENCES `pattern` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;

-- Add Pattern reference to Choice.
ALTER TABLE `choice`
  ADD CONSTRAINT `choice_fk_pattern`
FOREIGN KEY (`pattern_id`)
REFERENCES `pattern` (`id`)
  ON DELETE NO ACTION
  ON UPDATE NO ACTION;
