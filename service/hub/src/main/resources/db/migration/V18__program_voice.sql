/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */
CREATE TABLE xj.program_voice
(
  id         UUID PRIMARY KEY  DEFAULT uuid_generate_v1mc(),
  program_id UUID         NOT NULL REFERENCES xj.program (id),
  type       varchar(255) NOT NULL,
  name       varchar(255) NOT NULL,
  created_at timestamp    NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp    NULL DEFAULT CURRENT_TIMESTAMP
);

/* when table is updated, updated_at_now */
CREATE TRIGGER program_voice___updated
  BEFORE UPDATE
  ON xj.program_voice
  FOR EACH ROW
EXECUTE PROCEDURE xj.updated_at_now();
