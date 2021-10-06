// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.xj.hub.access.HubAccess;
import io.xj.hub.tables.pojos.User;
import io.xj.hub.tables.pojos.*;
import io.xj.hub.tables.records.*;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.ValueException;
import org.jooq.Record;
import org.jooq.*;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static io.xj.hub.Tables.*;

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
    .put(ProgramAuthorship.class, PROGRAM_AUTHORSHIP)
    .put(ProgramMessage.class, PROGRAM_MESSAGE)
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
    .put(InstrumentAuthorship.class, INSTRUMENT_AUTHORSHIP)
    .put(InstrumentMessage.class, INSTRUMENT_MESSAGE)
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
    .put(ProgramAuthorshipRecord.class, ProgramAuthorship.class)
    .put(ProgramMessageRecord.class, ProgramMessage.class)
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
    .put(InstrumentAuthorshipRecord.class, InstrumentAuthorship.class)
    .put(InstrumentMessageRecord.class, InstrumentMessage.class)
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
