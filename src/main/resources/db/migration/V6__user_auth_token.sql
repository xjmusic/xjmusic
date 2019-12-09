/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */
CREATE TABLE xj.user_auth_token
(
  id           UUID PRIMARY KEY DEFAULT uuid_generate_v1mc(),
  user_auth_id UUID      NOT NULL REFERENCES xj.user_auth (id),
  user_id      UUID      NOT NULL REFERENCES xj.user (id),
  access_token text      NOT NULL,
  created_at   timestamp NULL   DEFAULT CURRENT_TIMESTAMP,
  updated_at   timestamp NULL   DEFAULT CURRENT_TIMESTAMP
);

/* when table is updated, updated_at_now */
CREATE TRIGGER user_auth_token___updated
  BEFORE UPDATE
  ON xj.user_auth_token
  FOR EACH ROW
EXECUTE PROCEDURE xj.updated_at_now();
