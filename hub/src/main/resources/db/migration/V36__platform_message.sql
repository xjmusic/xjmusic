/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */
CREATE TABLE xj.platform_message
(
  id         UUID PRIMARY KEY  DEFAULT uuid_generate_v1mc(),
  type       varchar(255) not null,
  body       text         not null,
  created_at timestamp    NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp    NULL DEFAULT CURRENT_TIMESTAMP
);

/* when table is updated, updated_at_now */
CREATE TRIGGER platform_message___updated
  BEFORE UPDATE
  ON xj.platform_message
  FOR EACH ROW
EXECUTE PROCEDURE xj.updated_at_now();
