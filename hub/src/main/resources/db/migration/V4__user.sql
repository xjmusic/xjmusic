/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */
CREATE TABLE xj.user
(
  id         UUID PRIMARY KEY  DEFAULT uuid_generate_v1mc(),
  name       varchar(255) NOT NULL,
  email      varchar(1023)     DEFAULT NULL,
  avatar_url varchar(1023)     DEFAULT NULL,
  created_at timestamp    NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp    NULL DEFAULT CURRENT_TIMESTAMP
);

/* when table is updated, updated_at_now */
CREATE TRIGGER user___updated
  BEFORE UPDATE
  ON xj.user
  FOR EACH ROW
EXECUTE PROCEDURE xj.updated_at_now();
