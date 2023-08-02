/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */

--
-- Template Feedback Persistence #180138745
--

DROP TABLE xj.program_authorship;

DROP TABLE xj.program_message;

DROP TABLE xj.instrument_authorship;

DROP TABLE xj.instrument_message;

CREATE TYPE feedback_type AS ENUM (
  'Error',
  'Negative',
  'Neutral',
  'Positive',
  'Warning'
  );

CREATE TYPE feedback_source AS ENUM (
  'Artist',
  'Listener',
  'Nexus'
  );

CREATE TABLE xj.feedback
(
  id         UUID PRIMARY KEY         DEFAULT uuid_generate_v1mc(),
  source     feedback_source NOT NULL,
  type       feedback_type   NOT NULL,
  body       TEXT                     DEFAULT '',
  account_id UUID            NOT NULL REFERENCES xj.account (id),
  user_id    UUID                     DEFAULT NULL REFERENCES xj.user (id),
  timestamp  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE xj.feedback_template
(
  feedback_id UUID NOT NULL REFERENCES xj.feedback (id),
  template_id UUID NOT NULL REFERENCES xj.template (id),
  segment_key TEXT DEFAULT NULL,
  PRIMARY KEY (feedback_id, template_id)
);

CREATE TABLE xj.feedback_program
(
  feedback_id         UUID NOT NULL REFERENCES xj.feedback (id),
  program_id          UUID NOT NULL REFERENCES xj.program (id),
  program_sequence_id UUID DEFAULT NULL REFERENCES xj.program_sequence (id),
  PRIMARY KEY (feedback_id, program_id)
);

CREATE TABLE xj.feedback_library
(
  feedback_id UUID NOT NULL REFERENCES xj.feedback (id),
  library_id  UUID NOT NULL REFERENCES xj.library (id),
  PRIMARY KEY (feedback_id, library_id)
);

CREATE TABLE xj.feedback_instrument
(
  feedback_id   UUID NOT NULL REFERENCES xj.feedback (id),
  instrument_id UUID NOT NULL REFERENCES xj.instrument (id),
  PRIMARY KEY (feedback_id, instrument_id)
);

