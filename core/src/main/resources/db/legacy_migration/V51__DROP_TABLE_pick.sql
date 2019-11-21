--
-- [#154014731] Ops wants platform to use SQL only for business state persistence, in order to improve performance.
--

-- Remove foreign keys for phase_id column in voice
-- Remove phase_id column in voice
ALTER TABLE `pick`
  DROP FOREIGN KEY `pick_fk_arrangement`,
  DROP FOREIGN KEY `pick_fk_audio`,
  DROP INDEX `pick_fk_arrangement_idx`,
  DROP INDEX `pick_fk_audio_idx`;

DROP TABLE `pick`;
