/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */
CREATE TABLE xj.segment_choice
(
  id                          UUID PRIMARY KEY  DEFAULT uuid_generate_v1mc(),
  segment_id                  UUID         NOT NULL REFERENCES xj.segment (id),
  program_id                  UUID         NOT NULL REFERENCES xj.program (id),
  program_sequence_binding_id UUID              DEFAULT NULL REFERENCES xj.program_sequence_binding (id),
  type                        varchar(255) not null,
  transpose                   smallint     not null,
  created_at                  timestamp    NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at                  timestamp    NULL DEFAULT CURRENT_TIMESTAMP
);

/* when table is updated, updated_at_now */
CREATE TRIGGER segment_choice___updated
  BEFORE UPDATE
  ON xj.segment_choice
  FOR EACH ROW
EXECUTE PROCEDURE xj.updated_at_now();
