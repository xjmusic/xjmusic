// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.config.Exposure;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.dao.AudioDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.external.amazon.AmazonProvider;
import io.outright.xj.core.model.audio.Audio;
import io.outright.xj.core.model.audio.AudioWrapper;
import io.outright.xj.core.transport.JSON;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.Map;

import static io.outright.xj.core.Tables.AUDIO_EVENT;
import static io.outright.xj.core.tables.Audio.AUDIO;
import static io.outright.xj.core.tables.Instrument.INSTRUMENT;
import static io.outright.xj.core.tables.Library.LIBRARY;

public class AudioDAOImpl extends DAOImpl implements AudioDAO {
  private final AmazonProvider amazonProvider;

  @Inject
  public AudioDAOImpl(
    SQLDatabaseProvider dbProvider,
    AmazonProvider amazonProvider
  ) {
    this.amazonProvider = amazonProvider;
    this.dbProvider = dbProvider;
  }

  @Override
  public JSONObject create(AccessControl access, AudioWrapper data) throws Exception {
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
  public JSONObject uploadOne(AccessControl access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(uploadOne(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public JSONArray readAllIn(AccessControl access, ULong instrumentId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllIn(tx.getContext(), access, instrumentId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(AccessControl access, ULong id, AudioWrapper data) throws Exception {
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
   * Create a new Audio
   *
   * @param db     context
   * @param access control
   * @param data   for new audio
   * @return newly created record
   * @throws BusinessException if failure
   */
  private JSONObject create(DSLContext db, AccessControl access, AudioWrapper data) throws BusinessException {
    Audio model = data.validate();
    Map<Field, Object> fieldValues = model.intoFieldValueMap();

    if (access.isTopLevel()) {
      requireRecordExists("Instrument", db.select(INSTRUMENT.ID).from(INSTRUMENT)
        .where(INSTRUMENT.ID.eq(model.getInstrumentId()))
        .fetchOne());
    } else {
      requireRecordExists("Instrument", db.select(INSTRUMENT.ID).from(INSTRUMENT)
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .and(INSTRUMENT.ID.eq(model.getInstrumentId()))
        .fetchOne());
    }

    fieldValues.put(AUDIO.WAVEFORM_KEY, generateUrl(model.getInstrumentId()));

    return JSON.objectFromRecord(executeCreate(db, AUDIO, fieldValues));
  }

  private String generateUrl(ULong instrumentId) {
    return amazonProvider.generateKey(
      Exposure.FILE_INSTRUMENT + Exposure.FILE_SEPARATOR +
        instrumentId + Exposure.FILE_SEPARATOR +
        Exposure.FILE_AUDIO, Exposure.FILE_EXTENSION);
  }

  /**
   * Read one Audio if able
   *
   * @param db     context
   * @param access control
   * @param id     of audio
   * @return audio
   */
  private JSONObject readOne(DSLContext db, AccessControl access, ULong id) {
    if (access.isTopLevel()) {
      return JSON.objectFromRecord(db.selectFrom(AUDIO)
        .where(AUDIO.ID.eq(id))
        .fetchOne());
    } else {
      return JSON.objectFromRecord(db.select(AUDIO.fields())
        .from(AUDIO)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(AUDIO.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }
  }

  /**
   * Read all Audio able for an Instrument
   *
   * @param db           context
   * @param access       control
   * @param instrumentId to read all audio of
   * @return array of audios
   * @throws SQLException on failure
   */
  private JSONArray readAllIn(DSLContext db, AccessControl access, ULong instrumentId) throws SQLException {
    if (access.isTopLevel()) {
      return JSON.arrayFromResultSet(db.select(AUDIO.fields())
        .from(AUDIO)
        .where(AUDIO.INSTRUMENT_ID.eq(instrumentId))
        .fetchResultSet());
    } else {
      return JSON.arrayFromResultSet(db.select(AUDIO.fields())
        .from(AUDIO)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(AUDIO.INSTRUMENT_ID.eq(instrumentId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchResultSet());
    }
  }

  /**
   * Update an Audio record
   * <p>
   * TODO: ensure that the user access has access to this Audio by id
   * TODO: ensure ALL RECORDS HAVE ACCESS CONTROL that asserts the record primary id against the user access-- build a system for it and implement it over all DAO methods
   *
   * @param db     context
   * @param access control
   * @param id     to update
   * @param data   to update with
   * @throws BusinessException if failure
   */
  private void update(DSLContext db, AccessControl access, ULong id, AudioWrapper data) throws Exception {
    Audio model = data.validate();
    Map<Field, Object> fieldValues = model.intoFieldValueMap();
    fieldValues.put(AUDIO.ID, id);

    if (access.isTopLevel()) {
      requireRecordExists("Instrument", db.select(INSTRUMENT.ID).from(INSTRUMENT)
        .where(INSTRUMENT.ID.eq(model.getInstrumentId()))
        .fetchOne());
    } else {
      requireRecordExists("Instrument", db.select(INSTRUMENT.ID).from(INSTRUMENT)
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .and(INSTRUMENT.ID.eq(model.getInstrumentId()))
        .fetchOne());
    }

    if (executeUpdate(db, AUDIO, fieldValues) == 0) {
      throw new BusinessException("No records updated.");
    }
  }

  /**
   * Update an Audio record
   *
   * @param db     context
   * @param access control
   * @param id     to update
   * @throws BusinessException if failure
   */
  private JSONObject uploadOne(DSLContext db, AccessControl access, ULong id) throws Exception {
    Record audioRecord;

    if (access.isTopLevel()) {
      audioRecord = db.selectFrom(AUDIO)
        .where(AUDIO.ID.eq(id))
        .fetchOne();
    } else {
      audioRecord = db.select(AUDIO.fields())
        .from(AUDIO)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(AUDIO.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne();
    }

    requireRecordExists("Audio", audioRecord);

    JSONObject uploadAuthorization = new JSONObject();
    String waveformKey = audioRecord.get(AUDIO.WAVEFORM_KEY);
    uploadAuthorization.put(Exposure.KEY_WAVEFORM_KEY, waveformKey);
    uploadAuthorization.put(Exposure.KEY_UPLOAD_URL, amazonProvider.getUploadURL());
    uploadAuthorization.put(Exposure.KEY_UPLOAD_ACCESS_KEY, amazonProvider.getAccessKey());
    uploadAuthorization.put(Exposure.KEY_UPLOAD_POLICY, amazonProvider.generateUploadPolicy(waveformKey));
    return uploadAuthorization;
  }

  /**
   * Delete an Audio
   *
   * @param db context
   * @param id to delete
   * @throws Exception         if database failure
   * @throws ConfigException   if not configured properly
   * @throws BusinessException if fails business rule
   */
  private void delete(AccessControl access, DSLContext db, ULong id) throws Exception {
    requireEmptyResultSet(db.select(AUDIO_EVENT.ID)
      .from(AUDIO_EVENT)
      .where(AUDIO_EVENT.AUDIO_ID.eq(id))
      .fetchResultSet());

    if (!access.isTopLevel()) {
      requireRecordExists("Audio", db.select(AUDIO.ID).from(AUDIO)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(AUDIO.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
    }

    db.deleteFrom(AUDIO)
      .where(AUDIO.ID.eq(id))
      .andNotExists(
        db.select(AUDIO_EVENT.ID)
          .from(AUDIO_EVENT)
          .where(AUDIO_EVENT.AUDIO_ID.eq(id))
      )
      .execute();
  }

}
