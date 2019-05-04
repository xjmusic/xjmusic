// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.AudioEventDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.audio_event.AudioEvent;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

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

  /**
   Create a new Audio Event

   @param db     context
   @param access control
   @param entity for new audio
   @return newly readMany record
   @throws CoreException if failure
   */
  private static AudioEvent createRecord(DSLContext db, Access access, AudioEvent entity) throws CoreException {
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
  private static AudioEvent readOne(DSLContext db, Access access, ULong id) throws CoreException {
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
   Read all Event for an audio

   @param db       context
   @param access   control
   @param audioIds of which to read events
   @return array of audios
   */
  private static Collection<AudioEvent> readAll(DSLContext db, Access access, Collection<ULong> audioIds) throws CoreException {
    if (access.isTopLevel())
      return modelsFrom(db.select(AUDIO_EVENT.fields())
        .from(AUDIO_EVENT)
        .where(AUDIO_EVENT.AUDIO_ID.in(audioIds))
        .orderBy(AUDIO_EVENT.POSITION)
        .fetch(), AudioEvent.class);
    else
      return modelsFrom(db.select(AUDIO_EVENT.fields())
        .from(AUDIO_EVENT)
        .join(AUDIO).on(AUDIO.ID.eq(AUDIO_EVENT.AUDIO_ID))
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(AUDIO_EVENT.AUDIO_ID.in(audioIds))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(AUDIO_EVENT.POSITION)
        .fetch(), AudioEvent.class);
  }

  /**
   Read all audio event for an Instrument
   <p>
   Supports [#161197150] Developer wants to request all audioEvent for a specified instrument id, for efficiency loading an entire instrument.

   @param db            context
   @param access        control
   @param instrumentIds of which to read audio events
   @return array of audios
   */
  private static Collection<AudioEvent> readAllOfInstrument(DSLContext db, Access access, Collection<ULong> instrumentIds) throws CoreException {
    if (access.isTopLevel())
      return modelsFrom(db.select(AUDIO_EVENT.fields())
        .from(AUDIO_EVENT)
        .join(AUDIO).on(AUDIO.ID.eq(AUDIO_EVENT.AUDIO_ID))
        .where(AUDIO.INSTRUMENT_ID.in(instrumentIds))
        .orderBy(AUDIO_EVENT.POSITION)
        .fetch(), AudioEvent.class);
    else
      return modelsFrom(db.select(AUDIO_EVENT.fields())
        .from(AUDIO_EVENT)
        .join(AUDIO).on(AUDIO.ID.eq(AUDIO_EVENT.AUDIO_ID))
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(AUDIO.INSTRUMENT_ID.in(instrumentIds))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(AUDIO_EVENT.POSITION)
        .fetch(), AudioEvent.class);
  }

  /**
   Update a Event record

   @param db     context
   @param access control
   @param id     to update
   @param entity to update with
   @throws CoreException if failure
   */
  private static void update(DSLContext db, Access access, ULong id, AudioEvent entity) throws CoreException {
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
      throw new CoreException("No records updated.");
  }

  /**
   Delete an Event

   @param db context
   @param id to delete
   @throws CoreException if database failure
   @throws CoreException if not configured properly
   @throws CoreException if fails business rule
   */
  private static void delete(Access access, DSLContext db, ULong id) throws CoreException {
    if (!access.isTopLevel())
      requireExists("Audio Event", db.selectCount().from(AUDIO_EVENT)
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

  @Override
  public AudioEvent create(Access access, AudioEvent entity) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public AudioEvent readOne(Access access, BigInteger id) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Collection<AudioEvent> readAll(Access access, Collection<BigInteger> parentIds) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, uLongValuesOf(parentIds)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<AudioEvent> readAllOfInstrument(Access access, ImmutableList<BigInteger> instrumentIds) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllOfInstrument(tx.getContext(), access, uLongValuesOf(instrumentIds)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, AudioEvent entity) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, ULong.valueOf(id), entity);
      tx.success();
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void destroy(Access access, BigInteger id) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(access, tx.getContext(), ULong.valueOf(id));
      tx.success();
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }


}
