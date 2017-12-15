// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.AudioEventDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.audio_event.AudioEvent;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;
import io.xj.core.tables.records.AudioEventRecord;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

import static io.xj.core.Tables.AUDIO;
import static io.xj.core.Tables.AUDIO_EVENT;
import static io.xj.core.tables.Instrument.INSTRUMENT;
import static io.xj.core.tables.Library.LIBRARY;

public class AudioEventDAOImpl extends DAOImpl implements AudioEventDAO {

  @Inject
  public AudioEventDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public AudioEvent create(Access access, AudioEvent entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public AudioEvent readOne(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Collection<AudioEvent> readAll(Access access, BigInteger audioId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, ULong.valueOf(audioId)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<AudioEvent> readAllFirstEventsForInstrument(Access access, BigInteger instrumentId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {

      return tx.success(readAllFirstEventsForInstrument(tx.getContext(), access, ULong.valueOf(instrumentId)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, AudioEvent entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, ULong.valueOf(id), entity);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(access, tx.getContext(), ULong.valueOf(id));
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  /**
   Create a new Audio Event

   @param db     context
   @param access control
   @param entity for new audio
   @return newly readMany record
   @throws BusinessException if failure
   */
  private static AudioEvent createRecord(DSLContext db, Access access, AudioEvent entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    if (access.isTopLevel())
      requireExists("Audio", db.selectCount().from(AUDIO)
        .where(AUDIO.ID.eq(ULong.valueOf(entity.getAudioId())))
        .fetchOne(0, int.class));
    else
      requireExists("Audio", db.selectCount().from(AUDIO)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .and(AUDIO.ID.eq(ULong.valueOf(entity.getAudioId())))
        .fetchOne(0, int.class));

    return modelFrom(executeCreate(db, AUDIO_EVENT, fieldValues), AudioEvent.class);
  }

  /**
   Read one Event if able

   @param db     context
   @param access control
   @param id     of audio
   @return audio
   */
  private static AudioEvent readOne(DSLContext db, Access access, ULong id) throws BusinessException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(AUDIO_EVENT)
        .where(AUDIO_EVENT.ID.eq(id))
        .fetchOne(), AudioEvent.class);
    else
      return modelFrom(db.select(AUDIO_EVENT.fields())
        .from(AUDIO_EVENT)
        .join(AUDIO).on(AUDIO.ID.eq(AUDIO_EVENT.AUDIO_ID))
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(AUDIO_EVENT.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), AudioEvent.class);
  }

  /**
   Read all Event able for an Instrument

   @param db      context
   @param access  control
   @param audioId to readMany all audio of
   @return array of audios
   */
  private static Collection<AudioEvent> readAll(DSLContext db, Access access, ULong audioId) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.select(AUDIO_EVENT.fields())
        .from(AUDIO_EVENT)
        .where(AUDIO_EVENT.AUDIO_ID.eq(audioId))
        .orderBy(AUDIO_EVENT.POSITION)
        .fetch(), AudioEvent.class);
    else
      return modelsFrom(db.select(AUDIO_EVENT.fields())
        .from(AUDIO_EVENT)
        .join(AUDIO).on(AUDIO.ID.eq(AUDIO_EVENT.AUDIO_ID))
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(AUDIO_EVENT.AUDIO_ID.eq(audioId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(AUDIO_EVENT.POSITION)
        .fetch(), AudioEvent.class);
  }

  /**
   Read all AudioEvent that are first in an audio, for all audio in an Instrument
   for each audio id, the first (in terms of position) AudioEvent

   @param db           context
   @param access       control
   @param instrumentId to readMany all audio of
   @return array of audios
   @throws SQLException on failure
   */
  private static Collection<AudioEvent> readAllFirstEventsForInstrument(DSLContext db, Access access, ULong instrumentId) throws Exception {
    requireTopLevel(access);

    // for each audio id, the first (in terms of position) AudioEvent
    Map<ULong, AudioEventRecord> audioFirstEventRecords = Maps.newHashMap();

    // HAVEN'T BEEN ABLE TO GET ANYTHING MORE EFFICIENT TO WORK
    Consumer<? super AudioEventRecord> putIfEarlierThanExisting = audioEventRecord -> {
      ULong audioId = audioEventRecord.get(AUDIO_EVENT.AUDIO_ID);

      // for each AudioEvent, if not seen or earlier than what has been seen, store as result for that audio id
      if (!audioFirstEventRecords.containsKey(audioId) ||
        audioEventRecord.get(AUDIO_EVENT.POSITION) < audioFirstEventRecords.get(audioId).getPosition())
        audioFirstEventRecords.put(audioId, audioEventRecord);
    };

    // just fetch all the AudioEvent records and filter
    db.select(AUDIO_EVENT.fields())
      .from(AUDIO_EVENT)
      .join(AUDIO).on(AUDIO.ID.eq(AUDIO_EVENT.AUDIO_ID))
      .where(AUDIO.INSTRUMENT_ID.eq(instrumentId))
      .fetch()
      .into(AUDIO_EVENT)
      .forEach(putIfEarlierThanExisting);

    Collection<AudioEventRecord> result = Lists.newArrayList();
    audioFirstEventRecords.forEach((key, val) -> result.add(val));
    return modelsFrom(result, AudioEvent.class);
  }

  /**
   Update a Event record

   @param db     context
   @param access control
   @param id     to update
   @param entity to update with
   @throws BusinessException if failure
   */
  private static void update(DSLContext db, Access access, ULong id, AudioEvent entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);
    fieldValues.put(AUDIO_EVENT.ID, id);

    if (access.isTopLevel())
      requireExists("Audio", db.selectCount().from(AUDIO)
        .where(AUDIO.ID.eq(ULong.valueOf(entity.getAudioId())))
        .fetchOne(0, int.class));
    else
      requireExists("Audio", db.selectCount().from(AUDIO)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .and(AUDIO.ID.eq(ULong.valueOf(entity.getAudioId())))
        .fetchOne(0, int.class));

    if (0 == executeUpdate(db, AUDIO_EVENT, fieldValues))
      throw new BusinessException("No records updated.");
  }

  /**
   Delete an Event

   @param db context
   @param id to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private static void delete(Access access, DSLContext db, ULong id) throws Exception {
    if (!access.isTopLevel())
      requireExists("Audio Meme", db.selectCount().from(AUDIO_EVENT)
        .join(AUDIO).on(AUDIO.ID.eq(AUDIO_EVENT.AUDIO_ID))
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(AUDIO_EVENT.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    db.deleteFrom(AUDIO_EVENT)
      .where(AUDIO_EVENT.ID.eq(id))
      .execute();
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(AudioEvent entity) {
    Map<Field, Object> fieldValues = com.google.api.client.util.Maps.newHashMap();
    fieldValues.put(AUDIO_EVENT.DURATION, entity.getDuration());
    fieldValues.put(AUDIO_EVENT.INFLECTION, entity.getInflection());
    fieldValues.put(AUDIO_EVENT.NOTE, entity.getNote());
    fieldValues.put(AUDIO_EVENT.POSITION, entity.getPosition());
    fieldValues.put(AUDIO_EVENT.TONALITY, entity.getTonality());
    fieldValues.put(AUDIO_EVENT.VELOCITY, entity.getVelocity());
    fieldValues.put(AUDIO_EVENT.AUDIO_ID, ULong.valueOf(entity.getAudioId()));
    return fieldValues;
  }


}
