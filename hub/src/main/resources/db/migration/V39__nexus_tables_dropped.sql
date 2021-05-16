--  Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
--
-- [#171553408] XJ Lab Distributed Architecture
--   * Only user, account, and library entities will remain in the relational database.
--   * Migration to destroy Postgres tables:
--       `chain`
--       `chain_binding`
--       `chain_config`
--       `segment`
--       `segment_choice`
--       `segment_choice_arrangement`
--       `segment_choice_arrangement_pick`
--       `segment_chord`
--       `segment_meme`
--       `segment_message`
--       `platform_message`
--       `work`
--

drop table xj.work; -- before anything

drop table xj.segment_choice_arrangement_pick; -- before segment choice arrangement

drop table xj.segment_choice_arrangement; -- before segment choice

drop table xj.segment_choice; -- before segment

drop table xj.segment_chord; -- before segment

drop table xj.segment_meme; -- before segment

drop table xj.segment_message; -- before segment

drop table xj.segment; -- before chain

drop table xj.chain_binding; -- before chain

drop table xj.chain_config; -- before chain

drop table xj.chain;

drop table xj.platform_message;

