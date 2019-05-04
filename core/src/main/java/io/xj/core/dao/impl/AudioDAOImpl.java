// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import io.xj.core.access.impl.Access;
import io.xj.core.config.Config;
import io.xj.core.config.Exposure;
import io.xj.core.dao.AudioDAO;
import io.xj.core.exception.CoreException;
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
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.AUDIO_CHORD;
import static io.xj.core.Tables.AUDIO_EVENT;
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

  /**
   Read one Audio if able

   @param db     context
   @param access control
   @param id     of audio
   @return audio
   */
  private static Audio readOne(DSLContext db, Access access, ULong id) throws CoreException {
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

   @param db            context
   @param access        control
   @param instrumentIds to readMany all audio of
   @return Result of audio records.
   @throws CoreException on failure
   */
  private static Collection<Audio> readAll(DSLContext db, Access access, Collection<ULong> instrumentIds) throws CoreException {
    if (access.isTopLevel())
      return modelsFrom(db.select(AUDIO.fields())
        .from(AUDIO)
        .where(AUDIO.INSTRUMENT_ID.in(instrumentIds))
        .and(AUDIO.STATE.notEqual(String.valueOf(AudioState.Erase)))
        .orderBy(AUDIO.NAME.desc())
        .fetch(), Audio.class);
    else
      return modelsFrom(db.select(AUDIO.fields())
        .from(AUDIO)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(AUDIO.INSTRUMENT_ID.in(instrumentIds))
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
  private static Collection<Audio> readAllInState(DSLContext db, Access access, AudioState state) throws CoreException {
    requireRole("platform access", access, UserRoleType.Admin, UserRoleType.Engineer);

    return modelsFrom(db.select(AUDIO.fields())
      .from(AUDIO)
      .where(AUDIO.STATE.eq(state.toString()))
      .or(AUDIO.STATE.eq(state.toString().toLowerCase(Locale.ENGLISH)))
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
   @throws CoreException if failure
   */
  private static void update(DSLContext db, Access access, ULong id, Audio entity) throws CoreException {
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
      throw new CoreException("No records updated.");
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(Audio entity) {
    Map<Field, Object> fieldValues = com.google.api.client.util.Maps.newHashMap();
    fieldValues.put(AUDIO.INSTRUMENT_ID, ULong.valueOf(entity.getInstrumentId()));
    fieldValues.put(AUDIO.NAME, entity.getName());
    fieldValues.put(AUDIO.STATE, entity.getState());
    fieldValues.put(AUDIO.START, entity.getStart());
    fieldValues.put(AUDIO.LENGTH, entity.getLength());
    fieldValues.put(AUDIO.TEMPO, entity.getTempo());
    fieldValues.put(AUDIO.PITCH, entity.getPitch());
    // Excluding AUDIO.WAVEFORM_KEY a.k.a. waveformKey because that is read-only
    return fieldValues;
  }

  @Override
  public Audio create(Access access, Audio entity) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Audio clone(Access access, BigInteger cloneId, Audio entity) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(clone(tx.getContext(), access, cloneId, entity));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Audio readOne(Access access, BigInteger id) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public JSONObject authorizeUpload(Access access, BigInteger id) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(authorizeUpload(tx.getContext(), access, ULong.valueOf(id)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Collection<Audio> readAll(Access access, Collection<BigInteger> parentIds) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, uLongValuesOf(parentIds)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, Audio entity) throws CoreException {
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
      destroy(access, tx.getContext(), ULong.valueOf(id));
      tx.success();
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Audio> readAllInState(Access access, AudioState state) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInState(tx.getContext(), access, state));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void erase(Access access, BigInteger id) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      erase(access, tx.getContext(), ULong.valueOf(id));
      tx.success();
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  /**
   Create a new Audio

   @param db     context
   @param access control
   @param entity for new audio
   @return newly readMany record
   @throws CoreException if failure
   */
  private Audio create(DSLContext db, Access access, Audio entity) throws CoreException {
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
    AudioRecord result = executeCreate(db, AUDIO, fieldValues);

    return modelFrom(result, Audio.class);
  }

  /**
   Clone a Audio into a new Audio

   @param db      context
   @param access  control
   @param cloneId of audio to clone
   @param entity  for the new Account User.
   @return newly readMany record
   @throws CoreException on failure
   */
  private Audio clone(DSLContext db, Access access, BigInteger cloneId, Audio entity) throws CoreException {
    Audio from = readOne(db, access, ULong.valueOf(cloneId));
    if (Objects.isNull(from)) throw new CoreException("Can't clone nonexistent Audio");

    entity.setStateEnum(from.getState());
    entity.setStart(from.getStart());
    entity.setLength(from.getLength());
    entity.setTempo(from.getTempo());
    entity.setPitch(from.getPitch());

    Audio result = create(db, access, entity);
    workManager.doAudioClone(cloneId, result.getId());
    return result;
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
   Update an Audio record

   @param db     context
   @param access control
   @param id     to update
   @throws CoreException if failure
   */
  private JSONObject authorizeUpload(DSLContext db, Access access, ULong id) throws CoreException {
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
   @throws CoreException if database failure
   @throws CoreException if not configured properly
   @throws CoreException if fails business rule
   */
  private void destroy(Access access, DSLContext db, ULong audioId) throws CoreException {
    requireTopLevel(access);

    AudioRecord audioRecord = db.selectFrom(AUDIO)
      .where(AUDIO.ID.eq(audioId))
      .fetchOne();
    requireExists("Audio to destroy", audioRecord);

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
   @throws CoreException if database failure
   @throws CoreException if not configured properly
   @throws CoreException if fails business rule
   */
  private void erase(Access access, DSLContext db, ULong id) throws CoreException {
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
      throw new CoreException("No records updated.");

    // Schedule audio deletion job
    workManager.doAudioErase(id.toBigInteger());
  }
}
