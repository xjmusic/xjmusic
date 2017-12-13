// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.dao.AudioChordDAO;
import io.xj.core.persistence.sql.impl.SQLConnection;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.model.audio_chord.AudioChord;
import io.xj.core.tables.records.AudioChordRecord;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.sql.SQLException;
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

  @Override
  public AudioChordRecord create(Access access, AudioChord entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public AudioChordRecord readOne(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneRecord(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Result<AudioChordRecord> readAll(Access access, ULong audioId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, audioId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, ULong id, AudioChord entity) throws Exception {
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
   Create a new Audio Chord

   @param db     context
   @param access control
   @param entity for new audio
   @return newly readMany record
   @throws BusinessException if failure
   */
  private AudioChordRecord createRecord(DSLContext db, Access access, AudioChord entity) throws BusinessException {
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

    return executeCreate(db, AUDIO_CHORD, fieldValues);
  }

  /**
   Read one Chord if able

   @param db     context
   @param access control
   @param id     of audio
   @return audio
   */
  private AudioChordRecord readOneRecord(DSLContext db, Access access, ULong id) {
    if (access.isTopLevel())
      return db.selectFrom(AUDIO_CHORD)
        .where(AUDIO_CHORD.ID.eq(id))
        .fetchOne();
    else
      return recordInto(AUDIO_CHORD, db.select(AUDIO_CHORD.fields())
        .from(AUDIO_CHORD)
        .join(AUDIO).on(AUDIO.ID.eq(AUDIO_CHORD.AUDIO_ID))
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(AUDIO_CHORD.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
  }

  /**
   Read all Chord able for an Instrument

   @param db      context
   @param access  control
   @param audioId to readMany all audio of
   @return array of audios
   @throws SQLException on failure
   */
  private Result<AudioChordRecord> readAll(DSLContext db, Access access, ULong audioId) throws SQLException {
    if (access.isTopLevel())
      return resultInto(AUDIO_CHORD, db.select(AUDIO_CHORD.fields())
        .from(AUDIO_CHORD)
        .where(AUDIO_CHORD.AUDIO_ID.eq(audioId))
        .orderBy(AUDIO_CHORD.POSITION)
        .fetch());
    else
      return resultInto(AUDIO_CHORD, db.select(AUDIO_CHORD.fields())
        .from(AUDIO_CHORD)
        .join(AUDIO).on(AUDIO.ID.eq(AUDIO_CHORD.AUDIO_ID))
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(AUDIO_CHORD.AUDIO_ID.eq(audioId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .orderBy(AUDIO_CHORD.POSITION)
        .fetch());
  }

  /**
   Update a Chord record

   @param db     context
   @param access control
   @param id     to update
   @param entity to update with
   @throws BusinessException if failure
   */
  private void update(DSLContext db, Access access, ULong id, AudioChord entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();
    fieldValues.put(AUDIO_CHORD.ID, id);

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

    if (executeUpdate(db, AUDIO_CHORD, fieldValues) == 0)
      throw new BusinessException("No records updated.");
  }

  /**
   Delete an Chord

   @param db context
   @param id to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private void delete(Access access, DSLContext db, ULong id) throws Exception {
    if (!access.isTopLevel())
      requireExists("Audio Meme", db.selectCount().from(AUDIO_CHORD)
        .join(AUDIO).on(AUDIO.ID.eq(AUDIO_CHORD.AUDIO_ID))
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(AUDIO_CHORD.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne(0, int.class));

    db.deleteFrom(AUDIO_CHORD)
      .where(AUDIO_CHORD.ID.eq(id))
      .execute();
  }

}
