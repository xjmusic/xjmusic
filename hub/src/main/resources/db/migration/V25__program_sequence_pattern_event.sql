/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */
CREATE TABLE xj.program_sequence_pattern_event
(
  id                          UUID PRIMARY KEY  DEFAULT uuid_generate_v1mc(),
  program_id                  UUID         NOT NULL REFERENCES xj.program (id),
  program_sequence_pattern_id UUID         NOT NULL REFERENCES xj.program_sequence_pattern (id),
  program_voice_track_id      UUID         NOT NULL REFERENCES xj.program_voice_track (id),
  velocity                    real         NOT NULL,
  position                    float        NOT NULL,
  duration                    real         NOT NULL,
  note                        varchar(255) NOT NULL,
  created_at                  timestamp    NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at                  timestamp    NULL DEFAULT CURRENT_TIMESTAMP
);

/* when table is updated, updated_at_now */
CREATE TRIGGER program_sequence_pattern_event___updated
  BEFORE UPDATE
  ON xj.program_sequence_pattern_event
  FOR EACH ROW
EXECUTE PROCEDURE xj.updated_at_now();
