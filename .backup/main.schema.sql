\-\- Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

--
-- PostgreSQL database dump
--

-- Dumped from database version 13.5 (Debian 13.5-1.pgdg110+1)
-- Dumped by pg_dump version 13.6 (Ubuntu 13.6-1.pgdg20.04+1+b1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: xj_dev; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE xj_dev WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE = 'en_US.utf8';


ALTER DATABASE xj_dev OWNER TO postgres;

\connect xj_dev

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: xj; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA xj;


ALTER SCHEMA xj OWNER TO postgres;

--
-- Name: uuid-ossp; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA xj;


--
-- Name: EXTENSION "uuid-ossp"; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION "uuid-ossp" IS 'generate universally unique identifiers (UUIDs)';


--
-- Name: content_binding_type; Type: TYPE; Schema: xj; Owner: postgres
--

CREATE TYPE xj.content_binding_type AS ENUM (
    'Library',
    'Program',
    'Instrument'
);


ALTER TYPE xj.content_binding_type OWNER TO postgres;

--
-- Name: feedback_source; Type: TYPE; Schema: xj; Owner: postgres
--

CREATE TYPE xj.feedback_source AS ENUM (
    'Artist',
    'Listener',
    'Nexus'
);


ALTER TYPE xj.feedback_source OWNER TO postgres;

--
-- Name: feedback_type; Type: TYPE; Schema: xj; Owner: postgres
--

CREATE TYPE xj.feedback_type AS ENUM (
    'Error',
    'Negative',
    'Neutral',
    'Positive',
    'Warning'
);


ALTER TYPE xj.feedback_type OWNER TO postgres;

--
-- Name: instrument_mode; Type: TYPE; Schema: xj; Owner: postgres
--

CREATE TYPE xj.instrument_mode AS ENUM (
    'NoteEvent',
    'VoicingEvent',
    'ChordEvent',
    'VoicingPart',
    'ChordPart',
    'MainPart',
    'VoicingLoop'
);


ALTER TYPE xj.instrument_mode OWNER TO postgres;

--
-- Name: instrument_state; Type: TYPE; Schema: xj; Owner: postgres
--

CREATE TYPE xj.instrument_state AS ENUM (
    'Draft',
    'Published'
);


ALTER TYPE xj.instrument_state OWNER TO postgres;

--
-- Name: instrument_type; Type: TYPE; Schema: xj; Owner: postgres
--

CREATE TYPE xj.instrument_type AS ENUM (
    'Drum',
    'PercLoop',
    'Bass',
    'Pad',
    'Sticky',
    'Stripe',
    'Stab',
    'Transition',
    'Background',
    'Hook'
);


ALTER TYPE xj.instrument_type OWNER TO postgres;

--
-- Name: program_state; Type: TYPE; Schema: xj; Owner: postgres
--

CREATE TYPE xj.program_state AS ENUM (
    'Draft',
    'Published'
);


ALTER TYPE xj.program_state OWNER TO postgres;

--
-- Name: program_type; Type: TYPE; Schema: xj; Owner: postgres
--

CREATE TYPE xj.program_type AS ENUM (
    'Macro',
    'Main',
    'Beat',
    'Detail'
);


ALTER TYPE xj.program_type OWNER TO postgres;

--
-- Name: template_type; Type: TYPE; Schema: xj; Owner: postgres
--

CREATE TYPE xj.template_type AS ENUM (
    'Preview',
    'Production'
);


ALTER TYPE xj.template_type OWNER TO postgres;

--
-- Name: user_auth_type; Type: TYPE; Schema: xj; Owner: postgres
--

CREATE TYPE xj.user_auth_type AS ENUM (
    'Google'
);


ALTER TYPE xj.user_auth_type OWNER TO postgres;

--
-- Name: user_role_type; Type: TYPE; Schema: xj; Owner: postgres
--

CREATE TYPE xj.user_role_type AS ENUM (
    'Internal',
    'Admin',
    'Engineer',
    'Artist',
    'User',
    'Banned'
);


ALTER TYPE xj.user_role_type OWNER TO postgres;

--
-- Name: updated_at_now(); Type: FUNCTION; Schema: xj; Owner: postgres
--

CREATE FUNCTION xj.updated_at_now() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  NEW.updated_at = now();
  RETURN NEW;
END;
$$;


ALTER FUNCTION xj.updated_at_now() OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE public.flyway_schema_history OWNER TO postgres;

--
-- Name: account; Type: TABLE; Schema: xj; Owner: postgres
--

CREATE TABLE xj.account (
    id uuid DEFAULT xj.uuid_generate_v1mc() NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE xj.account OWNER TO postgres;

--
-- Name: account_user; Type: TABLE; Schema: xj; Owner: postgres
--

CREATE TABLE xj.account_user (
    id uuid DEFAULT xj.uuid_generate_v1mc() NOT NULL,
    user_id uuid NOT NULL,
    account_id uuid NOT NULL
);


ALTER TABLE xj.account_user OWNER TO postgres;

--
-- Name: feedback; Type: TABLE; Schema: xj; Owner: postgres
--

CREATE TABLE xj.feedback (
    id uuid DEFAULT xj.uuid_generate_v1mc() NOT NULL,
    source xj.feedback_source NOT NULL,
    type xj.feedback_type NOT NULL,
    body text DEFAULT ''::text,
    account_id uuid NOT NULL,
    user_id uuid,
    "timestamp" timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL
);


ALTER TABLE xj.feedback OWNER TO postgres;

--
-- Name: feedback_instrument; Type: TABLE; Schema: xj; Owner: postgres
--

CREATE TABLE xj.feedback_instrument (
    feedback_id uuid NOT NULL,
    instrument_id uuid NOT NULL
);


ALTER TABLE xj.feedback_instrument OWNER TO postgres;

--
-- Name: feedback_library; Type: TABLE; Schema: xj; Owner: postgres
--

CREATE TABLE xj.feedback_library (
    feedback_id uuid NOT NULL,
    library_id uuid NOT NULL
);


ALTER TABLE xj.feedback_library OWNER TO postgres;

--
-- Name: feedback_program; Type: TABLE; Schema: xj; Owner: postgres
--

CREATE TABLE xj.feedback_program (
    feedback_id uuid NOT NULL,
    program_id uuid NOT NULL,
    program_sequence_id uuid
);


ALTER TABLE xj.feedback_program OWNER TO postgres;

--
-- Name: feedback_template; Type: TABLE; Schema: xj; Owner: postgres
--

CREATE TABLE xj.feedback_template (
    feedback_id uuid NOT NULL,
    template_id uuid NOT NULL,
    segment_key text
);


ALTER TABLE xj.feedback_template OWNER TO postgres;

--
-- Name: flyway_schema_history; Type: TABLE; Schema: xj; Owner: postgres
--

CREATE TABLE xj.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE xj.flyway_schema_history OWNER TO postgres;

--
-- Name: instrument; Type: TABLE; Schema: xj; Owner: postgres
--

CREATE TABLE xj.instrument (
    id uuid DEFAULT xj.uuid_generate_v1mc() NOT NULL,
    library_id uuid NOT NULL,
    type xj.instrument_type NOT NULL,
    state xj.instrument_state NOT NULL,
    name character varying(255) NOT NULL,
    density real NOT NULL,
    config text DEFAULT ''::text NOT NULL,
    is_deleted boolean DEFAULT false,
    volume real DEFAULT 1.0,
    mode xj.instrument_mode DEFAULT 'NoteEvent'::xj.instrument_mode
);


ALTER TABLE xj.instrument OWNER TO postgres;

--
-- Name: instrument_audio; Type: TABLE; Schema: xj; Owner: postgres
--

CREATE TABLE xj.instrument_audio (
    id uuid DEFAULT xj.uuid_generate_v1mc() NOT NULL,
    instrument_id uuid NOT NULL,
    name character varying(255) NOT NULL,
    waveform_key character varying(2047),
    transient_seconds real NOT NULL,
    total_beats real NOT NULL,
    tempo real NOT NULL,
    density real NOT NULL,
    event character varying(255) DEFAULT NULL::character varying,
    volume real,
    tones character varying(255) DEFAULT NULL::character varying
);


ALTER TABLE xj.instrument_audio OWNER TO postgres;

--
-- Name: instrument_meme; Type: TABLE; Schema: xj; Owner: postgres
--

CREATE TABLE xj.instrument_meme (
    id uuid DEFAULT xj.uuid_generate_v1mc() NOT NULL,
    instrument_id uuid NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE xj.instrument_meme OWNER TO postgres;

--
-- Name: library; Type: TABLE; Schema: xj; Owner: postgres
--

CREATE TABLE xj.library (
    id uuid DEFAULT xj.uuid_generate_v1mc() NOT NULL,
    name character varying(255) NOT NULL,
    account_id uuid NOT NULL,
    is_deleted boolean DEFAULT false
);


ALTER TABLE xj.library OWNER TO postgres;

--
-- Name: program; Type: TABLE; Schema: xj; Owner: postgres
--

CREATE TABLE xj.program (
    id uuid DEFAULT xj.uuid_generate_v1mc() NOT NULL,
    library_id uuid NOT NULL,
    state xj.program_state NOT NULL,
    key character varying(255) NOT NULL,
    tempo real NOT NULL,
    type xj.program_type NOT NULL,
    name character varying(255) NOT NULL,
    density real NOT NULL,
    config text DEFAULT ''::text NOT NULL,
    is_deleted boolean DEFAULT false
);


ALTER TABLE xj.program OWNER TO postgres;

--
-- Name: program_meme; Type: TABLE; Schema: xj; Owner: postgres
--

CREATE TABLE xj.program_meme (
    id uuid DEFAULT xj.uuid_generate_v1mc() NOT NULL,
    name character varying(255) NOT NULL,
    program_id uuid NOT NULL
);


ALTER TABLE xj.program_meme OWNER TO postgres;

--
-- Name: program_sequence; Type: TABLE; Schema: xj; Owner: postgres
--

CREATE TABLE xj.program_sequence (
    id uuid DEFAULT xj.uuid_generate_v1mc() NOT NULL,
    program_id uuid NOT NULL,
    name character varying(255) NOT NULL,
    key character varying(255) NOT NULL,
    density real NOT NULL,
    total smallint NOT NULL
);


ALTER TABLE xj.program_sequence OWNER TO postgres;

--
-- Name: program_sequence_binding; Type: TABLE; Schema: xj; Owner: postgres
--

CREATE TABLE xj.program_sequence_binding (
    id uuid DEFAULT xj.uuid_generate_v1mc() NOT NULL,
    program_id uuid NOT NULL,
    program_sequence_id uuid NOT NULL,
    "offset" integer NOT NULL
);


ALTER TABLE xj.program_sequence_binding OWNER TO postgres;

--
-- Name: program_sequence_binding_meme; Type: TABLE; Schema: xj; Owner: postgres
--

CREATE TABLE xj.program_sequence_binding_meme (
    id uuid DEFAULT xj.uuid_generate_v1mc() NOT NULL,
    program_id uuid NOT NULL,
    program_sequence_binding_id uuid NOT NULL,
    name character varying(255) NOT NULL
);


ALTER TABLE xj.program_sequence_binding_meme OWNER TO postgres;

--
-- Name: program_sequence_chord; Type: TABLE; Schema: xj; Owner: postgres
--

CREATE TABLE xj.program_sequence_chord (
    id uuid DEFAULT xj.uuid_generate_v1mc() NOT NULL,
    program_id uuid NOT NULL,
    program_sequence_id uuid NOT NULL,
    name character varying(255) NOT NULL,
    "position" double precision NOT NULL
);


ALTER TABLE xj.program_sequence_chord OWNER TO postgres;

--
-- Name: program_sequence_chord_voicing; Type: TABLE; Schema: xj; Owner: postgres
--

CREATE TABLE xj.program_sequence_chord_voicing (
    id uuid DEFAULT xj.uuid_generate_v1mc() NOT NULL,
    program_id uuid NOT NULL,
    program_sequence_chord_id uuid NOT NULL,
    type xj.instrument_type NOT NULL,
    notes text NOT NULL
);


ALTER TABLE xj.program_sequence_chord_voicing OWNER TO postgres;

--
-- Name: program_sequence_pattern; Type: TABLE; Schema: xj; Owner: postgres
--

CREATE TABLE xj.program_sequence_pattern (
    id uuid DEFAULT xj.uuid_generate_v1mc() NOT NULL,
    program_id uuid NOT NULL,
    program_sequence_id uuid NOT NULL,
    program_voice_id uuid NOT NULL,
    name character varying(255) NOT NULL,
    total smallint NOT NULL
);


ALTER TABLE xj.program_sequence_pattern OWNER TO postgres;

--
-- Name: program_sequence_pattern_event; Type: TABLE; Schema: xj; Owner: postgres
--

CREATE TABLE xj.program_sequence_pattern_event (
    id uuid DEFAULT xj.uuid_generate_v1mc() NOT NULL,
    program_id uuid NOT NULL,
    program_sequence_pattern_id uuid NOT NULL,
    program_voice_track_id uuid NOT NULL,
    velocity real NOT NULL,
    "position" real NOT NULL,
    duration real NOT NULL,
    tones character varying(255) NOT NULL
);


ALTER TABLE xj.program_sequence_pattern_event OWNER TO postgres;

--
-- Name: program_voice; Type: TABLE; Schema: xj; Owner: postgres
--

CREATE TABLE xj.program_voice (
    id uuid DEFAULT xj.uuid_generate_v1mc() NOT NULL,
    program_id uuid NOT NULL,
    type xj.instrument_type NOT NULL,
    name character varying(255) NOT NULL,
    "order" real DEFAULT 1000.0
);


ALTER TABLE xj.program_voice OWNER TO postgres;

--
-- Name: program_voice_track; Type: TABLE; Schema: xj; Owner: postgres
--

CREATE TABLE xj.program_voice_track (
    id uuid DEFAULT xj.uuid_generate_v1mc() NOT NULL,
    program_id uuid NOT NULL,
    program_voice_id uuid NOT NULL,
    name character varying(255) NOT NULL,
    "order" real DEFAULT 1000.0
);


ALTER TABLE xj.program_voice_track OWNER TO postgres;

--
-- Name: template; Type: TABLE; Schema: xj; Owner: postgres
--

CREATE TABLE xj.template (
    id uuid DEFAULT xj.uuid_generate_v1mc() NOT NULL,
    account_id uuid NOT NULL,
    name character varying(255) NOT NULL,
    config text DEFAULT ''::text,
    ship_key character varying(255) DEFAULT ''::character varying,
    type xj.template_type,
    is_deleted boolean DEFAULT false
);


ALTER TABLE xj.template OWNER TO postgres;

--
-- Name: template_binding; Type: TABLE; Schema: xj; Owner: postgres
--

CREATE TABLE xj.template_binding (
    id uuid DEFAULT xj.uuid_generate_v1mc() NOT NULL,
    type xj.content_binding_type NOT NULL,
    template_id uuid NOT NULL,
    target_id uuid NOT NULL
);


ALTER TABLE xj.template_binding OWNER TO postgres;

--
-- Name: template_playback; Type: TABLE; Schema: xj; Owner: postgres
--

CREATE TABLE xj.template_playback (
    id uuid DEFAULT xj.uuid_generate_v1mc() NOT NULL,
    template_id uuid NOT NULL,
    user_id uuid NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE xj.template_playback OWNER TO postgres;

--
-- Name: template_publication; Type: TABLE; Schema: xj; Owner: postgres
--

CREATE TABLE xj.template_publication (
    id uuid DEFAULT xj.uuid_generate_v1mc() NOT NULL,
    template_id uuid NOT NULL,
    user_id uuid NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE xj.template_publication OWNER TO postgres;

--
-- Name: user; Type: TABLE; Schema: xj; Owner: postgres
--

CREATE TABLE xj."user" (
    id uuid DEFAULT xj.uuid_generate_v1mc() NOT NULL,
    name character varying(255) NOT NULL,
    email character varying(1023) DEFAULT NULL::character varying,
    avatar_url character varying(1023) DEFAULT NULL::character varying,
    roles character varying(255) DEFAULT 'User'::character varying NOT NULL
);


ALTER TABLE xj."user" OWNER TO postgres;

--
-- Name: user_auth; Type: TABLE; Schema: xj; Owner: postgres
--

CREATE TABLE xj.user_auth (
    id uuid DEFAULT xj.uuid_generate_v1mc() NOT NULL,
    type xj.user_auth_type NOT NULL,
    external_access_token character varying(1023) NOT NULL,
    external_refresh_token character varying(1023) DEFAULT NULL::character varying,
    external_account character varying(1023) NOT NULL,
    user_id uuid NOT NULL
);


ALTER TABLE xj.user_auth OWNER TO postgres;

--
-- Name: user_auth_token; Type: TABLE; Schema: xj; Owner: postgres
--

CREATE TABLE xj.user_auth_token (
    id uuid DEFAULT xj.uuid_generate_v1mc() NOT NULL,
    user_auth_id uuid NOT NULL,
    user_id uuid NOT NULL,
    access_token text NOT NULL
);


ALTER TABLE xj.user_auth_token OWNER TO postgres;

--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: account account_pkey; Type: CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.account
    ADD CONSTRAINT account_pkey PRIMARY KEY (id);


--
-- Name: account_user account_user_pkey; Type: CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.account_user
    ADD CONSTRAINT account_user_pkey PRIMARY KEY (id);


--
-- Name: feedback_instrument feedback_instrument_pkey; Type: CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.feedback_instrument
    ADD CONSTRAINT feedback_instrument_pkey PRIMARY KEY (feedback_id, instrument_id);


--
-- Name: feedback_library feedback_library_pkey; Type: CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.feedback_library
    ADD CONSTRAINT feedback_library_pkey PRIMARY KEY (feedback_id, library_id);


--
-- Name: feedback feedback_pkey; Type: CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.feedback
    ADD CONSTRAINT feedback_pkey PRIMARY KEY (id);


--
-- Name: feedback_program feedback_program_pkey; Type: CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.feedback_program
    ADD CONSTRAINT feedback_program_pkey PRIMARY KEY (feedback_id, program_id);


--
-- Name: feedback_template feedback_template_pkey; Type: CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.feedback_template
    ADD CONSTRAINT feedback_template_pkey PRIMARY KEY (feedback_id, template_id);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: instrument_audio instrument_audio_pkey; Type: CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.instrument_audio
    ADD CONSTRAINT instrument_audio_pkey PRIMARY KEY (id);


--
-- Name: instrument_meme instrument_meme_pkey; Type: CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.instrument_meme
    ADD CONSTRAINT instrument_meme_pkey PRIMARY KEY (id);


--
-- Name: instrument instrument_pkey; Type: CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.instrument
    ADD CONSTRAINT instrument_pkey PRIMARY KEY (id);


--
-- Name: library library_pkey; Type: CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.library
    ADD CONSTRAINT library_pkey PRIMARY KEY (id);


--
-- Name: program_meme program_meme_pkey; Type: CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.program_meme
    ADD CONSTRAINT program_meme_pkey PRIMARY KEY (id);


--
-- Name: program program_pkey; Type: CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.program
    ADD CONSTRAINT program_pkey PRIMARY KEY (id);


--
-- Name: program_sequence_binding_meme program_sequence_binding_meme_pkey; Type: CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.program_sequence_binding_meme
    ADD CONSTRAINT program_sequence_binding_meme_pkey PRIMARY KEY (id);


--
-- Name: program_sequence_binding program_sequence_binding_pkey; Type: CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.program_sequence_binding
    ADD CONSTRAINT program_sequence_binding_pkey PRIMARY KEY (id);


--
-- Name: program_sequence_chord program_sequence_chord_pkey; Type: CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.program_sequence_chord
    ADD CONSTRAINT program_sequence_chord_pkey PRIMARY KEY (id);


--
-- Name: program_sequence_chord_voicing program_sequence_chord_voicing_pkey; Type: CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.program_sequence_chord_voicing
    ADD CONSTRAINT program_sequence_chord_voicing_pkey PRIMARY KEY (id);


--
-- Name: program_sequence_pattern_event program_sequence_pattern_event_pkey; Type: CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.program_sequence_pattern_event
    ADD CONSTRAINT program_sequence_pattern_event_pkey PRIMARY KEY (id);


--
-- Name: program_sequence_pattern program_sequence_pattern_pkey; Type: CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.program_sequence_pattern
    ADD CONSTRAINT program_sequence_pattern_pkey PRIMARY KEY (id);


--
-- Name: program_sequence program_sequence_pkey; Type: CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.program_sequence
    ADD CONSTRAINT program_sequence_pkey PRIMARY KEY (id);


--
-- Name: program_voice program_voice_pkey; Type: CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.program_voice
    ADD CONSTRAINT program_voice_pkey PRIMARY KEY (id);


--
-- Name: program_voice_track program_voice_track_pkey; Type: CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.program_voice_track
    ADD CONSTRAINT program_voice_track_pkey PRIMARY KEY (id);


--
-- Name: template_binding template_binding_pkey; Type: CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.template_binding
    ADD CONSTRAINT template_binding_pkey PRIMARY KEY (id);


--
-- Name: template template_pkey; Type: CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.template
    ADD CONSTRAINT template_pkey PRIMARY KEY (id);


--
-- Name: template_playback template_playback_pkey; Type: CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.template_playback
    ADD CONSTRAINT template_playback_pkey PRIMARY KEY (id);


--
-- Name: template_publication template_publication_pkey; Type: CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.template_publication
    ADD CONSTRAINT template_publication_pkey PRIMARY KEY (id);


--
-- Name: user_auth user_auth_pkey; Type: CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.user_auth
    ADD CONSTRAINT user_auth_pkey PRIMARY KEY (id);


--
-- Name: user_auth_token user_auth_token_pkey; Type: CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.user_auth_token
    ADD CONSTRAINT user_auth_token_pkey PRIMARY KEY (id);


--
-- Name: user user_pkey; Type: CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj."user"
    ADD CONSTRAINT user_pkey PRIMARY KEY (id);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: xj; Owner: postgres
--

CREATE INDEX flyway_schema_history_s_idx ON xj.flyway_schema_history USING btree (success);


--
-- Name: program_voice_order_idx; Type: INDEX; Schema: xj; Owner: postgres
--

CREATE INDEX program_voice_order_idx ON xj.program_voice USING btree ("order");


--
-- Name: program_voice_track_order_idx; Type: INDEX; Schema: xj; Owner: postgres
--

CREATE INDEX program_voice_track_order_idx ON xj.program_voice_track USING btree ("order");


--
-- Name: account_user account_user_account_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.account_user
    ADD CONSTRAINT account_user_account_id_fkey FOREIGN KEY (account_id) REFERENCES xj.account(id);


--
-- Name: account_user account_user_user_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.account_user
    ADD CONSTRAINT account_user_user_id_fkey FOREIGN KEY (user_id) REFERENCES xj."user"(id);


--
-- Name: feedback feedback_account_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.feedback
    ADD CONSTRAINT feedback_account_id_fkey FOREIGN KEY (account_id) REFERENCES xj.account(id);


--
-- Name: feedback_instrument feedback_instrument_feedback_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.feedback_instrument
    ADD CONSTRAINT feedback_instrument_feedback_id_fkey FOREIGN KEY (feedback_id) REFERENCES xj.feedback(id);


--
-- Name: feedback_instrument feedback_instrument_instrument_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.feedback_instrument
    ADD CONSTRAINT feedback_instrument_instrument_id_fkey FOREIGN KEY (instrument_id) REFERENCES xj.instrument(id);


--
-- Name: feedback_library feedback_library_feedback_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.feedback_library
    ADD CONSTRAINT feedback_library_feedback_id_fkey FOREIGN KEY (feedback_id) REFERENCES xj.feedback(id);


--
-- Name: feedback_library feedback_library_library_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.feedback_library
    ADD CONSTRAINT feedback_library_library_id_fkey FOREIGN KEY (library_id) REFERENCES xj.library(id);


--
-- Name: feedback_program feedback_program_feedback_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.feedback_program
    ADD CONSTRAINT feedback_program_feedback_id_fkey FOREIGN KEY (feedback_id) REFERENCES xj.feedback(id);


--
-- Name: feedback_program feedback_program_program_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.feedback_program
    ADD CONSTRAINT feedback_program_program_id_fkey FOREIGN KEY (program_id) REFERENCES xj.program(id);


--
-- Name: feedback_program feedback_program_program_sequence_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.feedback_program
    ADD CONSTRAINT feedback_program_program_sequence_id_fkey FOREIGN KEY (program_sequence_id) REFERENCES xj.program_sequence(id);


--
-- Name: feedback_template feedback_template_feedback_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.feedback_template
    ADD CONSTRAINT feedback_template_feedback_id_fkey FOREIGN KEY (feedback_id) REFERENCES xj.feedback(id);


--
-- Name: feedback_template feedback_template_template_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.feedback_template
    ADD CONSTRAINT feedback_template_template_id_fkey FOREIGN KEY (template_id) REFERENCES xj.template(id);


--
-- Name: feedback feedback_user_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.feedback
    ADD CONSTRAINT feedback_user_id_fkey FOREIGN KEY (user_id) REFERENCES xj."user"(id);


--
-- Name: instrument_audio instrument_audio_instrument_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.instrument_audio
    ADD CONSTRAINT instrument_audio_instrument_id_fkey FOREIGN KEY (instrument_id) REFERENCES xj.instrument(id);


--
-- Name: instrument instrument_library_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.instrument
    ADD CONSTRAINT instrument_library_id_fkey FOREIGN KEY (library_id) REFERENCES xj.library(id);


--
-- Name: instrument_meme instrument_meme_instrument_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.instrument_meme
    ADD CONSTRAINT instrument_meme_instrument_id_fkey FOREIGN KEY (instrument_id) REFERENCES xj.instrument(id);


--
-- Name: library library_account_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.library
    ADD CONSTRAINT library_account_id_fkey FOREIGN KEY (account_id) REFERENCES xj.account(id);


--
-- Name: program program_library_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.program
    ADD CONSTRAINT program_library_id_fkey FOREIGN KEY (library_id) REFERENCES xj.library(id);


--
-- Name: program_meme program_meme_program_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.program_meme
    ADD CONSTRAINT program_meme_program_id_fkey FOREIGN KEY (program_id) REFERENCES xj.program(id);


--
-- Name: program_sequence_binding_meme program_sequence_binding_meme_program_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.program_sequence_binding_meme
    ADD CONSTRAINT program_sequence_binding_meme_program_id_fkey FOREIGN KEY (program_id) REFERENCES xj.program(id);


--
-- Name: program_sequence_binding_meme program_sequence_binding_meme_program_sequence_binding_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.program_sequence_binding_meme
    ADD CONSTRAINT program_sequence_binding_meme_program_sequence_binding_id_fkey FOREIGN KEY (program_sequence_binding_id) REFERENCES xj.program_sequence_binding(id);


--
-- Name: program_sequence_binding program_sequence_binding_program_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.program_sequence_binding
    ADD CONSTRAINT program_sequence_binding_program_id_fkey FOREIGN KEY (program_id) REFERENCES xj.program(id);


--
-- Name: program_sequence_binding program_sequence_binding_program_sequence_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.program_sequence_binding
    ADD CONSTRAINT program_sequence_binding_program_sequence_id_fkey FOREIGN KEY (program_sequence_id) REFERENCES xj.program_sequence(id);


--
-- Name: program_sequence_chord program_sequence_chord_program_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.program_sequence_chord
    ADD CONSTRAINT program_sequence_chord_program_id_fkey FOREIGN KEY (program_id) REFERENCES xj.program(id);


--
-- Name: program_sequence_chord program_sequence_chord_program_sequence_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.program_sequence_chord
    ADD CONSTRAINT program_sequence_chord_program_sequence_id_fkey FOREIGN KEY (program_sequence_id) REFERENCES xj.program_sequence(id);


--
-- Name: program_sequence_chord_voicing program_sequence_chord_voicing_program_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.program_sequence_chord_voicing
    ADD CONSTRAINT program_sequence_chord_voicing_program_id_fkey FOREIGN KEY (program_id) REFERENCES xj.program(id);


--
-- Name: program_sequence_chord_voicing program_sequence_chord_voicing_program_sequence_chord_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.program_sequence_chord_voicing
    ADD CONSTRAINT program_sequence_chord_voicing_program_sequence_chord_id_fkey FOREIGN KEY (program_sequence_chord_id) REFERENCES xj.program_sequence_chord(id);


--
-- Name: program_sequence_pattern_event program_sequence_pattern_event_program_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.program_sequence_pattern_event
    ADD CONSTRAINT program_sequence_pattern_event_program_id_fkey FOREIGN KEY (program_id) REFERENCES xj.program(id);


--
-- Name: program_sequence_pattern_event program_sequence_pattern_event_program_sequence_pattern_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.program_sequence_pattern_event
    ADD CONSTRAINT program_sequence_pattern_event_program_sequence_pattern_id_fkey FOREIGN KEY (program_sequence_pattern_id) REFERENCES xj.program_sequence_pattern(id);


--
-- Name: program_sequence_pattern_event program_sequence_pattern_event_program_voice_track_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.program_sequence_pattern_event
    ADD CONSTRAINT program_sequence_pattern_event_program_voice_track_id_fkey FOREIGN KEY (program_voice_track_id) REFERENCES xj.program_voice_track(id);


--
-- Name: program_sequence_pattern program_sequence_pattern_program_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.program_sequence_pattern
    ADD CONSTRAINT program_sequence_pattern_program_id_fkey FOREIGN KEY (program_id) REFERENCES xj.program(id);


--
-- Name: program_sequence_pattern program_sequence_pattern_program_sequence_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.program_sequence_pattern
    ADD CONSTRAINT program_sequence_pattern_program_sequence_id_fkey FOREIGN KEY (program_sequence_id) REFERENCES xj.program_sequence(id);


--
-- Name: program_sequence_pattern program_sequence_pattern_program_voice_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.program_sequence_pattern
    ADD CONSTRAINT program_sequence_pattern_program_voice_id_fkey FOREIGN KEY (program_voice_id) REFERENCES xj.program_voice(id);


--
-- Name: program_sequence program_sequence_program_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.program_sequence
    ADD CONSTRAINT program_sequence_program_id_fkey FOREIGN KEY (program_id) REFERENCES xj.program(id);


--
-- Name: program_voice program_voice_program_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.program_voice
    ADD CONSTRAINT program_voice_program_id_fkey FOREIGN KEY (program_id) REFERENCES xj.program(id);


--
-- Name: program_voice_track program_voice_track_program_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.program_voice_track
    ADD CONSTRAINT program_voice_track_program_id_fkey FOREIGN KEY (program_id) REFERENCES xj.program(id);


--
-- Name: program_voice_track program_voice_track_program_voice_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.program_voice_track
    ADD CONSTRAINT program_voice_track_program_voice_id_fkey FOREIGN KEY (program_voice_id) REFERENCES xj.program_voice(id);


--
-- Name: user_auth_token user_auth_token_user_auth_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.user_auth_token
    ADD CONSTRAINT user_auth_token_user_auth_id_fkey FOREIGN KEY (user_auth_id) REFERENCES xj.user_auth(id);


--
-- Name: user_auth_token user_auth_token_user_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.user_auth_token
    ADD CONSTRAINT user_auth_token_user_id_fkey FOREIGN KEY (user_id) REFERENCES xj."user"(id);


--
-- Name: user_auth user_auth_user_id_fkey; Type: FK CONSTRAINT; Schema: xj; Owner: postgres
--

ALTER TABLE ONLY xj.user_auth
    ADD CONSTRAINT user_auth_user_id_fkey FOREIGN KEY (user_id) REFERENCES xj."user"(id);


--
-- PostgreSQL database dump complete
--

--
-- PostgreSQL database dump
--

-- Dumped from database version 13.5 (Debian 13.5-1.pgdg110+1)
-- Dumped by pg_dump version 13.6 (Ubuntu 13.6-1.pgdg20.04+1+b1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: flyway_schema_history; Type: TABLE DATA; Schema: xj; Owner: postgres
--

COPY xj.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) FROM stdin;
0	\N	<< Flyway Schema Creation >>	SCHEMA	"xj"	\N	root	2020-03-03 15:30:26.020979	0	t
1	2	create extension uuid ossp	SQL	V2__create_extension_uuid_ossp.sql	2034728600	root	2020-03-03 15:30:26.042693	4	t
2	3	create updated at now	SQL	V3__create_updated_at_now.sql	-981124898	root	2020-03-03 15:30:26.058347	2	t
3	4	user	SQL	V4__user.sql	913045606	root	2020-03-03 15:30:26.071053	23	t
4	5	user auth	SQL	V5__user_auth.sql	-448926434	root	2020-03-03 15:30:26.108625	23	t
5	6	user auth token	SQL	V6__user_auth_token.sql	-1426970887	root	2020-03-03 15:30:26.140811	14	t
6	7	user role	SQL	V7__user_role.sql	520858385	root	2020-03-03 15:30:26.163839	11	t
7	8	account	SQL	V8__account.sql	-1413282908	root	2020-03-03 15:30:26.185594	14	t
8	9	account user	SQL	V9__account_user.sql	1415864885	root	2020-03-03 15:30:26.208847	12	t
9	10	library	SQL	V10__library.sql	1259098220	root	2020-03-03 15:30:26.23045	8	t
10	11	instrument	SQL	V11__instrument.sql	-1433436616	root	2020-03-03 15:30:26.244786	14	t
11	12	instrument meme	SQL	V12__instrument_meme.sql	-1027003655	root	2020-03-03 15:30:26.265726	13	t
12	13	instrument audio	SQL	V13__instrument_audio.sql	-1261244184	root	2020-03-03 15:30:26.288726	23	t
13	14	instrument audio chord	SQL	V14__instrument_audio_chord.sql	1803612396	root	2020-03-03 15:30:26.320943	8	t
14	15	instrument audio event	SQL	V15__instrument_audio_event.sql	1426476358	root	2020-03-03 15:30:26.335761	14	t
15	16	program	SQL	V16__program.sql	-941829822	root	2020-03-03 15:30:26.356632	26	t
16	17	program meme	SQL	V17__program_meme.sql	1450167054	root	2020-03-03 15:30:26.391512	12	t
17	18	program voice	SQL	V18__program_voice.sql	-276352046	root	2020-03-03 15:30:26.411963	14	t
18	19	program voice track	SQL	V19__program_voice_track.sql	1664915003	root	2020-03-03 15:30:26.432717	7	t
19	20	program sequence	SQL	V20__program_sequence.sql	-1827766662	root	2020-03-03 15:30:26.44644	25	t
20	21	program sequence chord	SQL	V21__program_sequence_chord.sql	-261844648	root	2020-03-03 15:30:26.480505	12	t
21	22	program sequence binding	SQL	V22__program_sequence_binding.sql	42064776	root	2020-03-03 15:30:26.501233	9	t
22	23	program sequence binding meme	SQL	V23__program_sequence_binding_meme.sql	-1024515531	root	2020-03-03 15:30:26.516872	8	t
23	24	program sequence pattern	SQL	V24__program_sequence_pattern.sql	-2133019060	root	2020-03-03 15:30:26.532549	26	t
24	25	program sequence pattern event	SQL	V25__program_sequence_pattern_event.sql	1863665119	root	2020-03-03 15:30:26.569972	15	t
25	26	chain	SQL	V26__chain.sql	-817753471	root	2020-03-03 15:30:26.602448	30	t
26	27	chain config	SQL	V27__chain_config.sql	-1978914320	root	2020-03-03 15:30:26.642301	14	t
27	28	chain binding	SQL	V28__chain_binding.sql	1711464431	root	2020-03-03 15:30:26.665447	13	t
28	29	segment	SQL	V29__segment.sql	839872793	root	2020-03-03 15:30:26.687993	23	t
29	30	segment meme	SQL	V30__segment_meme.sql	1945287463	root	2020-03-03 15:30:26.717552	8	t
30	31	segment chord	SQL	V31__segment_chord.sql	-23710397	root	2020-03-03 15:30:26.732261	8	t
31	32	segment choice	SQL	V32__segment_choice.sql	1686427245	root	2020-03-03 15:30:26.747424	13	t
32	33	segment choice arrangement	SQL	V33__segment_choice_arrangement.sql	826509835	root	2020-03-03 15:30:26.768716	12	t
33	34	segment choice arrangement pick	SQL	V34__segment_choice_arrangement_pick.sql	1654315292	root	2020-03-03 15:30:26.788818	12	t
34	35	segment message	SQL	V35__segment_message.sql	-895760763	root	2020-03-03 15:30:26.806766	14	t
35	36	platform message	SQL	V36__platform_message.sql	-1549213473	root	2020-03-03 15:30:26.827204	14	t
36	37	work	SQL	V37__work.sql	-1737567323	root	2020-03-03 15:30:26.847661	8	t
37	38	segment waveform preroll	SQL	V38__segment_waveform_preroll.sql	-1820712501	root	2020-03-03 15:30:26.869638	1	t
38	39	nexus tables dropped	SQL	V39__nexus_tables_dropped.sql	639873279	root	2020-07-15 00:06:47.139317	21	t
39	40	program voice track order	SQL	V40__program_voice_track_order.sql	-2048787325	root	2020-08-21 21:35:32.45529	20	t
40	41	program sequence chord voicing	SQL	V41__program_sequence_chord_voicing.sql	-1144011713	root	2020-10-22 02:53:13.759502	34	t
41	42	config program and instrument	SQL	V42__config_program_and_instrument.sql	-1304738102	root	2020-10-22 02:53:13.807552	4	t
42	43	delete created updated at columns	SQL	V43__delete_created_updated_at_columns.sql	2082510389	root	2020-11-17 20:11:47.524173	31	t
43	44	updated at delete procedures	SQL	V44__updated_at_delete_procedures.sql	-845882979	root	2020-11-17 20:11:47.580165	12	t
44	45	drop column program instrument user id	SQL	V45__drop_column_program_instrument_user_id.sql	1859235592	root	2020-11-17 20:11:47.602271	3	t
45	46	instrument audio waveform key optional	SQL	V46__instrument_audio_waveform_key_optional.sql	298148594	root	2020-11-19 06:24:25.644148	8	t
46	47	update instrument types	SQL	V47__update_instrument_types.sql	-438215389	postgres	2021-01-22 03:13:34.422717	62	t
47	48	remove pitch	SQL	V48__remove_pitch.sql	-2076774207	postgres	2021-03-19 04:10:28.345175	20	t
48	49	migrate instrument audio event	SQL	V49__migrate_instrument_audio_event.sql	-959935337	postgres	2021-06-02 06:58:43.736841	46	t
49	50	templates	SQL	V50__templates.sql	-1571497899	postgres	2021-08-30 17:48:51.037965	181	t
50	51	template playback stateless	SQL	V51__template_playback_stateless.sql	-1969369120	postgres	2021-08-30 17:48:51.249366	4	t
51	52	template playback created at	SQL	V52__template_playback_created_at.sql	-155540697	postgres	2021-08-30 17:48:51.276355	7	t
52	53	template type	SQL	V53__template_type.sql	1695445248	postgres	2021-08-30 17:48:51.301977	2	t
53	54	refactor percussive to drum	SQL	V54__refactor_percussive_to_drum.sql	-912722638	postgres	2021-09-10 03:39:10.815487	43	t
54	55	more jooq less swagger	SQL	V55__more_jooq_less_swagger.sql	-800589339	postgres	2021-09-17 21:00:02.429484	1119	t
55	56	content authors	SQL	V56__content_authors.sql	286572797	postgres	2021-09-18 00:52:39.518455	345	t
56	57	ship key	SQL	V57__ship_key.sql	163079654	postgres	2021-09-18 22:31:46.588341	45	t
57	58	no pattern types	SQL	V58__no_pattern_types.sql	739249340	postgres	2021-10-25 02:21:54.290671	52	t
58	59	audio transient beats	SQL	V59__audio_transient_beats.sql	-1097739720	postgres	2021-10-27 04:14:32.968872	67	t
59	60	feedback	SQL	V60__feedback.sql	-2095068615	postgres	2021-11-05 07:52:51.149202	307	t
60	61	soft deletion	SQL	V61__soft_deletion.sql	970780052	postgres	2021-11-05 07:52:51.816645	8	t
61	62	instrument transition ambient	SQL	V62__instrument_transition_ambient.sql	-1647046792	postgres	2021-11-10 07:07:15.169513	13	t
62	63	program type beat	SQL	V63__program_type_beat.sql	1314367548	postgres	2021-12-29 01:00:42.858408	14	t
63	64	instrument volume	SQL	V64__instrument_volume.sql	773430791	postgres	2022-01-15 04:01:07.001452	94	t
64	65	template publication	SQL	V65__template_publication.sql	1713007390	postgres	2022-01-20 09:23:19.28495	263	t
65	66	instrument mode	SQL	V66__instrument_mode.sql	-1860015340	postgres	2022-02-09 23:28:51.201581	69	t
66	67	tempo v2	SQL	V67__tempo_v2.sql	463621531	postgres	2022-02-15 08:46:45.652711	31	t
67	68	instrument mode	SQL	V68__instrument_mode.sql	-1753693622	postgres	2022-02-17 00:28:20.795213	66	t
68	69	hook instruments	SQL	V69__hook_instruments.sql	-647722380	postgres	2022-04-07 03:55:53.459428	14	t
69	70	voicing loop instruments	SQL	V70__voicing_loop_instruments.sql	-86880693	postgres	2022-04-07 03:55:53.522426	8	t
70	71	rename note to tones	SQL	V71__rename_note_to_tones.sql	1076040293	postgres	2022-04-11 03:34:19.886997	14	t
\.


--
-- PostgreSQL database dump complete
--

