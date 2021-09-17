/* Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved. */

--
-- User has roles column, not a separate table
--

ALTER TABLE xj."user"
  ADD COLUMN roles varchar(255) NOT NULL DEFAULT 'User';

UPDATE xj."user"
SET roles='User,Artist,Engineer'
WHERE id IN (
             '14c688fe-16eb-11ea-8a37-a798275d637c', -- Mark Stewart
             '14e0e8c0-16eb-11ea-8a37-336eae21d833', -- Jamal Whitaker
             '8a2439bc-a9ca-11ea-9cb5-33555a4054e4', -- Ian Hersey
             '1b299a74-f187-11eb-8314-cf9697b34927' -- Dave Cole
  );

UPDATE xj."user"
SET roles='Admin'
WHERE id IN (
  '488ada38-16ea-11ea-88e3-573050fbaae5' -- Charney Kaye
  );

DROP TABLE xj."user_role";

--
-- Values can be float precision, not double
--

ALTER TABLE xj."program_sequence_pattern_event"
  ALTER COLUMN position TYPE real;

ALTER TABLE xj."program_voice"
  ALTER COLUMN "order" TYPE real;

ALTER TABLE xj."program_voice_track"
  ALTER COLUMN "order" TYPE real;


--
-- Postgres Enums!
--

CREATE TYPE content_binding_type AS ENUM (
  'Library',
  'Program',
  'Instrument'
  );

ALTER TABLE xj.template_binding
  ALTER COLUMN type TYPE content_binding_type USING type::content_binding_type;

CREATE TYPE instrument_state AS ENUM (
  'Draft',
  'Published'
  );

ALTER TABLE xj.instrument
  ALTER COLUMN state TYPE instrument_state USING state::instrument_state;

CREATE TYPE instrument_type AS ENUM (
  'Drum',
  'PercLoop',
  'Bass',
  'Pad',
  'Sticky',
  'Stripe',
  'Stab'
  );

ALTER TABLE xj.instrument
  ALTER COLUMN type TYPE instrument_type USING type::instrument_type;

ALTER TABLE xj.program_sequence_chord_voicing
  ALTER COLUMN type TYPE instrument_type USING type::instrument_type;

ALTER TABLE xj.program_voice
  ALTER COLUMN type TYPE instrument_type USING type::instrument_type;

CREATE TYPE program_sequence_pattern_type AS ENUM (
  'Intro',
  'Loop',
  'Outro'
  );

ALTER TABLE xj.program_sequence_pattern
  ALTER COLUMN type TYPE program_sequence_pattern_type USING type::program_sequence_pattern_type;

CREATE TYPE program_state AS ENUM (
  'Draft',
  'Published'
  );

ALTER TABLE xj.program
  ALTER COLUMN state TYPE program_state USING state::program_state;

CREATE TYPE program_type AS ENUM (
  'Macro',
  'Main',
  'Rhythm',
  'Detail'
  );

ALTER TABLE xj.program
  ALTER COLUMN type TYPE program_type USING type::program_type;

CREATE TYPE user_role_type AS ENUM (
  'Internal',
  'Admin',
  'Engineer',
  'Artist',
  'User',
  'Banned'
  );

CREATE TYPE user_auth_type AS ENUM (
  'Google'
  );

ALTER TABLE xj.user_auth
  ALTER COLUMN type TYPE user_auth_type USING type::user_auth_type;

CREATE TYPE template_type AS ENUM (
  'Preview',
  'Production'
  );

ALTER TABLE xj.template
  ALTER COLUMN type DROP DEFAULT;

ALTER TABLE xj.template
  ALTER COLUMN type TYPE template_type USING type::template_type;
