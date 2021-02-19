/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */
CREATE TABLE xj.work
(
  id         UUID PRIMARY KEY  DEFAULT uuid_generate_v1mc(),
  type       varchar(255) NOT NULL,
  target_id  UUID              default null,
  created_at timestamp    NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp    NULL DEFAULT CURRENT_TIMESTAMP
);

/* when table is updated, updated_at_now */
CREATE TRIGGER work___updated
  BEFORE UPDATE
  ON xj.work
  FOR EACH ROW
EXECUTE PROCEDURE xj.updated_at_now();
