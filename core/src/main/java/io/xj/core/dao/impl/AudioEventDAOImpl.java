// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.dao.AudioEventDAO;
import io.xj.core.persistence.sql.impl.SQLConnection;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.model.audio_event.AudioEvent;
import io.xj.core.tables.records.AudioEventRecord;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.List;
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
  public AudioEventRecord create(Access access, AudioEvent entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public AudioEventRecord readOne(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Result<AudioEventRecord> readAll(Access access, ULong audioId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, audioId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public List<AudioEvent> readAllFirstEventsForInstrument(Access access, ULong instrumentId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {

      return tx.success(readAllFirstEventsForInstrument(tx.getContext(), access, instrumentId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, ULong id, AudioEvent entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, id, entity);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(access, tx.getContext(), id);
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
  private AudioEventRecord createRecord(DSLContext db, Access access, AudioEvent entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    if (access.isTopLevel())
      requireExists("Audio", db.selectCount().from(AUDIO)
        .where(AUDIO.ID.eq(entity.getAudioId()))
        .fetchOne(0, int.class));
    else
      requireExists("Audio", db.selectCount().from(AUDIO)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .and(AUDIO.ID.eq(entity.getAudioId()))
        .fetchOne(0, int.class));

    return executeCreate(db, AUDIO_EVENT, fieldValues);
  }

  /**
   Read one Event if able

   @param db     context
   @param access control
   @param id     of audio
   @return audio
   */
  private AudioEventRecord readOne(DSLContext db, Access access, ULong id) {
    if (access.isTopLevel())
      return db.selectFrom(AUDIO_EVENT)
        .where(AUDIO_EVENT.ID.eq(id))
        .fetchOne();
    else
      return recordInto(AUDIO_EVENT, db.select(AUDIO_EVENT.fields())
        .from(AUDIO_EVENT)
        .join(AUDIO).on(AUDIO.ID.eq(AUDIO_EVENT.AUDIO_ID))
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(AUDIO_EVENT.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
  }

  /**
   Read all Event able for an Instrument

   @param db      context
   @param access  control
   @param audioId to readMany all audio of
   @return array of audios
   @throws SQLException on failure
   */
  private Result<AudioEventRecord> readAll(DSLContext db, Access access, ULong audioId) throws SQLException {
    if (access.isTopLevel())
      return resultInto(AUDIO_EVENT, db.select(AUDIO_EVENT.fields())
        .from(AUDIO_EVENT)
        .where(AUDIO_EVENT.AUDIO_ID.eq(audioId))
        .orderBy(AUDIO_EVENT.POSITION)
        .fetch());
    else
      return resultInto(AUDIO_EVENT, db.select(AUDIO_EVENT.fields())
        .from(AUDIO_EVENT)
        .join(AUDIO).on(AUDIO.ID.eq(AUDIO_EVENT.AUDIO_ID))
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(AUDIO_EVENT.AUDIO_ID.eq(audioId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .orderBy(AUDIO_EVENT.POSITION)
        .fetch());
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
  private List<AudioEvent> readAllFirstEventsForInstrument(DSLContext db, Access access, ULong instrumentId) throws Exception {
    requireTopLevel(access);

    // for each audio id, the first (in terms of position) AudioEvent
    Map<ULong, AudioEvent> audioFirstEvents = Maps.newHashMap();

    // HAVEN'T BEEN ABLE TO GET ANYTHING MORE EFFICIENT TO WORK
    Consumer<? super Record> putIfEarlierThanExisting = audioEventRecord -> {
      ULong audioId = audioEventRecord.get(AUDIO_EVENT.AUDIO_ID);

      // for each AudioEvent, if not seen or earlier than what has been seen, store as result for that audio id
      if (!audioFirstEvents.containsKey(audioId) ||
        audioEventRecord.get(AUDIO_EVENT.POSITION) < audioFirstEvents.get(audioId).getPosition())
        audioFirstEvents.put(audioId, new AudioEvent().setFromRecord(audioEventRecord));
    };

    // just fetch all the AudioEvent records and filter
    db.select(AUDIO_EVENT.fields())
      .from(AUDIO_EVENT)
      .join(AUDIO).on(AUDIO.ID.eq(AUDIO_EVENT.AUDIO_ID))
      .where(AUDIO.INSTRUMENT_ID.eq(instrumentId))
      .fetch()
      .forEach(putIfEarlierThanExisting);

    List<AudioEvent> audioEvents = Lists.newArrayList();
    audioFirstEvents.forEach((id, audioEvent) -> audioEvents.add(audioEvent));
    return audioEvents;
  }

  /**
   Update a Event record

   @param db     context
   @param access control
   @param id     to update
   @param entity to update with
   @throws BusinessException if failure
   */
  private void update(DSLContext db, Access access, ULong id, AudioEvent entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();
    fieldValues.put(AUDIO_EVENT.ID, id);

    if (access.isTopLevel())
      requireExists("Audio", db.selectCount().from(AUDIO)
        .where(AUDIO.ID.eq(entity.getAudioId()))
        .fetchOne(0, int.class));
    else
      requireExists("Audio", db.selectCount().from(AUDIO)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .and(AUDIO.ID.eq(entity.getAudioId()))
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
  private void delete(Access access, DSLContext db, ULong id) throws Exception {
    if (!access.isTopLevel())
      requireExists("Audio Meme", db.selectCount().from(AUDIO_EVENT)
        .join(AUDIO).on(AUDIO.ID.eq(AUDIO_EVENT.AUDIO_ID))
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(AUDIO_EVENT.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne(0, int.class));

    db.deleteFrom(AUDIO_EVENT)
      .where(AUDIO_EVENT.ID.eq(id))
      .execute();
  }

}
