/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */
CREATE TABLE xj.segment_meme
(
  id         UUID PRIMARY KEY  DEFAULT uuid_generate_v1mc(),
  segment_id UUID         NOT NULL REFERENCES xj.segment (id),
  name       varchar(255) NOT NULL,
  created_at timestamp    NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at timestamp    NULL DEFAULT CURRENT_TIMESTAMP
);

/* when table is updated, updated_at_now */
CREATE TRIGGER segment_meme___updated
  BEFORE UPDATE
  ON xj.segment_meme
  FOR EACH ROW
EXECUTE PROCEDURE xj.updated_at_now();
