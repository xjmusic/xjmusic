/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */
CREATE TABLE xj.account_user
(
  id         UUID PRIMARY KEY DEFAULT uuid_generate_v1mc(),
  user_id    UUID      NOT NULL REFERENCES xj.user (id),
  account_id UUID      NOT NULL REFERENCES xj.account (id),
  created_at timestamp NULL   DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp NULL   DEFAULT CURRENT_TIMESTAMP
);

/* when table is updated, updated_at_now */
CREATE TRIGGER account_user___updated
  BEFORE UPDATE
  ON xj.account_user
  FOR EACH ROW
EXECUTE PROCEDURE xj.updated_at_now();
