/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */
CREATE TABLE xj.instrument_audio
(
  id            UUID PRIMARY KEY   DEFAULT uuid_generate_v1mc(),
  instrument_id UUID          NOT NULL REFERENCES xj.instrument (id),
  name          varchar(255)  NOT NULL,
  waveform_key  varchar(2047) NOT NULL,
  start         real          not NULL,
  length        real          not NULL,
  tempo         real          NOT NULL,
  pitch         real          NOT NULL,
  density       real          not NULL,
  created_at    timestamp     NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    timestamp     NULL DEFAULT CURRENT_TIMESTAMP
);

/* when table is updated, updated_at_now */
CREATE TRIGGER instrument_audio___updated
  BEFORE UPDATE
  ON xj.instrument_audio
  FOR EACH ROW
EXECUTE PROCEDURE xj.updated_at_now();
