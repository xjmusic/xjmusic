/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */
CREATE TABLE xj.user_role
(
  id         UUID PRIMARY KEY  DEFAULT uuid_generate_v1mc(),
  type       varchar(255) NOT NULL,
  user_id    UUID         NOT NULL REFERENCES xj.user (id),
  created_at timestamp    NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp    NULL DEFAULT CURRENT_TIMESTAMP
);

/* when table is updated, updated_at_now */
CREATE TRIGGER user_role___updated
  BEFORE UPDATE
  ON xj.user_role
  FOR EACH ROW
EXECUTE PROCEDURE xj.updated_at_now();
