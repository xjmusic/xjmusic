/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */

--
-- Instruments have InstrumentMode #181134085
--

CREATE TYPE instrument_mode AS ENUM (
  'Events',
  'ChordPart',
  'MainPart'
  );

ALTER TABLE xj.instrument
  ADD COLUMN mode instrument_mode DEFAULT 'Events';

