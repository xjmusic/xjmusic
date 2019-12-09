/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */
CREATE TABLE xj.program_sequence
(
  id         UUID PRIMARY KEY  DEFAULT uuid_generate_v1mc(),
  program_id UUID         NOT NULL REFERENCES xj.program (id),
  name       varchar(255) not null,
  key        varchar(255) not null,
  density    real         not null,
  total      smallint     not null,
  tempo      real         not null,
  created_at timestamp    NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp    NULL DEFAULT CURRENT_TIMESTAMP
);

/* when table is updated, updated_at_now */
CREATE TRIGGER program_sequence___updated
  BEFORE UPDATE
  ON xj.program_sequence
  FOR EACH ROW
EXECUTE PROCEDURE xj.updated_at_now();
