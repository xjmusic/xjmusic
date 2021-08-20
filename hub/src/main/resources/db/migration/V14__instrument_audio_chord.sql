/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */
CREATE TABLE xj.instrument_audio_chord
(
  id                  UUID PRIMARY KEY  DEFAULT uuid_generate_v1mc(),
  instrument_id       UUID         NOT NULL REFERENCES xj.instrument (id),
  instrument_audio_id UUID         NOT NULL REFERENCES xj.instrument_audio (id),
  name                varchar(255) NOT NULL,
  position            float        NOT NULL,
  created_at          timestamp    NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at          timestamp    NULL DEFAULT CURRENT_TIMESTAMP
);

/* when table is updated, updated_at_now */
CREATE TRIGGER instrument_audio_chord___updated
  BEFORE UPDATE
  ON xj.instrument_audio_chord
  FOR EACH ROW
EXECUTE PROCEDURE xj.updated_at_now();
