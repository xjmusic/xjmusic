// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.AudioChordDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.exception.CoreException;
import io.xj.core.model.audio_chord.AudioChord;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;
import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

import static io.xj.core.Tables.AUDIO;
import static io.xj.core.Tables.AUDIO_CHORD;
import static io.xj.core.Tables.INSTRUMENT;
import static io.xj.core.Tables.LIBRARY;

public class AudioChordDAOImpl extends DAOImpl implements AudioChordDAO {

  @Inject
  public AudioChordDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  /**
   Create a new Audio Chord

   @param db     context
   @param access control
   @param entity for new audio
   @return newly readMany record
   @throws CoreException if failure
   */
  private static AudioChord createRecord(DSLContext db, Access access, AudioChord entity) throws CoreException {
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

    return modelFrom(executeCreate(db, AUDIO_CHORD, fieldValues), AudioChord.class);
  }

  /**
   Read one Chord if able

   @param db     context
   @param access control
   @param id     of audio
   @return audio
   */
  private static AudioChord readOneRecord(DSLContext db, Access access, ULong id) throws CoreException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(AUDIO_CHORD)
        .where(AUDIO_CHORD.ID.eq(id))
        .fetchOne(), AudioChord.class);
    else
      return modelFrom(db.select(AUDIO_CHORD.fields())
        .from(AUDIO_CHORD)
        .join(AUDIO).on(AUDIO.ID.eq(AUDIO_CHORD.AUDIO_ID))
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(AUDIO_CHORD.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), AudioChord.class);
  }

  /**
   Read all Chord able for an Instrument

   @param db       context
   @param access   control
   @param audioIds to readMany all audio of
   @return array of audios
   */
  private static Collection<AudioChord> readAll(DSLContext db, Access access, Collection<ULong> audioIds) throws CoreException {
    if (access.isTopLevel())
      return modelsFrom(db.select(AUDIO_CHORD.fields())
        .from(AUDIO_CHORD)
        .where(AUDIO_CHORD.AUDIO_ID.in(audioIds))
        .orderBy(AUDIO_CHORD.POSITION)
        .fetch(), AudioChord.class);
    else
      return modelsFrom(db.select(AUDIO_CHORD.fields())
        .from(AUDIO_CHORD)
        .join(AUDIO).on(AUDIO.ID.eq(AUDIO_CHORD.AUDIO_ID))
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(AUDIO_CHORD.AUDIO_ID.in(audioIds))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(AUDIO_CHORD.POSITION)
        .fetch(), AudioChord.class);
  }

  /**
   Update a Chord record

   @param db     context
   @param access control
   @param id     to update
   @param entity to update with
   @throws CoreException if failure
   */
  private static void update(DSLContext db, Access access, ULong id, AudioChord entity) throws CoreException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);
    fieldValues.put(AUDIO_CHORD.ID, id);

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

    if (0 == executeUpdate(db, AUDIO_CHORD, fieldValues))
      throw new CoreException("No records updated.");
  }

  /**
   Delete an Chord

   @param db context
   @param id to delete
   @throws CoreException         if database failure
   @throws CoreException   if not configured properly
   @throws CoreException if fails business rule
   */
  private static void delete(Access access, DSLContext db, ULong id) throws CoreException {
    if (!access.isTopLevel())
      requireExists("Audio Chord", db.selectCount().from(AUDIO_CHORD)
        .join(AUDIO).on(AUDIO.ID.eq(AUDIO_CHORD.AUDIO_ID))
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(AUDIO_CHORD.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    db.deleteFrom(AUDIO_CHORD)
      .where(AUDIO_CHORD.ID.eq(id))
      .execute();
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(AudioChord entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(AUDIO_CHORD.NAME, entity.getName());
    fieldValues.put(AUDIO_CHORD.AUDIO_ID, ULong.valueOf(entity.getAudioId()));
    fieldValues.put(AUDIO_CHORD.POSITION, entity.getPosition());
    return fieldValues;
  }

  @Override
  public AudioChord create(Access access, AudioChord entity) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public AudioChord readOne(Access access, BigInteger id) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneRecord(tx.getContext(), access, ULong.valueOf(id)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Collection<AudioChord> readAll(Access access, Collection<BigInteger> parentIds) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, uLongValuesOf(parentIds)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, AudioChord entity) throws CoreException {
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
