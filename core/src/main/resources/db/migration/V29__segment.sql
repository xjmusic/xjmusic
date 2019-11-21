/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */
CREATE TABLE xj.segment
(
  chain_id     UUID         NOT NULL REFERENCES xj.chain (id),
  id           UUID PRIMARY KEY  DEFAULT uuid_generate_v1mc(),
  state        varchar(255) not null,
  begin_at     timestamp    not null,
  "offset"     bigint       not null,
  type         varchar(255)      default null,
  end_at       timestamp         DEFAULT NULL,
  total        smallint          DEFAULT NULL,
  density      real              DEFAULT NULL,
  tempo        real              DEFAULT NULL,
  key          varchar(255)      DEFAULT NULL,
  waveform_key varchar(255)      DEFAULT NULL,
  created_at   timestamp    NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   timestamp    NULL DEFAULT CURRENT_TIMESTAMP
);

/* when table is updated, updated_at_now */
CREATE TRIGGER segment___updated
  BEFORE UPDATE
  ON xj.segment
  FOR EACH ROW
EXECUTE PROCEDURE xj.updated_at_now();
