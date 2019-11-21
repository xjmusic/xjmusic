/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */
CREATE TABLE xj.account
(
  id         UUID PRIMARY KEY  DEFAULT uuid_generate_v1mc(),
  name       varchar(255) NOT NULL,
  created_at timestamp    NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp    NULL DEFAULT CURRENT_TIMESTAMP
);

/* when table is updated, updated_at_now */
CREATE TRIGGER account___updated
  BEFORE UPDATE
  ON xj.account
  FOR EACH ROW
EXECUTE PROCEDURE xj.updated_at_now();
