/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */

--
-- Templates: enhanced preview chain creation for artists in Lab UI #178457569
--

ALTER TABLE xj.template_playback
  DROP COLUMN state;
