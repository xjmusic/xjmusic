/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */
CREATE TABLE xj.program_meme
(
  id         UUID PRIMARY KEY  DEFAULT uuid_generate_v1mc(),
  name       varchar(255) NOT NULL,
  program_id UUID         NOT NULL REFERENCES xj.program (id),
  created_at timestamp    NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp    NULL DEFAULT CURRENT_TIMESTAMP
);

/* when table is updated, updated_at_now */
CREATE TRIGGER program_meme___updated
  BEFORE UPDATE
  ON xj.program_meme
  FOR EACH ROW
EXECUTE PROCEDURE xj.updated_at_now();
