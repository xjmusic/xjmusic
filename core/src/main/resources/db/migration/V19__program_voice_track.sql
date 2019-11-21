/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */
CREATE TABLE xj.program_voice_track
(
  id               UUID PRIMARY KEY  DEFAULT uuid_generate_v1mc(),
  program_id       UUID         NOT NULL REFERENCES xj.program (id),
  program_voice_id UUID         NOT NULL REFERENCES xj.program_voice (id),
  name             varchar(255) NOT NULL,
  created_at       timestamp    NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at       timestamp    NULL DEFAULT CURRENT_TIMESTAMP
);

/* when table is updated, updated_at_now */
CREATE TRIGGER program_voice_track___updated
  BEFORE UPDATE
  ON xj.program_voice_track
  FOR EACH ROW
EXECUTE PROCEDURE xj.updated_at_now();
