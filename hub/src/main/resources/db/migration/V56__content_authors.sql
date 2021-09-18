/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */

--
-- Instruments and Programs have author history #166724453
--

CREATE TABLE xj.program_authorship
(
  id          UUID PRIMARY KEY DEFAULT uuid_generate_v1mc(),
  program_id  UUID      NOT NULL REFERENCES xj.program (id),
  user_id     UUID      NOT NULL REFERENCES xj.user (id),
  hours       REAL             DEFAULT 0,
  description TEXT      NOT NULL,
  timestamp   TIMESTAMP NULL   DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE xj.program_message
(
  id         UUID PRIMARY KEY DEFAULT uuid_generate_v1mc(),
  program_id UUID      NOT NULL REFERENCES xj.program (id),
  user_id    UUID      NOT NULL REFERENCES xj.user (id),
  body       TEXT      NOT NULL,
  timestamp  TIMESTAMP NULL   DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE xj.instrument_authorship
(
  id            UUID PRIMARY KEY DEFAULT uuid_generate_v1mc(),
  instrument_id UUID      NOT NULL REFERENCES xj.instrument (id),
  user_id       UUID      NOT NULL REFERENCES xj.user (id),
  hours         REAL             DEFAULT 0,
  description   TEXT      NOT NULL,
  timestamp     TIMESTAMP NULL   DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE xj.instrument_message
(
  id            UUID PRIMARY KEY DEFAULT uuid_generate_v1mc(),
  instrument_id UUID      NOT NULL REFERENCES xj.instrument (id),
  user_id       UUID      NOT NULL REFERENCES xj.user (id),
  body          TEXT      NOT NULL,
  timestamp     TIMESTAMP NULL   DEFAULT CURRENT_TIMESTAMP
);

