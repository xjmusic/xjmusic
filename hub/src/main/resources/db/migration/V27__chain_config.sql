/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */
CREATE TABLE xj.chain_config
(
  id         UUID PRIMARY KEY  DEFAULT uuid_generate_v1mc(),
  chain_id   UUID         NOT NULL REFERENCES xj.chain (id),
  type       varchar(255) not null,
  value      text         not null,
  created_at timestamp    NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp    NULL DEFAULT CURRENT_TIMESTAMP
);

/* when table is updated, updated_at_now */
CREATE TRIGGER chain_config___updated
  BEFORE UPDATE
  ON xj.chain_config
  FOR EACH ROW
EXECUTE PROCEDURE xj.updated_at_now();
