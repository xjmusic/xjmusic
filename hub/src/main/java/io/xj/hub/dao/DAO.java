// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.AccountUser;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.InstrumentMeme;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramMeme;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.hub.tables.pojos.ProgramSequenceBindingMeme;
import io.xj.hub.tables.pojos.ProgramSequenceChord;
import io.xj.hub.tables.pojos.ProgramSequenceChordVoicing;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.tables.pojos.ProgramSequencePatternEvent;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.tables.pojos.ProgramVoiceTrack;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplateBinding;
import io.xj.hub.tables.pojos.TemplatePlayback;
import io.xj.hub.tables.pojos.User;
import io.xj.hub.tables.pojos.UserAuth;
import io.xj.hub.tables.pojos.UserAuthToken;
import io.xj.hub.access.HubAccess;
import io.xj.hub.tables.records.AccountRecord;
import io.xj.hub.tables.records.AccountUserRecord;
import io.xj.hub.tables.records.InstrumentAudioRecord;
import io.xj.hub.tables.records.InstrumentMemeRecord;
import io.xj.hub.tables.records.InstrumentRecord;
import io.xj.hub.tables.records.LibraryRecord;
import io.xj.hub.tables.records.ProgramMemeRecord;
import io.xj.hub.tables.records.ProgramRecord;
import io.xj.hub.tables.records.ProgramSequenceBindingMemeRecord;
import io.xj.hub.tables.records.ProgramSequenceBindingRecord;
import io.xj.hub.tables.records.ProgramSequenceChordRecord;
import io.xj.hub.tables.records.ProgramSequenceChordVoicingRecord;
import io.xj.hub.tables.records.ProgramSequencePatternEventRecord;
import io.xj.hub.tables.records.ProgramSequencePatternRecord;
import io.xj.hub.tables.records.ProgramSequenceRecord;
import io.xj.hub.tables.records.ProgramVoiceRecord;
import io.xj.hub.tables.records.ProgramVoiceTrackRecord;
import io.xj.hub.tables.records.TemplateBindingRecord;
import io.xj.hub.tables.records.TemplatePlaybackRecord;
import io.xj.hub.tables.records.TemplateRecord;
import io.xj.hub.tables.records.UserAuthRecord;
import io.xj.hub.tables.records.UserAuthTokenRecord;
import io.xj.hub.tables.records.UserRecord;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.ValueException;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static io.xj.hub.Tables.ACCOUNT;
import static io.xj.hub.Tables.ACCOUNT_USER;
import static io.xj.hub.Tables.INSTRUMENT;
import static io.xj.hub.Tables.INSTRUMENT_AUDIO;
import static io.xj.hub.Tables.INSTRUMENT_MEME;
import static io.xj.hub.Tables.LIBRARY;
import static io.xj.hub.Tables.PROGRAM_MEME;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_BINDING;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_BINDING_MEME;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_CHORD;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_CHORD_VOICING;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_PATTERN;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_PATTERN_EVENT;
import static io.xj.hub.Tables.PROGRAM_VOICE;
import static io.xj.hub.Tables.PROGRAM_VOICE_TRACK;
import static io.xj.hub.Tables.TEMPLATE;
import static io.xj.hub.Tables.TEMPLATE_BINDING;
import static io.xj.hub.Tables.TEMPLATE_PLAYBACK;
import static io.xj.hub.Tables.USER;
import static io.xj.hub.Tables.USER_AUTH;
import static io.xj.hub.Tables.USER_AUTH_TOKEN;
import static io.xj.hub.tables.Program.PROGRAM;

public interface DAO<E> {
  Map<Class<?>, Table<?>> tablesInSchemaConstructionOrder = ImmutableMap.<Class<?>, Table<?>>builder() // DELIBERATE ORDER
    .put(User.class, USER)
    .put(UserAuth.class, USER_AUTH) // after user
    .put(UserAuthToken.class, USER_AUTH_TOKEN) // after user
    .put(Account.class, ACCOUNT) // after user
    .put(AccountUser.class, ACCOUNT_USER) // after user
    .put(Template.class, TEMPLATE) // after account
    .put(TemplateBinding.class, TEMPLATE_BINDING) // after template
    .put(TemplatePlayback.class, TEMPLATE_PLAYBACK) // after template
    .put(Library.class, LIBRARY) // after account
    .put(Program.class, PROGRAM) // after library
    .put(ProgramMeme.class, PROGRAM_MEME)
    .put(ProgramVoice.class, PROGRAM_VOICE)
    .put(ProgramVoiceTrack.class, PROGRAM_VOICE_TRACK)
    .put(ProgramSequence.class, PROGRAM_SEQUENCE)
    .put(ProgramSequenceBinding.class, PROGRAM_SEQUENCE_BINDING)
    .put(ProgramSequenceBindingMeme.class, PROGRAM_SEQUENCE_BINDING_MEME)
    .put(ProgramSequenceChord.class, PROGRAM_SEQUENCE_CHORD)
    .put(ProgramSequenceChordVoicing.class, PROGRAM_SEQUENCE_CHORD_VOICING)
    .put(ProgramSequencePattern.class, PROGRAM_SEQUENCE_PATTERN)
    .put(ProgramSequencePatternEvent.class, PROGRAM_SEQUENCE_PATTERN_EVENT)
    .put(Instrument.class, INSTRUMENT) // after library
    .put(InstrumentAudio.class, INSTRUMENT_AUDIO)
    .put(InstrumentMeme.class, INSTRUMENT_MEME)
    .build();
  Map<Class<? extends Record>, Class<?>> modelsForRecords = ImmutableMap.<Class<? extends Record>, Class<?>>builder()
    .put(UserRecord.class, User.class)
    .put(UserAuthRecord.class, UserAuth.class)
    .put(UserAuthTokenRecord.class, UserAuthToken.class)
    .put(AccountRecord.class, Account.class)
    .put(AccountUserRecord.class, AccountUser.class)
    .put(TemplateRecord.class, Template.class)
    .put(TemplateBindingRecord.class, TemplateBinding.class)
    .put(TemplatePlaybackRecord.class, TemplatePlayback.class)
    .put(LibraryRecord.class, Library.class)
    .put(ProgramRecord.class, Program.class)
    .put(ProgramMemeRecord.class, ProgramMeme.class)
    .put(ProgramVoiceRecord.class, ProgramVoice.class)
    .put(ProgramVoiceTrackRecord.class, ProgramVoiceTrack.class)
    .put(ProgramSequenceRecord.class, ProgramSequence.class)
    .put(ProgramSequenceBindingRecord.class, ProgramSequenceBinding.class)
    .put(ProgramSequenceBindingMemeRecord.class, ProgramSequenceBindingMeme.class)
    .put(ProgramSequenceChordRecord.class, ProgramSequenceChord.class)
    .put(ProgramSequenceChordVoicingRecord.class, ProgramSequenceChordVoicing.class)
    .put(ProgramSequencePatternRecord.class, ProgramSequencePattern.class)
    .put(ProgramSequencePatternEventRecord.class, ProgramSequencePatternEvent.class)
    .put(InstrumentRecord.class, Instrument.class)
    .put(InstrumentAudioRecord.class, InstrumentAudio.class)
    .put(InstrumentMemeRecord.class, InstrumentMeme.class)
    .build();
  Logger log = LoggerFactory.getLogger(DAO.class);
  Collection<String> nullValueClasses = ImmutableList.of("Null", "JsonNull");

  /**
   ids of a result set

   @param records set
   @return ids
   */
  static Collection<UUID> idsFrom(Result<Record1<UUID>> records) {
    return records.map(Record1::value1);
  }

  /**
   Get DSL context

   @param dataSource SQL dataSource
   @return DSL context
   */
  static DSLContext DSL(DataSource dataSource) {
    return DSL.using(dataSource, SQLDialect.POSTGRES, new Settings());
  }

  /**
   Create a new Record
   <p>
   [#175213519] Expect new Audios to have no waveform

   @param hubAccess control
   @param entity    for the new Record
   @return newly readMany record
   */
  E create(HubAccess hubAccess, E entity) throws DAOException, JsonapiException, ValueException;

  /**
   Delete a specified Entity@param hubAccess control@param id of specific Entity to delete.


   */
  void destroy(HubAccess hubAccess, UUID id) throws DAOException;

  /**
   Create a new instance of this type of Entity

   @return new entity instance
   */
  E newInstance();

  /**
   Fetch many records for many parents by id, if accessible

   @return collection of retrieved records
   @throws DAOException on failure
   @param hubAccess control
   @param parentIds to fetch records for.
   */
  Collection<E> readMany(HubAccess hubAccess, Collection<UUID> parentIds) throws DAOException;

  /**
   Fetch one record  if accessible

   @return retrieved record
   @throws DAOException on failure
   @param hubAccess control
   @param id        of record to fetch
   */
  E readOne(HubAccess hubAccess, UUID id) throws DAOException;

  /**
   Update a specified Entity@param hubAccess control@param id     of specific Entity to update.
   @param entity for the updated Entity.


   */
  E update(HubAccess hubAccess, UUID id, E entity) throws DAOException, JsonapiException, ValueException;

  /**
   Transmogrify a jOOQ Result set into a Collection of POJO entities

   @param modelClass instance of a single target entity
   @param records    to source values of
   @return entity after transmogrification
   @throws DAOException on failure to transmogrify
   */
  <N, R extends Record> Collection<N> modelsFrom(Class<N> modelClass, Iterable<R> records) throws DAOException;

  /**
   Transmogrify the field-value pairs of a jOOQ record and set values on the corresponding POJO entity.

   @param record to source field-values of
   @return entity after transmogrification
   @throws DAOException on failure to transmogrify
   */
  <N, R extends Record> N modelFrom(R record) throws DAOException;

}
