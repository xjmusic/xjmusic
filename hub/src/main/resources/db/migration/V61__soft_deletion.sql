/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */

--
-- Don't actually delete Account/User/Library/Template/Program/Instrument record; only tombstone it #180141227
--

ALTER TABLE xj.program
  ADD COLUMN is_deleted BOOLEAN DEFAULT false;

ALTER TABLE xj.instrument
  ADD COLUMN is_deleted BOOLEAN DEFAULT false;

ALTER TABLE xj.library
  ADD COLUMN is_deleted BOOLEAN DEFAULT false;

ALTER TABLE xj.template
  ADD COLUMN is_deleted BOOLEAN DEFAULT false;
