// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.impl;

import com.amazonaws.services.ec2.util.S3UploadPolicy;
import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.config.Exposure;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.dao.AudioDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.external.amazon.AmazonProvider;
import io.outright.xj.core.model.audio.Audio;
import io.outright.xj.core.tables.records.AudioRecord;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.inject.Inject;

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
  public AudioRecord create(Access access, Audio entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public AudioRecord readOne(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public JSONObject uploadOne(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(uploadOne(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Result<AudioRecord> readAll(Access access, ULong instrumentId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, instrumentId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, ULong id, Audio entity) throws Exception {
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
   Create a new Audio

   @param db     context
   @param access control
   @param entity for new audio
   @return newly readMany record
   @throws BusinessException if failure
   */
  private AudioRecord createRecord(DSLContext db, Access access, Audio entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    if (access.isTopLevel())
      requireExists("Instrument", db.select(INSTRUMENT.ID).from(INSTRUMENT)
        .where(INSTRUMENT.ID.eq(entity.getInstrumentId()))
        .fetchOne());
    else
      requireExists("Instrument", db.select(INSTRUMENT.ID).from(INSTRUMENT)
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .and(INSTRUMENT.ID.eq(entity.getInstrumentId()))
        .fetchOne());

    fieldValues.put(AUDIO.WAVEFORM_KEY, generateUrl(entity.getInstrumentId()));

    return executeCreate(db, AUDIO, fieldValues);
  }

  /**
   General an Audio URL

   @param instrumentId to generate URL for
   @return URL as string
   */
  private String generateUrl(ULong instrumentId) {
    return amazonProvider.generateKey(
      Exposure.FILE_INSTRUMENT + Exposure.FILE_SEPARATOR +
        instrumentId + Exposure.FILE_SEPARATOR +
        Exposure.FILE_AUDIO, Exposure.FILE_EXTENSION);
  }

  /**
   Read one Audio if able

   @param db     context
   @param access control
   @param id     of audio
   @return audio
   */
  private AudioRecord readOne(DSLContext db, Access access, ULong id) {
    if (access.isTopLevel())
      return db.selectFrom(AUDIO)
        .where(AUDIO.ID.eq(id))
        .fetchOne();
    else
      return recordInto(AUDIO, db.select(AUDIO.fields())
        .from(AUDIO)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(AUDIO.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
  }

  /**
   Read all Audio able for an Instrument

   @param db           context
   @param access       control
   @param instrumentId to readMany all audio of
   @return array of audios
   @throws SQLException on failure
   */
  private Result<AudioRecord> readAll(DSLContext db, Access access, ULong instrumentId) throws SQLException {
    if (access.isTopLevel())
      return resultInto(AUDIO, db.select(AUDIO.fields())
        .from(AUDIO)
        .where(AUDIO.INSTRUMENT_ID.eq(instrumentId))
        .fetch());
    else
      return resultInto(AUDIO, db.select(AUDIO.fields())
        .from(AUDIO)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(AUDIO.INSTRUMENT_ID.eq(instrumentId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetch());
  }

  /**
   Update an Audio record
   <p>
   TODO: ensure that the user access has access to this Audio by id
   TODO: ensure ALL RECORDS HAVE ACCESS CONTROL that asserts the record primary id against the user access-- build a system for it and implement it over all DAO methods

   @param db     context
   @param access control
   @param id     to update
   @param entity to update with
   @throws BusinessException if failure
   */
  private void update(DSLContext db, Access access, ULong id, Audio entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();
    fieldValues.put(AUDIO.ID, id);

    if (access.isTopLevel())
      requireExists("Instrument", db.select(INSTRUMENT.ID).from(INSTRUMENT)
        .where(INSTRUMENT.ID.eq(entity.getInstrumentId()))
        .fetchOne());
    else
      requireExists("Instrument", db.select(INSTRUMENT.ID).from(INSTRUMENT)
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .and(INSTRUMENT.ID.eq(entity.getInstrumentId()))
        .fetchOne());

    if (executeUpdate(db, AUDIO, fieldValues) == 0)
      throw new BusinessException("No records updated.");
  }

  /**
   Update an Audio record

   @param db     context
   @param access control
   @param id     to update
   @throws BusinessException if failure
   */
  private JSONObject uploadOne(DSLContext db, Access access, ULong id) throws Exception {
    Record audioRecord;

    if (access.isTopLevel())
      audioRecord = db.selectFrom(AUDIO)
        .where(AUDIO.ID.eq(id))
        .fetchOne();
    else
      audioRecord = db.select(AUDIO.fields())
        .from(AUDIO)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(AUDIO.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne();

    requireExists("Audio", audioRecord);

    JSONObject uploadAuthorization = new JSONObject();
    S3UploadPolicy uploadPolicy = amazonProvider.generateUploadPolicy();
    String waveformKey = audioRecord.get(AUDIO.WAVEFORM_KEY);
    uploadAuthorization.put(Exposure.KEY_WAVEFORM_KEY, waveformKey);
    uploadAuthorization.put(Exposure.KEY_UPLOAD_URL, amazonProvider.getUploadURL());
    uploadAuthorization.put(Exposure.KEY_UPLOAD_ACCESS_KEY, amazonProvider.getAccessKey());
    uploadAuthorization.put(Exposure.KEY_UPLOAD_POLICY, uploadPolicy.getPolicyString());
    uploadAuthorization.put(Exposure.KEY_UPLOAD_POLICY_SIGNATURE, uploadPolicy.getPolicySignature());
    uploadAuthorization.put(Exposure.KEY_UPLOAD_BUCKET_NAME, amazonProvider.getBucketName());
    uploadAuthorization.put(Exposure.KEY_UPLOAD_ACL, amazonProvider.getUploadACL());
    return uploadAuthorization;
  }

  /**
   Delete an Audio

   @param db context
   @param id to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private void delete(Access access, DSLContext db, ULong id) throws Exception {
    requireNotExists("Event in Audio", db.select(AUDIO_EVENT.ID)
      .from(AUDIO_EVENT)
      .where(AUDIO_EVENT.AUDIO_ID.eq(id))
      .fetch());

    if (!access.isTopLevel())
      requireExists("Audio", db.select(AUDIO.ID).from(AUDIO)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(AUDIO.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());

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
