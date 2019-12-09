/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */
CREATE TABLE xj.library
(
  id         UUID PRIMARY KEY  DEFAULT uuid_generate_v1mc(),
  name       varchar(255) NOT NULL,
  account_id UUID         NOT NULL REFERENCES xj.account (id),
  created_at timestamp    NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp    NULL DEFAULT CURRENT_TIMESTAMP
);

/* when table is updated, updated_at_now */
CREATE TRIGGER library___updated
  BEFORE UPDATE
  ON xj.library
  FOR EACH ROW
EXECUTE PROCEDURE xj.updated_at_now();
