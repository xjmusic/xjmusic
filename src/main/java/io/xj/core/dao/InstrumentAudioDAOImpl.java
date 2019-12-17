// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.core.access.Access;
import io.xj.core.entity.Entity;
import io.xj.core.exception.CoreException;
import io.xj.core.external.AmazonProvider;
import io.xj.core.external.S3UploadPolicy;
import io.xj.core.model.InstrumentAudio;
import io.xj.core.persistence.SQLDatabaseProvider;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static io.xj.core.Tables.INSTRUMENT;
import static io.xj.core.Tables.INSTRUMENT_AUDIO;
import static io.xj.core.Tables.INSTRUMENT_AUDIO_CHORD;
import static io.xj.core.Tables.INSTRUMENT_AUDIO_EVENT;
import static io.xj.core.Tables.LIBRARY;

public class InstrumentAudioDAOImpl extends DAOImpl<InstrumentAudio> implements InstrumentAudioDAO {
  private final AmazonProvider amazonProvider;
  // key special resources (e.g. upload policy)
  String KEY_UPLOAD_ACCESS_KEY = "awsAccessKeyId";
  String KEY_UPLOAD_POLICY = "uploadPolicy";
  String KEY_UPLOAD_URL = "uploadUrl";
  String KEY_WAVEFORM_KEY = "waveformKey";
  String KEY_UPLOAD_POLICY_SIGNATURE = "uploadPolicySignature";
  String KEY_UPLOAD_BUCKET_NAME = "bucketName";
  String KEY_UPLOAD_ACL = "acl";
  private String waveformFileExtension;

  @Inject
  public InstrumentAudioDAOImpl(
    SQLDatabaseProvider dbProvider,
    AmazonProvider amazonProvider,
    Config config
  ) {
    this.amazonProvider = amazonProvider;
    this.dbProvider = dbProvider;

    // TODO [#170288602] Create instrument audio, provide waveform file extension as query parameter (checked by front-end after selecting the upload file)
    waveformFileExtension = config.getString("audio.fileExtension");
  }

  @Override
  public InstrumentAudio create(Access access, InstrumentAudio entity) throws CoreException {
    entity.validate();
    requireArtist(access);

    DSLContext db = dbProvider.getDSL();
    requireParentExists(db, access, entity);
    entity.setWaveformKey(generateKey(entity.getInstrumentId()));

    return DAO.modelFrom(InstrumentAudio.class,
      executeCreate(db, INSTRUMENT_AUDIO, entity));
  }

  @Override
  public Map<String, String> authorizeUpload(Access access, UUID id) throws CoreException {
    InstrumentAudio entity = readOne(dbProvider.getDSL(), access, id);

    Map<String, String> uploadAuthorization = Maps.newConcurrentMap();
    S3UploadPolicy uploadPolicy = amazonProvider.generateAudioUploadPolicy();

    uploadAuthorization.put(KEY_WAVEFORM_KEY, entity.getWaveformKey());
    uploadAuthorization.put(KEY_UPLOAD_URL, amazonProvider.getUploadURL());
    uploadAuthorization.put(KEY_UPLOAD_ACCESS_KEY, amazonProvider.getCredentialId());
    uploadAuthorization.put(KEY_UPLOAD_POLICY, uploadPolicy.getPolicyString());
    uploadAuthorization.put(KEY_UPLOAD_POLICY_SIGNATURE, uploadPolicy.getPolicySignature());
    uploadAuthorization.put(KEY_UPLOAD_BUCKET_NAME, amazonProvider.getAudioBucketName());
    uploadAuthorization.put(KEY_UPLOAD_ACL, amazonProvider.getAudioUploadACL());
    return uploadAuthorization;
  }

  @Override
  @Nullable
  public InstrumentAudio readOne(Access access, UUID id) throws CoreException {
    return readOne(dbProvider.getDSL(), access, id);
  }

  @Override
  @Nullable
  public Collection<InstrumentAudio> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    requireArtist(access);
    if (access.isTopLevel())
      return DAO.modelsFrom(InstrumentAudio.class,
        dbProvider.getDSL().selectFrom(INSTRUMENT_AUDIO)
          .where(INSTRUMENT_AUDIO.INSTRUMENT_ID.in(parentIds))
          .fetch());
    else
      return DAO.modelsFrom(InstrumentAudio.class,
        dbProvider.getDSL().select(INSTRUMENT_AUDIO.fields())
          .from(INSTRUMENT_AUDIO)
          .join(INSTRUMENT).on(INSTRUMENT.ID.eq(INSTRUMENT_AUDIO.INSTRUMENT_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
          .where(INSTRUMENT_AUDIO.INSTRUMENT_ID.in(parentIds))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetch());
  }

  @Override
  public void update(Access access, UUID id, InstrumentAudio updates) throws CoreException {
    updates.validate();
    requireArtist(access);

    DSLContext db = dbProvider.getDSL();

    requireParentExists(db, access, updates);

    InstrumentAudio original = readOne(db, access, id);
    updates.setWaveformKey(original.getWaveformKey());

    executeUpdate(db, INSTRUMENT_AUDIO, id, updates);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {

    DSLContext db = dbProvider.getDSL();

    requireExists("InstrumentAudio", readOne(db, access, id));

    db.deleteFrom(INSTRUMENT_AUDIO_EVENT)
      .where(INSTRUMENT_AUDIO_EVENT.INSTRUMENT_AUDIO_ID.eq(id))
      .execute();

    db.deleteFrom(INSTRUMENT_AUDIO_CHORD)
      .where(INSTRUMENT_AUDIO_CHORD.INSTRUMENT_AUDIO_ID.eq(id))
      .execute();

    db.deleteFrom(INSTRUMENT_AUDIO)
      .where(INSTRUMENT_AUDIO.ID.eq(id))
      .execute();
  }

  @Override
  public InstrumentAudio newInstance() {
    return new InstrumentAudio();
  }

  @Override
  public InstrumentAudio clone(Access access, UUID cloneId, InstrumentAudio entity) throws CoreException {
    requireArtist(access);
    AtomicReference<InstrumentAudio> result = new AtomicReference<>();
    dbProvider.getDSL().transaction(ctx -> {
      DSLContext db = DSL.using(ctx);

      InstrumentAudio from = readOne(db, access, cloneId);
      if (Objects.isNull(from))
        throw new CoreException("Can't clone nonexistent InstrumentAudio");

      // When null, inherits, type, state, key, and tempo
      if (Objects.isNull(entity.getPitch())) entity.setPitch(from.getPitch());
      if (Objects.isNull(entity.getStart())) entity.setStart(from.getStart());
      if (Objects.isNull(entity.getWaveformKey())) entity.setWaveformKey(from.getWaveformKey());
      if (Objects.isNull(entity.getTempo())) entity.setTempo(from.getTempo());
      if (Objects.isNull(entity.getDensity())) entity.setDensity(from.getDensity());
      if (Objects.isNull(entity.getLength())) entity.setLength(from.getLength());
      if (Objects.isNull(entity.getName())) entity.setName(from.getName());
      entity.validate();
      requireParentExists(db, access, entity);

      result.set(DAO.modelFrom(InstrumentAudio.class, executeCreate(db, INSTRUMENT_AUDIO, entity)));

      DAOCloner<Entity> cloner = new DAOCloner<Entity>(result.get());
      cloner.clone(db, INSTRUMENT_AUDIO_EVENT, INSTRUMENT_AUDIO_EVENT.ID, ImmutableSet.of(), INSTRUMENT_AUDIO_EVENT.INSTRUMENT_AUDIO_ID, cloneId, result.get().getId());
      cloner.clone(db, INSTRUMENT_AUDIO_CHORD, INSTRUMENT_AUDIO_CHORD.ID, ImmutableSet.of(), INSTRUMENT_AUDIO_CHORD.INSTRUMENT_AUDIO_ID, cloneId, result.get().getId());
    });
    return result.get();
  }

  /**
   General an Audio URL

   @param instrumentId to generate URL for
   @return URL as string
   */
  private String generateKey(UUID instrumentId) {
    String prefix = String.format("instrument-%s-audio", instrumentId);
    return amazonProvider.generateKey(prefix, waveformFileExtension);
  }

  /**
   Require parent instrument exists of a given possible entity in a DSL context

   @param db     DSL context
   @param access control
   @param entity to validate
   @throws CoreException if parent does not exist
   */
  private void requireParentExists(DSLContext db, Access access, InstrumentAudio entity) throws CoreException {
    if (access.isTopLevel())
      requireExists("Instrument", db.selectCount().from(INSTRUMENT)
        .where(INSTRUMENT.ID.eq(entity.getInstrumentId()))
        .fetchOne(0, int.class));
    else
      requireExists("Instrument", db.selectCount().from(INSTRUMENT)
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .and(INSTRUMENT.ID.eq(entity.getInstrumentId()))
        .fetchOne(0, int.class));
  }

  /**
   Read one record with the given DSL context,
   ensuring audio in instrument in library in access control account ids

   @param db     DSL context
   @param access control
   @param id     of record to read
   @return entity
   */
  private InstrumentAudio readOne(DSLContext db, Access access, UUID id) throws CoreException {
    requireArtist(access);
    Record record;
    if (access.isTopLevel())
      record = db.selectFrom(INSTRUMENT_AUDIO)
        .where(INSTRUMENT_AUDIO.ID.eq(id))
        .fetchOne();
    else
      record = db.select(INSTRUMENT_AUDIO.fields())
        .from(INSTRUMENT_AUDIO)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(INSTRUMENT_AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(INSTRUMENT_AUDIO.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne();
    requireExists("InstrumentAudio", record);
    return DAO.modelFrom(InstrumentAudio.class, record);
  }

}
