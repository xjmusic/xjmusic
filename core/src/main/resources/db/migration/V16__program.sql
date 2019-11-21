/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */
CREATE TABLE xj.program
(
  id         UUID PRIMARY KEY  DEFAULT uuid_generate_v1mc(),
  user_id    UUID         NOT NULL REFERENCES xj.user (id),
  library_id UUID         NOT NULL REFERENCES xj.library (id),
  state      varchar(255) not null,
  key        varchar(255) not null,
  tempo      real         not null,
  type       varchar(255) not null,
  name       varchar(255) not null,
  density    real         NOT NULL,
  created_at timestamp    NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp    NULL DEFAULT CURRENT_TIMESTAMP
);

/* when table is updated, updated_at_now */
CREATE TRIGGER program___updated
  BEFORE UPDATE
  ON xj.program
  FOR EACH ROW
EXECUTE PROCEDURE xj.updated_at_now();
