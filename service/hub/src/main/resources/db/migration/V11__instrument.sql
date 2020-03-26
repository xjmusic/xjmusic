/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */
CREATE TABLE xj.instrument
(
  id         UUID PRIMARY KEY  DEFAULT uuid_generate_v1mc(),
  user_id    UUID         NOT NULL REFERENCES xj.user (id),
  library_id UUID         NOT NULL REFERENCES xj.library (id),
  type       varchar(255) NOT NULL,
  state      varchar(255) NOT NULL,
  name       varchar(255) NOT NULL,
  density    real         NOT NULL,
  created_at timestamp    NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp    NULL DEFAULT CURRENT_TIMESTAMP
);

/* when table is updated, updated_at_now */
CREATE TRIGGER instrument___updated
  BEFORE UPDATE
  ON xj.instrument
  FOR EACH ROW
EXECUTE PROCEDURE xj.updated_at_now();
