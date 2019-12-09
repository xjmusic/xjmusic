--
-- [#336] Chain can be "Public" in order to be played by unauthenticated client apps
--
-- A Chain containing an `embed_key` can be retrieved without authentication.
--
ALTER TABLE `chain`
  ADD COLUMN `embed_key` VARCHAR(255) NULL;

ALTER TABLE `chain`
  ADD CONSTRAINT unique_embed_key UNIQUE (embed_key);
