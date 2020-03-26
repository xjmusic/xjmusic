/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */
CREATE TABLE xj.user_auth
(
  id                     UUID PRIMARY KEY   DEFAULT uuid_generate_v1mc(),
  type                   varchar(255)  NOT NULL,
  external_access_token  varchar(1023) NOT NULL,
  external_refresh_token varchar(1023)      DEFAULT NULL,
  external_account       varchar(1023) NOT NULL,
  user_id                UUID          NOT NULL REFERENCES xj.user (id),
  created_at             timestamp     NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at             timestamp     NULL DEFAULT CURRENT_TIMESTAMP
);

/* when table is updated, updated_at_now */
CREATE TRIGGER user_auth____updated
  BEFORE UPDATE
  ON xj.user_auth
  FOR EACH ROW
EXECUTE PROCEDURE xj.updated_at_now();
