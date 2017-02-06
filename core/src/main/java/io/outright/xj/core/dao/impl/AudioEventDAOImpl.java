// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.dao.AudioEventDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.audio_event.AudioEventWrapper;
import io.outright.xj.core.tables.Audio;
import io.outright.xj.core.tables.records.AudioEventRecord;
import io.outright.xj.core.transport.JSON;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.sql.SQLException;

import static io.outright.xj.core.Tables.AUDIO;
import static io.outright.xj.core.Tables.AUDIO_EVENT;
import static io.outright.xj.core.tables.Instrument.INSTRUMENT;
import static io.outright.xj.core.tables.Library.LIBRARY;

public class AudioEventDAOImpl extends DAOImpl implements AudioEventDAO {
  //  private static Logger log = LoggerFactory.getLogger(AudioDAOImpl.class);

  @Inject
  public AudioEventDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public JSONObject create(AccessControl access, AudioEventWrapper data) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, data));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public JSONObject readOne(AccessControl access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public JSONArray readAllIn(AccessControl access, ULong audioId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllIn(tx.getContext(), access, audioId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(AccessControl access, ULong id, AudioEventWrapper data) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, id, data);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(AccessControl access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(access, tx.getContext(), id);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  /**
   * Create a new Audio Event
   *
   * @param db     context
   * @param access control
   * @param data   for new audio
   * @return newly created record
   * @throws BusinessException if failure
   */
  private JSONObject create(DSLContext db, AccessControl access, AudioEventWrapper data) throws BusinessException {
    AudioEventRecord record = db.newRecord(AUDIO_EVENT);
    data.validate();
    data.getAudioEvent().intoFieldValueMap().forEach(record::setValue);

    if (access.isAdmin()) {
      requireRecordExists("Audio", db.select(AUDIO.ID).from(AUDIO)
        .where(AUDIO.ID.eq(data.getAudioEvent().getAudioId()))
        .fetchOne());
    } else {
      requireRecordExists("Audio", db.select(AUDIO.ID).from(AUDIO)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .and(AUDIO.ID.eq(data.getAudioEvent().getAudioId()))
        .fetchOne());
    }

    record.store();
    return JSON.objectFromRecord(record);
  }

  /**
   * Read one Event if able
   *
   * @param db     context
   * @param access control
   * @param id     of audio
   * @return audio
   */
  private JSONObject readOne(DSLContext db, AccessControl access, ULong id) {
    JSONObject result;
    if (access.isAdmin()) {
      result = JSON.objectFromRecord(db.selectFrom(AUDIO_EVENT)
        .where(AUDIO_EVENT.ID.eq(id))
        .fetchOne());
    } else {
      result = JSON.objectFromRecord(db.select(AUDIO_EVENT.fields())
        .from(AUDIO_EVENT)
        .join(AUDIO).on(AUDIO.ID.eq(AUDIO_EVENT.AUDIO_ID))
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(AUDIO_EVENT.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }
    return result;
  }

  /**
   * Read all Event able for an Instrument
   *
   * @param db      context
   * @param access  control
   * @param audioId to read all audio of
   * @return array of audios
   * @throws SQLException on failure
   */
  private JSONArray readAllIn(DSLContext db, AccessControl access, ULong audioId) throws SQLException {
    JSONArray result;
    if (access.isAdmin()) {
      result = JSON.arrayFromResultSet(db.select(AUDIO_EVENT.fields())
        .from(AUDIO_EVENT)
        .where(AUDIO_EVENT.AUDIO_ID.eq(audioId))
        .orderBy(AUDIO_EVENT.POSITION)
        .fetchResultSet());
    } else {
      result = JSON.arrayFromResultSet(db.select(AUDIO_EVENT.fields())
        .from(AUDIO_EVENT)
        .join(AUDIO).on(AUDIO.ID.eq(AUDIO_EVENT.AUDIO_ID))
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(AUDIO_EVENT.AUDIO_ID.eq(audioId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .orderBy(AUDIO_EVENT.POSITION)
        .fetchResultSet());
    }
    return result;
  }

  /**
   * Update a Event record
   *
   * @param db     context
   * @param access control
   * @param id     to update
   * @param data   to update with
   * @throws BusinessException if failure
   */
  private void update(DSLContext db, AccessControl access, ULong id, AudioEventWrapper data) throws Exception {
    AudioEventRecord record;

    record = db.newRecord(AUDIO_EVENT);
    record.setId(id);
    data.validate();
    data.getAudioEvent().intoFieldValueMap().forEach(record::setValue);

    if (access.isAdmin()) {
      requireRecordExists("Audio", db.select(AUDIO.ID).from(AUDIO)
        .where(AUDIO.ID.eq(data.getAudioEvent().getAudioId()))
        .fetchOne());
    } else {
      requireRecordExists("Audio", db.select(AUDIO.ID).from(AUDIO)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .and(AUDIO.ID.eq(data.getAudioEvent().getAudioId()))
        .fetchOne());
    }

    if (db.executeUpdate(record) == 0) {
      throw new DatabaseException("No records updated.");
    }
  }

  /**
   * Delete an Event
   *
   * @param db context
   * @param id to delete
   * @throws Exception         if database failure
   * @throws ConfigException   if not configured properly
   * @throws BusinessException if fails business rule
   */
  private void delete(AccessControl access, DSLContext db, ULong id) throws Exception {
    if (!access.isAdmin()) {
      Record record = db.select(AUDIO_EVENT.ID).from(AUDIO_EVENT)
        .join(Audio.AUDIO).on(Audio.AUDIO.ID.eq(AUDIO_EVENT.AUDIO_ID))
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(INSTRUMENT.LIBRARY_ID.eq(LIBRARY.ID))
        .where(AUDIO_EVENT.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne();
      requireRecordExists("Audio Meme", record);
    }

    db.deleteFrom(AUDIO_EVENT)
      .where(AUDIO_EVENT.ID.eq(id))
      .execute();
  }

}
