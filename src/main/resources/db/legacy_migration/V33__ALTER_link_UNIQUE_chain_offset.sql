-- -----------------------------------------------------
-- Table `link`
-- -----------------------------------------------------
ALTER TABLE `link` ADD UNIQUE `unique_chain_offset_index`(`chain_id`, `offset`);
