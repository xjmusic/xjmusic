-- -----------------------------------------------------
-- [#166743281] Chain handles all of its own binding + config entities
-- -----------------------------------------------------

-- Add content CSV column to Chain
ALTER TABLE `chain`
  ADD COLUMN `content`    json                NOT NULL
    AFTER embed_key;

-- Deprecated: Table for binding Libraries to Chain
ALTER TABLE `chain_library`
  DROP FOREIGN KEY `chain_library_fk_chain`,
  DROP FOREIGN KEY `chain_library_fk_library`,
  DROP INDEX `chain_library_fk_chain_idx`,
  DROP INDEX `chain_library_fk_library_idx`;
DROP TABLE `chain_library`;

-- Deprecated: Table for binding Instruments to Chain
ALTER TABLE `chain_instrument`
  DROP FOREIGN KEY `chain_instrument_fk_chain`,
  DROP FOREIGN KEY `chain_instrument_fk_instrument`,
  DROP INDEX `chain_instrument_fk_chain_idx`,
  DROP INDEX `chain_instrument_fk_instrument_idx`;
DROP TABLE `chain_instrument`;

-- Deprecated: Table for binding Sequences to Chain
ALTER TABLE `chain_sequence`
  DROP FOREIGN KEY `chain_sequence_fk_chain`,
  DROP FOREIGN KEY `chain_sequence_fk_sequence`,
  DROP INDEX `chain_sequence_fk_chain`,
  DROP INDEX `chain_sequence_fk_sequence`;
DROP TABLE `chain_sequence`;

-- Deprecated: Table of Configs for Chain
ALTER TABLE `chain_config`
  DROP FOREIGN KEY `chain_config_fk_chain`,
  DROP INDEX `chain_config_fk_chain_idx`;
DROP TABLE `chain_config`;

