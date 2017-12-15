// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.Tables;
import io.xj.core.access.impl.Access;
import io.xj.core.config.Config;
import io.xj.core.config.Exposure;
import io.xj.core.dao.AudioDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.external.amazon.S3UploadPolicy;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.audio.AudioState;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;
import io.xj.core.tables.records.AudioRecord;
import io.xj.core.work.WorkManager;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.types.ULong;

import com.google.common.collect.Maps;
import com.google.inject.Inject;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.ARRANGEMENT;
import static io.xj.core.Tables.AUDIO_CHORD;
import static io.xj.core.Tables.AUDIO_EVENT;
import static io.xj.core.Tables.CHOICE;
import static io.xj.core.Tables.PICK;
import static io.xj.core.tables.Audio.AUDIO;
import static io.xj.core.tables.Instrument.INSTRUMENT;
import static io.xj.core.tables.Library.LIBRARY;

public class AudioDAOImpl extends DAOImpl implements AudioDAO {
  private static final Logger log = LoggerFactory.getLogger(AudioDAOImpl.class);
  private final AmazonProvider amazonProvider;
  private final WorkManager workManager;

  @Inject
  public AudioDAOImpl(
    SQLDatabaseProvider dbProvider,
    AmazonProvider amazonProvider,
    WorkManager workManager
  ) {
    this.amazonProvider = amazonProvider;
    this.workManager = workManager;
    this.dbProvider = dbProvider;
  }

  @Override
  public Audio create(Access access, Audio entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Audio readOne(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public JSONObject authorizeUpload(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(authorizeUpload(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Collection<Audio> readAll(Access access, BigInteger instrumentId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, ULong.valueOf(instrumentId)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Audio> readAllInState(Access access, AudioState state) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInState(tx.getContext(), access, state));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Audio> readAllPickedForLink(Access access, BigInteger linkId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllPickedForLink(tx.getContext(), access, ULong.valueOf(linkId)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, Audio entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, ULong.valueOf(id), entity);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void destroy(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      destroy(access, tx.getContext(), ULong.valueOf(id));
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void erase(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      erase(access, tx.getContext(), ULong.valueOf(id));
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
  private Audio create(DSLContext db, Access access, Audio entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    if (access.isTopLevel())
      requireExists("Instrument", db.selectCount().from(INSTRUMENT)
        .where(INSTRUMENT.ID.eq(ULong.valueOf(entity.getInstrumentId())))
        .fetchOne());
    else
      requireExists("Instrument", db.selectCount().from(INSTRUMENT)
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(idCollection(access.getAccountIds())))
        .and(INSTRUMENT.ID.eq(ULong.valueOf(entity.getInstrumentId())))
        .fetchOne());

    fieldValues.put(AUDIO.WAVEFORM_KEY, generateKey(entity.getInstrumentId()));

    return modelFrom(executeCreate(db, AUDIO, fieldValues), Audio.class);
  }

  /**
   General an Audio URL

   @param instrumentId to generate URL for
   @return URL as string
   */
  private String generateKey(BigInteger instrumentId) {
    return amazonProvider.generateKey(
      Exposure.FILE_INSTRUMENT + Exposure.FILE_SEPARATOR +
        instrumentId + Exposure.FILE_SEPARATOR +
        Exposure.FILE_AUDIO, Audio.FILE_EXTENSION);
  }

  /**
   Read one Audio if able

   @param db     context
   @param access control
   @param id     of audio
   @return audio
   */
  private static Audio readOne(DSLContext db, Access access, ULong id) throws BusinessException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(AUDIO)
        .where(AUDIO.ID.eq(id))
        .fetchOne(), Audio.class);
    else
      return modelFrom(db.select(AUDIO.fields())
        .from(AUDIO)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(AUDIO.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), Audio.class);
  }

  /**
   Read all Audio able for an Instrument
   [#326] Instruments Audios returned in order of name

   @param db           context
   @param access       control
   @param instrumentId to readMany all audio of
   @return Result of audio records.
   @throws Exception on failure
   */
  private static Collection<Audio> readAll(DSLContext db, Access access, ULong instrumentId) throws Exception {
    if (access.isTopLevel())
      return modelsFrom(db.select(AUDIO.fields())
        .from(AUDIO)
        .where(AUDIO.INSTRUMENT_ID.eq(instrumentId))
        .and(AUDIO.STATE.notEqual(String.valueOf(AudioState.Erase)))
        .orderBy(AUDIO.NAME.desc())
        .fetch(), Audio.class);
    else
      return modelsFrom(db.select(AUDIO.fields())
        .from(AUDIO)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(AUDIO.INSTRUMENT_ID.eq(instrumentId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .and(AUDIO.STATE.notEqual(String.valueOf(AudioState.Erase)))
        .orderBy(AUDIO.NAME.desc())
        .fetch(), Audio.class);
  }

  /**
   Read all records in a given state

   @param db     context
   @param access control
   @param state  to read audios in
   @return array of records
   */
  private static Collection<Audio> readAllInState(DSLContext db, Access access, AudioState state) throws Exception {
    requireRole("platform access", access, UserRoleType.Admin, UserRoleType.Engineer);

    return modelsFrom(db.select(AUDIO.fields())
      .from(AUDIO)
      .where(AUDIO.STATE.eq(state.toString()))
      .or(AUDIO.STATE.eq(state.toString().toLowerCase()))
      .fetch(), Audio.class);
  }


  /**
   Read all Audio able picked for a Link

   @param db     context
   @param access control
   @param linkId to get audio picked for
   @return Result of audio records.
   @throws Exception on failure
   */
  private static Collection<Audio> readAllPickedForLink(DSLContext db, Access access, ULong linkId) throws Exception {
    requireTopLevel(access);

    return modelsFrom(db.select(AUDIO.fields())
      .from(AUDIO)
      .join(PICK).on(PICK.AUDIO_ID.eq(AUDIO.ID))
      .join(ARRANGEMENT).on(ARRANGEMENT.ID.eq(PICK.ARRANGEMENT_ID))
      .join(CHOICE).on(CHOICE.ID.eq(ARRANGEMENT.CHOICE_ID))
      .where(CHOICE.LINK_ID.eq(linkId))
      .fetch(), Audio.class);
  }

  /**
   Update an Audio record
   <p>
   future: should ensure that the user access has access to this Audio by id
   future: should ensure ALL RECORDS HAVE ACCESS CONTROL that asserts the record primary id against the user access-- build a system for it and implement it over all DAO methods

   @param db     context
   @param access control
   @param id     to update
   @param entity to update with
   @throws BusinessException if failure
   */
  private static void update(DSLContext db, Access access, ULong id, Audio entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);
    fieldValues.put(AUDIO.ID, id);

    if (access.isTopLevel())
      requireExists("Instrument", db.selectCount().from(INSTRUMENT)
        .where(INSTRUMENT.ID.eq(ULong.valueOf(entity.getInstrumentId())))
        .fetchOne(0, int.class));
    else
      requireExists("Instrument", db.selectCount().from(INSTRUMENT)
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .and(INSTRUMENT.ID.eq(ULong.valueOf(entity.getInstrumentId())))
        .fetchOne(0, int.class));

    if (0 == executeUpdate(db, AUDIO, fieldValues))
      throw new BusinessException("No records updated.");
  }

  /**
   Update an Audio record

   @param db     context
   @param access control
   @param id     to update
   @throws BusinessException if failure
   */
  private JSONObject authorizeUpload(DSLContext db, Access access, ULong id) throws Exception {
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
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne();

    requireExists("Audio", audioRecord);

    JSONObject uploadAuthorization = new JSONObject();
    S3UploadPolicy uploadPolicy = amazonProvider.generateAudioUploadPolicy();
    String waveformKey = audioRecord.get(AUDIO.WAVEFORM_KEY);
    uploadAuthorization.put(Exposure.KEY_WAVEFORM_KEY, waveformKey);
    uploadAuthorization.put(Exposure.KEY_UPLOAD_URL, amazonProvider.getUploadURL());
    uploadAuthorization.put(Exposure.KEY_UPLOAD_ACCESS_KEY, amazonProvider.getCredentialId());
    uploadAuthorization.put(Exposure.KEY_UPLOAD_POLICY, uploadPolicy.getPolicyString());
    uploadAuthorization.put(Exposure.KEY_UPLOAD_POLICY_SIGNATURE, uploadPolicy.getPolicySignature());
    uploadAuthorization.put(Exposure.KEY_UPLOAD_BUCKET_NAME, amazonProvider.getAudioBucketName());
    uploadAuthorization.put(Exposure.KEY_UPLOAD_ACL, amazonProvider.getAudioUploadACL());
    return uploadAuthorization;
  }

  /**
   Destroy an Audio

   @param db      context
   @param audioId to destroy
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private void destroy(Access access, DSLContext db, ULong audioId) throws Exception {
    requireTopLevel(access);

    AudioRecord audioRecord = db.selectFrom(AUDIO)
      .where(AUDIO.ID.eq(audioId))
      .fetchOne();
    requireExists("Audio to destroy", audioRecord);

    // Picks of Audio
    db.deleteFrom(PICK)
      .where(PICK.AUDIO_ID.eq(audioId))
      .execute();

    // [#163] When an Audio record is deleted, remove its related S3 object in order to save storage space.
    // Only Delete audio waveform from S3 if non-null
    String waveformKey = audioRecord.get(AUDIO.WAVEFORM_KEY);
    if (Objects.nonNull(waveformKey))
      amazonProvider.deleteS3Object(
        Config.audioFileBucket(),
        waveformKey);

    // Audio Events
    db.deleteFrom(AUDIO_EVENT)
      .where(AUDIO_EVENT.AUDIO_ID.eq(audioId))
      .execute();

    // Audio Chords
    db.deleteFrom(AUDIO_CHORD)
      .where(AUDIO_CHORD.AUDIO_ID.eq(audioId))
      .execute();

    // Audio
    db.deleteFrom(AUDIO)
      .where(AUDIO.ID.eq(audioId))
      .execute();
  }

  /**
   Update an audio to Erase state

   @param db context
   @param id to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private void erase(Access access, DSLContext db, ULong id) throws Exception {
    if (access.isTopLevel()) requireExists("Audio", db.selectCount().from(AUDIO)
      .where(AUDIO.ID.eq(id))
      .fetchOne(0, int.class));
    else requireExists("Audio", db.selectCount().from(AUDIO)
      .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
      .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
      .where(AUDIO.ID.eq(id))
      .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
      .fetchOne(0, int.class));

    // Update audio state to Erase
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(AUDIO.ID, id);
    fieldValues.put(AUDIO.STATE, AudioState.Erase);

    if (0 == executeUpdate(db, AUDIO, fieldValues))
      throw new BusinessException("No records updated.");

    // Schedule audio deletion job
    try {
      workManager.startAudioErase(id.toBigInteger());
    } catch (Exception e) {
      log.error("Failed to start AudioErase work after updating Audio to Erase state. See the elusive [#153492153] Audio can be deleted without an error", e);
    }
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(Audio entity) {
    Map<Field, Object> fieldValues = com.google.api.client.util.Maps.newHashMap();
    fieldValues.put(Tables.AUDIO.INSTRUMENT_ID, ULong.valueOf(entity.getInstrumentId()));
    fieldValues.put(Tables.AUDIO.NAME, entity.getName());
    fieldValues.put(Tables.AUDIO.STATE, entity.getState());
    fieldValues.put(Tables.AUDIO.START, entity.getStart());
    fieldValues.put(Tables.AUDIO.LENGTH, entity.getLength());
    fieldValues.put(Tables.AUDIO.TEMPO, entity.getTempo());
    fieldValues.put(Tables.AUDIO.PITCH, entity.getPitch());
    // Excluding AUDIO.WAVEFORM_KEY a.k.a. waveformKey because that is read-only
    return fieldValues;
  }
}
