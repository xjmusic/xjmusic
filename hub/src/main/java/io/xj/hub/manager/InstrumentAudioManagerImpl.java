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
import io.xj.lib.util.Text;
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

import static io.xj.hub.Tables.ACCOUNT;
import static io.xj.hub.Tables.INSTRUMENT;
import static io.xj.hub.Tables.INSTRUMENT_AUDIO;
import static io.xj.hub.Tables.LIBRARY;

public class InstrumentAudioManagerImpl extends HubPersistenceServiceImpl<InstrumentAudio> implements InstrumentAudioManager {
  private static final String DEFAULT_EVENT = "X";
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

    // FUTURE https://www.pivotaltracker.com/story/show/170288602 Create instrument audio, provide waveform file extension as query parameter (checked by front-end after selecting the upload file)
//    String waveformFileExtension = env.getAudioFileExtension();
  }

  @Override
  public InstrumentAudio create(HubAccess access, InstrumentAudio rawAudio) throws ManagerException, JsonapiException, ValueException {
    var audio = validate(rawAudio);
    requireArtist(access);

    DSLContext db = dbProvider.getDSL();
    requireParentExists(db, access, audio);

    return modelFrom(InstrumentAudio.class,
      executeCreate(db, INSTRUMENT_AUDIO, audio));
  }

  @Override
  public Map<String, String> authorizeUpload(HubAccess access, UUID id, String extension) throws ManagerException, FileStoreException, ValueException, JsonapiException {
    DSLContext db = dbProvider.getDSL();
    var entity = readOne(db, access, id);

    // Cannot authorize upload of audio when generated key would overwrite another one in an instrument https://www.pivotaltracker.com/story/show/181848232
    var waveformKey = computeKey(db, entity, extension);
    requireNotExists(String.format("Generated key \"%s\" would overwrite existing audio- please change name of audio before uploading file.", waveformKey),
      db.select(INSTRUMENT_AUDIO.ID)
        .from(INSTRUMENT_AUDIO)
        .where(INSTRUMENT_AUDIO.INSTRUMENT_ID.eq(entity.getInstrumentId()))
        .and(INSTRUMENT_AUDIO.WAVEFORM_KEY.eq(waveformKey))
        .and(INSTRUMENT_AUDIO.ID.notEqual(id))
        .fetch());

    // Update the audio waveform key
    entity.setWaveformKey(waveformKey);
    update(access, id, entity);

    // Authorize the upload
    Map<String, String> uploadAuthorization = Maps.newConcurrentMap();
    S3UploadPolicy uploadPolicy = fileStoreProvider.generateAudioUploadPolicy();
    uploadAuthorization.put(KEY_WAVEFORM_KEY, waveformKey);
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
  public InstrumentAudio readOne(HubAccess access, UUID id) throws ManagerException {
    return readOne(dbProvider.getDSL(), access, id);
  }

  @Override
  public String computeKey(DSLContext db, InstrumentAudio instrumentAudio, String extension) throws ManagerException {
    var fields = db.select(ACCOUNT.NAME, LIBRARY.NAME, INSTRUMENT.NAME)
      .from(INSTRUMENT)
      .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
      .join(ACCOUNT).on(ACCOUNT.ID.eq(LIBRARY.ACCOUNT_ID))
      .where(INSTRUMENT.ID.eq(instrumentAudio.getInstrumentId()))
      .fetchOne();

    if (Objects.isNull(fields))
      throw new ManagerException(String.format("Failed to retrieve Account, Library, and Instrument[%s]", instrumentAudio.getInstrumentId()));

    return String.format("%s-%s-%s-%s.%s",
      Text.toAlphanumericHyphenated((String) fields.get(0)),
      Text.toAlphanumericHyphenated((String) fields.get(1)),
      Text.toAlphanumericHyphenated((String) fields.get(2)),
      Text.toAlphanumericHyphenated(instrumentAudio.getName()),
      extension);
  }

  @Override
  @Nullable
  public Collection<InstrumentAudio> readMany(HubAccess access, Collection<UUID> parentIds) throws ManagerException {
    requireArtist(access);
    if (access.isTopLevel())
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
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetch());
  }

  @Override
  public InstrumentAudio update(HubAccess access, UUID id, InstrumentAudio rawAudio) throws ManagerException, JsonapiException, ValueException {
    var audio = validate(rawAudio);
    requireArtist(access);

    DSLContext db = dbProvider.getDSL();

    requireParentExists(db, access, audio);

    if (Strings.isNullOrEmpty(audio.getWaveformKey()))
      audio.setWaveformKey(readOne(db, access, id).getWaveformKey());
    executeUpdate(db, INSTRUMENT_AUDIO, id, audio);

    return audio;
  }

  @Override
  public void destroy(HubAccess access, UUID id) throws ManagerException {
    DSLContext db = dbProvider.getDSL();

    requireExists("InstrumentAudio", readOne(db, access, id));

    db.deleteFrom(INSTRUMENT_AUDIO)
      .where(INSTRUMENT_AUDIO.ID.eq(id))
      .execute();
  }

  @Override
  public InstrumentAudio newInstance() {
    return new InstrumentAudio();
  }

  @Override
  public InstrumentAudio clone(HubAccess access, UUID rawCloneId, InstrumentAudio to) throws ManagerException {
    requireArtist(access);
    AtomicReference<InstrumentAudio> result = new AtomicReference<>();
    dbProvider.getDSL().transaction(ctx -> {
      DSLContext db = DSL.using(ctx);

      var from = readOne(db, access, rawCloneId);
      if (Objects.isNull(from))
        throw new ManagerException("Can't clone nonexistent InstrumentAudio");

      // When not set, clone inherits attribute values from original record
      entityFactory.setAllEmptyAttributes(from, to);
      var audio = validate(to);
      requireParentExists(db, access, audio);

      result.set(modelFrom(InstrumentAudio.class, executeCreate(db, INSTRUMENT_AUDIO, audio)));
    });
    return result.get();
  }

  /**
   Require parent instrument exists of a given possible entity in a DSL context

   @param db     DSL context
   @param access control
   @param entity to validate
   @throws ManagerException if parent does not exist
   */
  private void requireParentExists(DSLContext db, HubAccess access, InstrumentAudio entity) throws ManagerException {
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
  private InstrumentAudio readOne(DSLContext db, HubAccess access, UUID id) throws ManagerException {
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

      if (Objects.isNull(builder.getEvent()) || builder.getEvent().isEmpty())
        builder.setEvent(DEFAULT_EVENT);
      builder.setEvent(Text.toEvent(builder.getEvent()));

      return builder;

    } catch (ValueException e) {
      throw new ManagerException(e);
    }
  }

}
