/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */

--
-- Instrument has overall volume parameter #179215413
--

ALTER TABLE xj.instrument
  ADD COLUMN volume real DEFAULT 1.0;
