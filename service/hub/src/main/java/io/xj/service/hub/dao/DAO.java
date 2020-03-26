// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.xj.lib.rest_api.RestApiException;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.entity.Entity;
import io.xj.service.hub.model.Account;
import io.xj.service.hub.model.AccountUser;
import io.xj.service.hub.model.Chain;
import io.xj.service.hub.model.ChainBinding;
import io.xj.service.hub.model.ChainConfig;
import io.xj.service.hub.model.Instrument;
import io.xj.service.hub.model.InstrumentAudio;
import io.xj.service.hub.model.InstrumentAudioChord;
import io.xj.service.hub.model.InstrumentAudioEvent;
import io.xj.service.hub.model.InstrumentMeme;
import io.xj.service.hub.model.Library;
import io.xj.service.hub.model.PlatformMessage;
import io.xj.service.hub.model.Program;
import io.xj.service.hub.model.ProgramMeme;
import io.xj.service.hub.model.ProgramSequence;
import io.xj.service.hub.model.ProgramSequenceBinding;
import io.xj.service.hub.model.ProgramSequenceBindingMeme;
import io.xj.service.hub.model.ProgramSequenceChord;
import io.xj.service.hub.model.ProgramSequencePattern;
import io.xj.service.hub.model.ProgramSequencePatternEvent;
import io.xj.service.hub.model.ProgramVoice;
import io.xj.service.hub.model.ProgramVoiceTrack;
import io.xj.service.hub.model.Segment;
import io.xj.service.hub.model.SegmentChoice;
import io.xj.service.hub.model.SegmentChoiceArrangement;
import io.xj.service.hub.model.SegmentChoiceArrangementPick;
import io.xj.service.hub.model.SegmentChord;
import io.xj.service.hub.model.SegmentMeme;
import io.xj.service.hub.model.SegmentMessage;
import io.xj.service.hub.model.User;
import io.xj.service.hub.model.UserAuth;
import io.xj.service.hub.model.UserAuthToken;
import io.xj.service.hub.model.UserRole;
import io.xj.service.hub.tables.records.AccountRecord;
import io.xj.service.hub.tables.records.AccountUserRecord;
import io.xj.service.hub.tables.records.ChainBindingRecord;
import io.xj.service.hub.tables.records.ChainConfigRecord;
import io.xj.service.hub.tables.records.ChainRecord;
import io.xj.service.hub.tables.records.InstrumentAudioChordRecord;
import io.xj.service.hub.tables.records.InstrumentAudioEventRecord;
import io.xj.service.hub.tables.records.InstrumentAudioRecord;
import io.xj.service.hub.tables.records.InstrumentMemeRecord;
import io.xj.service.hub.tables.records.InstrumentRecord;
import io.xj.service.hub.tables.records.LibraryRecord;
import io.xj.service.hub.tables.records.PlatformMessageRecord;
import io.xj.service.hub.tables.records.ProgramMemeRecord;
import io.xj.service.hub.tables.records.ProgramRecord;
import io.xj.service.hub.tables.records.ProgramSequenceBindingMemeRecord;
import io.xj.service.hub.tables.records.ProgramSequenceBindingRecord;
import io.xj.service.hub.tables.records.ProgramSequenceChordRecord;
import io.xj.service.hub.tables.records.ProgramSequencePatternEventRecord;
import io.xj.service.hub.tables.records.ProgramSequencePatternRecord;
import io.xj.service.hub.tables.records.ProgramSequenceRecord;
import io.xj.service.hub.tables.records.ProgramVoiceRecord;
import io.xj.service.hub.tables.records.ProgramVoiceTrackRecord;
import io.xj.service.hub.tables.records.SegmentChoiceArrangementPickRecord;
import io.xj.service.hub.tables.records.SegmentChoiceArrangementRecord;
import io.xj.service.hub.tables.records.SegmentChoiceRecord;
import io.xj.service.hub.tables.records.SegmentChordRecord;
import io.xj.service.hub.tables.records.SegmentMemeRecord;
import io.xj.service.hub.tables.records.SegmentMessageRecord;
import io.xj.service.hub.tables.records.SegmentRecord;
import io.xj.service.hub.tables.records.UserAuthRecord;
import io.xj.service.hub.tables.records.UserAuthTokenRecord;
import io.xj.service.hub.tables.records.UserRecord;
import io.xj.service.hub.tables.records.UserRoleRecord;
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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.xj.service.hub.Tables.ACCOUNT;
import static io.xj.service.hub.Tables.ACCOUNT_USER;
import static io.xj.service.hub.Tables.CHAIN;
import static io.xj.service.hub.Tables.CHAIN_BINDING;
import static io.xj.service.hub.Tables.CHAIN_CONFIG;
import static io.xj.service.hub.Tables.INSTRUMENT;
import static io.xj.service.hub.Tables.INSTRUMENT_AUDIO;
import static io.xj.service.hub.Tables.INSTRUMENT_AUDIO_CHORD;
import static io.xj.service.hub.Tables.INSTRUMENT_AUDIO_EVENT;
import static io.xj.service.hub.Tables.INSTRUMENT_MEME;
import static io.xj.service.hub.Tables.LIBRARY;
import static io.xj.service.hub.Tables.PLATFORM_MESSAGE;
import static io.xj.service.hub.Tables.PROGRAM_MEME;
import static io.xj.service.hub.Tables.PROGRAM_SEQUENCE;
import static io.xj.service.hub.Tables.PROGRAM_SEQUENCE_BINDING;
import static io.xj.service.hub.Tables.PROGRAM_SEQUENCE_BINDING_MEME;
import static io.xj.service.hub.Tables.PROGRAM_SEQUENCE_CHORD;
import static io.xj.service.hub.Tables.PROGRAM_SEQUENCE_PATTERN;
import static io.xj.service.hub.Tables.PROGRAM_SEQUENCE_PATTERN_EVENT;
import static io.xj.service.hub.Tables.PROGRAM_VOICE;
import static io.xj.service.hub.Tables.PROGRAM_VOICE_TRACK;
import static io.xj.service.hub.Tables.SEGMENT;
import static io.xj.service.hub.Tables.SEGMENT_CHOICE;
import static io.xj.service.hub.Tables.SEGMENT_CHOICE_ARRANGEMENT;
import static io.xj.service.hub.Tables.SEGMENT_CHOICE_ARRANGEMENT_PICK;
import static io.xj.service.hub.Tables.SEGMENT_CHORD;
import static io.xj.service.hub.Tables.SEGMENT_MEME;
import static io.xj.service.hub.Tables.SEGMENT_MESSAGE;
import static io.xj.service.hub.Tables.USER;
import static io.xj.service.hub.Tables.USER_AUTH;
import static io.xj.service.hub.Tables.USER_AUTH_TOKEN;
import static io.xj.service.hub.Tables.USER_ROLE;
import static io.xj.service.hub.tables.Program.PROGRAM;

public interface DAO<E> {

  Map<Class<?>, Table<?>> tablesInSchemaConstructionOrder = ImmutableMap.<Class<?>, Table<?>>builder() // DELIBERATE ORDER
    .put(User.class, USER)
    .put(UserRole.class, USER_ROLE) // after user
    .put(UserAuth.class, USER_AUTH) // after user
    .put(UserAuthToken.class, USER_AUTH_TOKEN) // after user
    .put(Account.class, ACCOUNT) // after user
    .put(AccountUser.class, ACCOUNT_USER) // after user
    .put(Library.class, LIBRARY) // after account
    .put(Program.class, PROGRAM) // after library
    .put(ProgramMeme.class, PROGRAM_MEME)
    .put(ProgramVoice.class, PROGRAM_VOICE)
    .put(ProgramVoiceTrack.class, PROGRAM_VOICE_TRACK)
    .put(ProgramSequence.class, PROGRAM_SEQUENCE)
    .put(ProgramSequenceBinding.class, PROGRAM_SEQUENCE_BINDING)
    .put(ProgramSequenceBindingMeme.class, PROGRAM_SEQUENCE_BINDING_MEME)
    .put(ProgramSequenceChord.class, PROGRAM_SEQUENCE_CHORD)
    .put(ProgramSequencePattern.class, PROGRAM_SEQUENCE_PATTERN)
    .put(ProgramSequencePatternEvent.class, PROGRAM_SEQUENCE_PATTERN_EVENT)
    .put(Instrument.class, INSTRUMENT) // after library
    .put(InstrumentAudio.class, INSTRUMENT_AUDIO)
    .put(InstrumentAudioChord.class, INSTRUMENT_AUDIO_CHORD)
    .put(InstrumentAudioEvent.class, INSTRUMENT_AUDIO_EVENT)
    .put(InstrumentMeme.class, INSTRUMENT_MEME)
    .put(Chain.class, CHAIN) // after account
    .put(ChainBinding.class, CHAIN_BINDING) // after chain, library
    .put(ChainConfig.class, CHAIN_CONFIG) // after chain
    .put(Segment.class, SEGMENT) // after chain
    .put(SegmentChord.class, SEGMENT_CHORD) // after segment
    .put(SegmentMeme.class, SEGMENT_MEME) // after segment
    .put(SegmentMessage.class, SEGMENT_MESSAGE) // after segment
    .put(SegmentChoice.class, SEGMENT_CHOICE) // after segment
    .put(SegmentChoiceArrangement.class, SEGMENT_CHOICE_ARRANGEMENT) // after segment choice
    .put(SegmentChoiceArrangementPick.class, SEGMENT_CHOICE_ARRANGEMENT_PICK) // after segment arrangement
    .put(PlatformMessage.class, PLATFORM_MESSAGE)
    .build();
  Map<Class<? extends Record>, Class<? extends Entity>> modelsForRecords = ImmutableMap.<Class<? extends Record>, Class<? extends Entity>>builder()
    .put(UserRecord.class, User.class)
    .put(UserRoleRecord.class, UserRole.class)
    .put(UserAuthRecord.class, UserAuth.class)
    .put(UserAuthTokenRecord.class, UserAuthToken.class)
    .put(AccountRecord.class, Account.class)
    .put(AccountUserRecord.class, AccountUser.class)
    .put(LibraryRecord.class, Library.class)
    .put(ProgramRecord.class, Program.class)
    .put(ProgramMemeRecord.class, ProgramMeme.class)
    .put(ProgramVoiceRecord.class, ProgramVoice.class)
    .put(ProgramVoiceTrackRecord.class, ProgramVoiceTrack.class)
    .put(ProgramSequenceRecord.class, ProgramSequence.class)
    .put(ProgramSequenceBindingRecord.class, ProgramSequenceBinding.class)
    .put(ProgramSequenceBindingMemeRecord.class, ProgramSequenceBindingMeme.class)
    .put(ProgramSequenceChordRecord.class, ProgramSequenceChord.class)
    .put(ProgramSequencePatternRecord.class, ProgramSequencePattern.class)
    .put(ProgramSequencePatternEventRecord.class, ProgramSequencePatternEvent.class)
    .put(InstrumentRecord.class, Instrument.class)
    .put(InstrumentAudioRecord.class, InstrumentAudio.class)
    .put(InstrumentAudioChordRecord.class, InstrumentAudioChord.class)
    .put(InstrumentAudioEventRecord.class, InstrumentAudioEvent.class)
    .put(InstrumentMemeRecord.class, InstrumentMeme.class)
    .put(ChainRecord.class, Chain.class)
    .put(ChainBindingRecord.class, ChainBinding.class)
    .put(ChainConfigRecord.class, ChainConfig.class)
    .put(SegmentRecord.class, Segment.class)
    .put(SegmentChordRecord.class, SegmentChord.class)
    .put(SegmentMemeRecord.class, SegmentMeme.class)
    .put(SegmentMessageRecord.class, SegmentMessage.class)
    .put(SegmentChoiceRecord.class, SegmentChoice.class)
    .put(SegmentChoiceArrangementRecord.class, SegmentChoiceArrangement.class)
    .put(SegmentChoiceArrangementPickRecord.class, SegmentChoiceArrangementPick.class)
    .put(PlatformMessageRecord.class, PlatformMessage.class)
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
   ids of an entity set

   @param entities to get ids of
   @return ids
   */
  static <N extends Entity> Set<UUID> idsFrom(Collection<N> entities) {
    return entities.stream()
      .map(Entity::getId)
      .collect(Collectors.toSet());
  }

  /**
   Create a new Record

   @param access control
   @param entity for the new Record
   @return newly readMany record
   */
  E create(Access access, E entity) throws HubException, RestApiException, ValueException;

  /**
   Create many new records.
   There's a default implementation of this, the slowest possible version, iterating over records and adding them.
   Override that for classes that clearly benefit of batch writing. (*ahem* segment entities)@param access

   @param entities to of many of
   */
  void createMany(Access access, Collection<E> entities) throws HubException, RestApiException, ValueException;

  /**
   Delete a specified Entity@param access control

   @param id of specific Entity to delete.
   */
  void destroy(Access access, UUID id) throws HubException;

  /**
   Create a new instance of this type of Entity

   @return new entity instance
   */
  E newInstance();

  /**
   Fetch many records for many parents by id, if accessible

   @param access    control
   @param parentIds to fetch records for.
   @return collection of retrieved records
   @throws HubException on failure
   */
  Collection<E> readMany(Access access, Collection<UUID> parentIds) throws HubException;

  /**
   Fetch one record  if accessible

   @param access control
   @param id     of record to fetch
   @return retrieved record
   @throws HubException on failure
   */
  E readOne(Access access, UUID id) throws HubException;

  /**
   Update a specified Entity@param access control

   @param id     of specific Entity to update.
   @param entity for the updated Entity.
   */
  void update(Access access, UUID id, E entity) throws HubException, RestApiException, ValueException;

  /**
   Transmogrify a jOOQ Result set into a Collection of POJO entities

   @param modelClass instance of a single target entity
   @param records    to source values of
   @return entity after transmogrification
   @throws HubException on failure to transmogrify
   */
  <N extends Entity, R extends Record> Collection<N> modelsFrom(Class<N> modelClass, Iterable<R> records) throws HubException;

  /**
   Transmogrify the field-value pairs of a jOOQ record and set values on the corresponding POJO entity.

   @param record to source field-values of
   @return entity after transmogrification
   @throws HubException on failure to transmogrify
   */
  <N extends Entity, R extends Record> N modelFrom(R record) throws HubException;
}
