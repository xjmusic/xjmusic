/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */
CREATE TABLE xj.chain
(
  id         UUID PRIMARY KEY  DEFAULT uuid_generate_v1mc(),
  account_id UUID         NOT NULL REFERENCES xj.account (id),
  name       varchar(255) not null,
  state      varchar(255) not null,
  type       varchar(255) not null,
  start_at   timestamp    not null,
  stop_at    timestamp         DEFAULT NULL,
  embed_key  varchar(1023)     DEFAULT NULL,
  created_at timestamp    NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp    NULL DEFAULT CURRENT_TIMESTAMP
);

/* when table is updated, updated_at_now */
CREATE TRIGGER chain___updated
  BEFORE UPDATE
  ON xj.chain
  FOR EACH ROW
EXECUTE PROCEDURE xj.updated_at_now();
