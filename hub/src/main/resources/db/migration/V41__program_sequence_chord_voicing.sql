/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */
CREATE TABLE xj.program_sequence_chord_voicing
(
  id                        UUID PRIMARY KEY  DEFAULT uuid_generate_v1mc(),
  program_id                UUID         NOT NULL REFERENCES xj.program (id),
  program_sequence_chord_id UUID         NOT NULL REFERENCES xj.program_sequence_chord (id),
  type                      varchar(255) not null,
  notes                     text         not null,
  created_at                timestamp    NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at                timestamp    NULL DEFAULT CURRENT_TIMESTAMP
);

/* when table is updated, updated_at_now */
CREATE TRIGGER program_sequence_chord_voicing___updated
  BEFORE UPDATE
  ON xj.program_sequence_chord_voicing
  FOR EACH ROW
EXECUTE PROCEDURE xj.updated_at_now();
