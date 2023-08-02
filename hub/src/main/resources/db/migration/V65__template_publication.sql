/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */

--
-- Hub can publish content for production fabrication #180805580
--

CREATE TABLE xj.template_publication
(
  id          uuid           DEFAULT xj.uuid_generate_v1mc() NOT NULL PRIMARY KEY,
  template_id uuid      NOT NULL,
  user_id     uuid      NOT NULL,
  created_at  timestamp NULL DEFAULT CURRENT_TIMESTAMP
);
