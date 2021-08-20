/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */

--
-- Templates: enhanced preview chain creation for artists in Lab UI #178457569
--

CREATE TABLE xj.template
(
  id         uuid                   DEFAULT xj.uuid_generate_v1mc() NOT NULL PRIMARY KEY,
  account_id uuid                                                   NOT NULL,
  name       character varying(255)                                 NOT NULL,
  config     text                   DEFAULT '',
  embed_key  character varying(255) DEFAULT ''
);

CREATE TABLE xj.template_binding
(
  id          uuid DEFAULT xj.uuid_generate_v1mc() NOT NULL PRIMARY KEY,
  type        character varying(255)               NOT NULL,
  template_id uuid                                 NOT NULL,
  target_id   uuid                                 NOT NULL
);

CREATE TABLE xj.template_playback
(
  id          uuid DEFAULT xj.uuid_generate_v1mc() NOT NULL PRIMARY KEY,
  template_id uuid                                 NOT NULL,
  user_id     uuid                                 NOT NULL,
  state       character varying(255)               NOT NULL
);
