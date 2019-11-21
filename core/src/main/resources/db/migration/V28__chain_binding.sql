/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */
CREATE TABLE xj.chain_binding
(
  id         UUID PRIMARY KEY  DEFAULT uuid_generate_v1mc(),
  chain_id   UUID         NOT NULL REFERENCES xj.chain (id),
  target_id  UUID         NOT NULL, -- can reference any class
  type       varchar(255) not null,
  created_at timestamp    NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp    NULL DEFAULT CURRENT_TIMESTAMP
);

/* when table is updated, updated_at_now */
CREATE TRIGGER chain_binding___updated
  BEFORE UPDATE
  ON xj.chain_binding
  FOR EACH ROW
EXECUTE PROCEDURE xj.updated_at_now();
