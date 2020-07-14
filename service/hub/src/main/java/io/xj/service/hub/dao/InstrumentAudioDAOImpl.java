// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.lib.entity.Entity;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.filestore.S3UploadPolicy;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.entity.InstrumentAudio;
import io.xj.service.hub.persistence.HubDatabaseProvider;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static io.xj.service.hub.Tables.*;

public class InstrumentAudioDAOImpl extends DAOImpl<InstrumentAudio> implements InstrumentAudioDAO {
  private final FileStoreProvider fileStoreProvider;
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
    PayloadFactory payloadFactory,
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider,
    FileStoreProvider fileStoreProvider,
    Config config
  ) {
    super(payloadFactory, entityFactory);
    this.fileStoreProvider = fileStoreProvider;
    this.dbProvider = dbProvider;

    // FUTURE [#170288602] Create instrument audio, provide waveform file extension as query parameter (checked by front-end after selecting the upload file)
    waveformFileExtension = config.getString("audio.fileExtension");
  }

  @Override
  public InstrumentAudio create(HubAccess hubAccess, InstrumentAudio entity) throws DAOException, JsonApiException, ValueException {
    entity.validate();
    requireArtist(hubAccess);

    DSLContext db = dbProvider.getDSL();
    requireParentExists(db, hubAccess, entity);
    entity.setWaveformKey(generateKey(entity.getInstrumentId()));

    return modelFrom(InstrumentAudio.class,
      executeCreate(db, INSTRUMENT_AUDIO, entity));
  }

  @Override
  public Map<String, String> authorizeUpload(HubAccess hubAccess, UUID id) throws DAOException, FileStoreException {
    InstrumentAudio entity = readOne(dbProvider.getDSL(), hubAccess, id);

    Map<String, String> uploadAuthorization = Maps.newConcurrentMap();
    S3UploadPolicy uploadPolicy = fileStoreProvider.generateAudioUploadPolicy();

    uploadAuthorization.put(KEY_WAVEFORM_KEY, entity.getWaveformKey());
    uploadAuthorization.put(KEY_UPLOAD_URL, fileStoreProvider.getUploadURL());
    uploadAuthorization.put(KEY_UPLOAD_ACCESS_KEY, fileStoreProvider.getCredentialId());
    uploadAuthorization.put(KEY_UPLOAD_POLICY, uploadPolicy.getPolicyString());
    uploadAuthorization.put(KEY_UPLOAD_POLICY_SIGNATURE, uploadPolicy.getPolicySignature());
    uploadAuthorization.put(KEY_UPLOAD_BUCKET_NAME, fileStoreProvider.getAudioBucketName());
    uploadAuthorization.put(KEY_UPLOAD_ACL, fileStoreProvider.getAudioUploadACL());
    return uploadAuthorization;
  }

  @Override
  @Nullable
  public InstrumentAudio readOne(HubAccess hubAccess, UUID id) throws DAOException {
    return readOne(dbProvider.getDSL(), hubAccess, id);
  }

  @Override
  @Nullable
  public Collection<InstrumentAudio> readMany(HubAccess hubAccess, Collection<UUID> parentIds) throws DAOException {
    requireArtist(hubAccess);
    if (hubAccess.isTopLevel())
      return modelsFrom(InstrumentAudio.class,
        dbProvider.getDSL().selectFrom(INSTRUMENT_AUDIO)
          .where(INSTRUMENT_AUDIO.INSTRUMENT_ID.in(parentIds))
          .fetch());
    else
      return modelsFrom(InstrumentAudio.class,
        dbProvider.getDSL().select(INSTRUMENT_AUDIO.fields())
          .from(INSTRUMENT_AUDIO)
          .join(INSTRUMENT).on(INSTRUMENT.ID.eq(INSTRUMENT_AUDIO.INSTRUMENT_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
          .where(INSTRUMENT_AUDIO.INSTRUMENT_ID.in(parentIds))
          .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
          .fetch());
  }

  @Override
  public void update(HubAccess hubAccess, UUID id, InstrumentAudio updates) throws DAOException, JsonApiException, ValueException {
    updates.validate();
    requireArtist(hubAccess);

    DSLContext db = dbProvider.getDSL();

    requireParentExists(db, hubAccess, updates);

    InstrumentAudio original = readOne(db, hubAccess, id);
    updates.setWaveformKey(original.getWaveformKey());

    executeUpdate(db, INSTRUMENT_AUDIO, id, updates);
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) throws DAOException {

    DSLContext db = dbProvider.getDSL();

    requireExists("InstrumentAudio", readOne(db, hubAccess, id));

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
  public InstrumentAudio clone(HubAccess hubAccess, UUID cloneId, InstrumentAudio entity) throws DAOException {
    requireArtist(hubAccess);
    AtomicReference<InstrumentAudio> result = new AtomicReference<>();
    dbProvider.getDSL().transaction(ctx -> {
      DSLContext db = DSL.using(ctx);

      InstrumentAudio from = readOne(db, hubAccess, cloneId);
      if (Objects.isNull(from))
        throw new DAOException("Can't clone nonexistent InstrumentAudio");

      // When null, inherits, type, state, key, and tempo
      if (Objects.isNull(entity.getPitch())) entity.setPitch(from.getPitch());
      if (Objects.isNull(entity.getStart())) entity.setStart(from.getStart());
      if (Objects.isNull(entity.getWaveformKey())) entity.setWaveformKey(from.getWaveformKey());
      if (Objects.isNull(entity.getTempo())) entity.setTempo(from.getTempo());
      if (Objects.isNull(entity.getDensity())) entity.setDensity(from.getDensity());
      if (Objects.isNull(entity.getLength())) entity.setLength(from.getLength());
      if (Objects.isNull(entity.getName())) entity.setName(from.getName());
      entity.validate();
      requireParentExists(db, hubAccess, entity);

      result.set(modelFrom(InstrumentAudio.class, executeCreate(db, INSTRUMENT_AUDIO, entity)));

      DAOCloner<Entity> cloner = new DAOCloner<>(result.get(), this);
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
    return fileStoreProvider.generateKey(prefix, waveformFileExtension);
  }

  /**
   Require parent instrument exists of a given possible entity in a DSL context

   @param db        DSL context
   @param hubAccess control
   @param entity    to validate
   @throws DAOException if parent does not exist
   */
  private void requireParentExists(DSLContext db, HubAccess hubAccess, InstrumentAudio entity) throws DAOException {
    if (hubAccess.isTopLevel())
      requireExists("Instrument", db.selectCount().from(INSTRUMENT)
        .where(INSTRUMENT.ID.eq(entity.getInstrumentId()))
        .fetchOne(0, int.class));
    else
      requireExists("Instrument", db.selectCount().from(INSTRUMENT)
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .and(INSTRUMENT.ID.eq(entity.getInstrumentId()))
        .fetchOne(0, int.class));
  }

  /**
   Read one record with the given DSL context,
   ensuring audio in instrument in library in hubAccess control account ids

   @param db        DSL context
   @param hubAccess control
   @param id        of record to read
   @return entity
   */
  private InstrumentAudio readOne(DSLContext db, HubAccess hubAccess, UUID id) throws DAOException {
    requireArtist(hubAccess);
    Record record;
    if (hubAccess.isTopLevel())
      record = db.selectFrom(INSTRUMENT_AUDIO)
        .where(INSTRUMENT_AUDIO.ID.eq(id))
        .fetchOne();
    else
      record = db.select(INSTRUMENT_AUDIO.fields())
        .from(INSTRUMENT_AUDIO)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(INSTRUMENT_AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(INSTRUMENT_AUDIO.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .fetchOne();
    requireExists("InstrumentAudio", record);
    return modelFrom(InstrumentAudio.class, record);
  }

}
