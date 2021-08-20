/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */

--
-- Templates have type, Preview or Production #178457569
--

ALTER TABLE xj.template
  ADD COLUMN type character varying(255) DEFAULT 'Preview';
