// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.filestore.S3UploadPolicy;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static io.xj.hub.Tables.*;

public class InstrumentAudioManagerImpl extends HubPersistenceServiceImpl<InstrumentAudio> implements InstrumentAudioManager {
  private final FileStoreProvider fileStoreProvider;
  // key special resources (e.g. upload policy)
  String KEY_UPLOAD_ACCESS_KEY = "awsAccessKeyId";
  String KEY_UPLOAD_POLICY = "uploadPolicy";
  String KEY_UPLOAD_URL = "uploadUrl";
  String KEY_WAVEFORM_KEY = "waveformKey";
  String KEY_UPLOAD_POLICY_SIGNATURE = "uploadPolicySignature";
  String KEY_UPLOAD_BUCKET_NAME = "bucketName";
  String KEY_UPLOAD_ACL = "acl";

  @Inject
  public InstrumentAudioManagerImpl(
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider,
    FileStoreProvider fileStoreProvider
  ) {
    super(entityFactory, dbProvider);
    this.fileStoreProvider = fileStoreProvider;

    // FUTURE [#170288602] Create instrument audio, provide waveform file extension as query parameter (checked by front-end after selecting the upload file)
//    String waveformFileExtension = env.getAudioFileExtension();
  }

  @Override
  public InstrumentAudio create(HubAccess hubAccess, InstrumentAudio rawAudio) throws ManagerException, JsonapiException, ValueException {
    var audio = validate(rawAudio);
    requireArtist(hubAccess);

    DSLContext db = dbProvider.getDSL();
    requireParentExists(db, hubAccess, audio);

    return modelFrom(InstrumentAudio.class,
      executeCreate(db, INSTRUMENT_AUDIO, audio));
  }

  @Override
  public Map<String, String> authorizeUpload(HubAccess hubAccess, UUID id, String extension) throws ManagerException, FileStoreException {
    var entity = readOne(dbProvider.getDSL(), hubAccess, id);

    Map<String, String> uploadAuthorization = Maps.newConcurrentMap();
    S3UploadPolicy uploadPolicy = fileStoreProvider.generateAudioUploadPolicy();

    uploadAuthorization.put(KEY_WAVEFORM_KEY, generateKey(entity.getInstrumentId(), extension));
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
  public InstrumentAudio readOne(HubAccess hubAccess, UUID id) throws ManagerException {
    return readOne(dbProvider.getDSL(), hubAccess, id);
  }

  @Override
  @Nullable
  public Collection<InstrumentAudio> readMany(HubAccess hubAccess, Collection<UUID> parentIds) throws ManagerException {
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
  public InstrumentAudio update(HubAccess hubAccess, UUID id, InstrumentAudio rawAudio) throws ManagerException, JsonapiException, ValueException {
    var audio = validate(rawAudio);
    requireArtist(hubAccess);

    DSLContext db = dbProvider.getDSL();

    requireParentExists(db, hubAccess, audio);

    if (Strings.isNullOrEmpty(audio.getWaveformKey()))
      audio.setWaveformKey(readOne(db, hubAccess, id).getWaveformKey());
    executeUpdate(db, INSTRUMENT_AUDIO, id, audio);

    return audio;
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) throws ManagerException {
    DSLContext db = dbProvider.getDSL();

    requireExists("InstrumentAudio", readOne(db, hubAccess, id));

    db.deleteFrom(INSTRUMENT_AUDIO)
      .where(INSTRUMENT_AUDIO.ID.eq(id))
      .execute();
  }

  @Override
  public InstrumentAudio newInstance() {
    return new InstrumentAudio();
  }

  @Override
  public InstrumentAudio clone(HubAccess hubAccess, UUID rawCloneId, InstrumentAudio to) throws ManagerException {
    requireArtist(hubAccess);
    AtomicReference<InstrumentAudio> result = new AtomicReference<>();
    dbProvider.getDSL().transaction(ctx -> {
      DSLContext db = DSL.using(ctx);

      var from = readOne(db, hubAccess, rawCloneId);
      if (Objects.isNull(from))
        throw new ManagerException("Can't clone nonexistent InstrumentAudio");

      // When not set, clone inherits attribute values from original record
      entityFactory.setAllEmptyAttributes(from, to);
      var audio = validate(to);
      requireParentExists(db, hubAccess, audio);

      result.set(modelFrom(InstrumentAudio.class, executeCreate(db, INSTRUMENT_AUDIO, audio)));
    });
    return result.get();
  }

  /**
   General an Audio URL key

   @param instrumentId to generate URL key for
   @param extension    of key to general
   @return URL as string
   */
  private String generateKey(UUID instrumentId, String extension) {
    String prefix = String.format("instrument-%s-audio", instrumentId.toString());
    return fileStoreProvider.generateKey(prefix, extension);
  }

  /**
   Require parent instrument exists of a given possible entity in a DSL context

   @param db        DSL context
   @param hubAccess control
   @param entity    to validate
   @throws ManagerException if parent does not exist
   */
  private void requireParentExists(DSLContext db, HubAccess hubAccess, InstrumentAudio entity) throws ManagerException {
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
  private InstrumentAudio readOne(DSLContext db, HubAccess hubAccess, UUID id) throws ManagerException {
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

  /**
   Validate data

   @param builder to validate
   @throws ManagerException if invalid
   */
  public InstrumentAudio validate(InstrumentAudio builder) throws ManagerException {
    try {
      Values.require(builder.getInstrumentId(), "Instrument ID");

      if (Objects.isNull(builder.getName()) || builder.getName().isEmpty())
        throw new ValueException("Name is required.");

      if (Objects.isNull(builder.getWaveformKey()) || builder.getWaveformKey().isEmpty())
        builder.setWaveformKey("");

      if (Values.isEmpty(builder.getDensity()))
        builder.setDensity(0.5f);

      if (Values.isEmpty(builder.getTransientSeconds()))
        builder.setTransientSeconds(0.0f);

      if (Values.isEmpty(builder.getTotalBeats()))
        builder.setTotalBeats(1.0f);

      Values.require(builder.getTempo(), "Tempo");
      Values.requireNonZero(builder.getTempo(), "Tempo");

      return builder;

    } catch (ValueException e) {
      throw new ManagerException(e);
    }
  }

}
