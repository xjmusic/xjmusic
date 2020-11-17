// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.InstrumentAudio;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.filestore.S3UploadPolicy;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.access.HubAccess;
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

import static io.xj.service.hub.Tables.INSTRUMENT;
import static io.xj.service.hub.Tables.INSTRUMENT_AUDIO;
import static io.xj.service.hub.Tables.INSTRUMENT_AUDIO_CHORD;
import static io.xj.service.hub.Tables.INSTRUMENT_AUDIO_EVENT;
import static io.xj.service.hub.Tables.LIBRARY;

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
    String waveformFileExtension = config.getString("audio.fileExtension");
  }

  @Override
  public InstrumentAudio create(HubAccess hubAccess, InstrumentAudio rawAudio) throws DAOException, JsonApiException, ValueException {
    InstrumentAudio audio = validate(rawAudio.toBuilder()).build();
    requireArtist(hubAccess);

    DSLContext db = dbProvider.getDSL();
    requireParentExists(db, hubAccess, audio);

    return modelFrom(InstrumentAudio.class,
      executeCreate(db, INSTRUMENT_AUDIO, audio));
  }

  @Override
  public Map<String, String> authorizeUpload(HubAccess hubAccess, String id) throws DAOException, FileStoreException {
    InstrumentAudio entity = readOne(dbProvider.getDSL(), hubAccess, id);

    Map<String, String> uploadAuthorization = Maps.newConcurrentMap();
    S3UploadPolicy uploadPolicy = fileStoreProvider.generateAudioUploadPolicy();

    uploadAuthorization.put(KEY_WAVEFORM_KEY, generateKey(entity.getInstrumentId()));
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
  public InstrumentAudio readOne(HubAccess hubAccess, String id) throws DAOException {
    return readOne(dbProvider.getDSL(), hubAccess, id);
  }

  @Override
  @Nullable
  public Collection<InstrumentAudio> readMany(HubAccess hubAccess, Collection<String> parentIds) throws DAOException {
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
  public void update(HubAccess hubAccess, String id, InstrumentAudio rawAudio) throws DAOException, JsonApiException, ValueException {
    InstrumentAudio audio = validate(rawAudio.toBuilder()).build();
    requireArtist(hubAccess);

    DSLContext db = dbProvider.getDSL();

    requireParentExists(db, hubAccess, audio);

    if (Strings.isNullOrEmpty(audio.getWaveformKey()))
      executeUpdate(db, INSTRUMENT_AUDIO, id, audio.toBuilder()
        .setWaveformKey(readOne(db, hubAccess, id).getWaveformKey())
        .build());
    else
      executeUpdate(db, INSTRUMENT_AUDIO, id, audio);
  }

  @Override
  public void destroy(HubAccess hubAccess, String rawId) throws DAOException {
    UUID id = UUID.fromString(rawId);
    DSLContext db = dbProvider.getDSL();

    requireExists("InstrumentAudio", readOne(db, hubAccess, rawId));

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
    return InstrumentAudio.getDefaultInstance();
  }

  @Override
  public InstrumentAudio clone(HubAccess hubAccess, String rawCloneId, InstrumentAudio rawAudio) throws DAOException {
    requireArtist(hubAccess);
    AtomicReference<InstrumentAudio> result = new AtomicReference<>();
    dbProvider.getDSL().transaction(ctx -> {
      DSLContext db = DSL.using(ctx);

      InstrumentAudio from = readOne(db, hubAccess, rawCloneId);
      if (Objects.isNull(from))
        throw new DAOException("Can't clone nonexistent InstrumentAudio");

      // When not set, clone inherits attribute values from original record
      InstrumentAudio.Builder audioBuilder = rawAudio.toBuilder();
      if (Value.isEmpty(rawAudio.getWaveformKey())) audioBuilder.setWaveformKey(from.getWaveformKey());
      if (Value.isEmpty(rawAudio.getName())) audioBuilder.setName(from.getName());
      InstrumentAudio audio = validate(audioBuilder).build();
      requireParentExists(db, hubAccess, audio);

      result.set(modelFrom(InstrumentAudio.class, executeCreate(db, INSTRUMENT_AUDIO, audio)));
      UUID cloneId = UUID.fromString(rawCloneId);
      UUID sourceId = UUID.fromString(result.get().getId());

      DAOCloner<Object> cloner = new DAOCloner<>(result.get(), this);
      cloner.clone(db, INSTRUMENT_AUDIO_EVENT, INSTRUMENT_AUDIO_EVENT.ID, ImmutableSet.of(), INSTRUMENT_AUDIO_EVENT.INSTRUMENT_AUDIO_ID, cloneId, sourceId);
      cloner.clone(db, INSTRUMENT_AUDIO_CHORD, INSTRUMENT_AUDIO_CHORD.ID, ImmutableSet.of(), INSTRUMENT_AUDIO_CHORD.INSTRUMENT_AUDIO_ID, cloneId, sourceId);
    });
    return result.get();
  }

  /**
   General an Audio URL

   @param instrumentId to generate URL for
   @return URL as string
   */
  private String generateKey(String instrumentId) {
    String prefix = String.format("instrument-%s-audio", instrumentId);
    return fileStoreProvider.generateKey(prefix);
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
        .where(INSTRUMENT.ID.eq(UUID.fromString(entity.getInstrumentId())))
        .fetchOne(0, int.class));
    else
      requireExists("Instrument", db.selectCount().from(INSTRUMENT)
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .and(INSTRUMENT.ID.eq(UUID.fromString(entity.getInstrumentId())))
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
  private InstrumentAudio readOne(DSLContext db, HubAccess hubAccess, String id) throws DAOException {
    requireArtist(hubAccess);
    Record record;
    if (hubAccess.isTopLevel())
      record = db.selectFrom(INSTRUMENT_AUDIO)
        .where(INSTRUMENT_AUDIO.ID.eq(UUID.fromString(id)))
        .fetchOne();
    else
      record = db.select(INSTRUMENT_AUDIO.fields())
        .from(INSTRUMENT_AUDIO)
        .join(INSTRUMENT).on(INSTRUMENT.ID.eq(INSTRUMENT_AUDIO.INSTRUMENT_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))
        .where(INSTRUMENT_AUDIO.ID.eq(UUID.fromString(id)))
        .and(LIBRARY.ACCOUNT_ID.in(hubAccess.getAccountIds()))
        .fetchOne();
    requireExists("InstrumentAudio", record);
    return modelFrom(InstrumentAudio.class, record);
  }

  /**
   Validate data

   @param builder to validate
   @throws DAOException if invalid
   */
  public InstrumentAudio.Builder validate(InstrumentAudio.Builder builder) throws DAOException {
    try {
      Value.require(builder.getInstrumentId(), "Instrument ID");

      if (Objects.isNull(builder.getName()) || builder.getName().isEmpty())
        throw new ValueException("Name is required.");

      if (Objects.isNull(builder.getWaveformKey()) || builder.getWaveformKey().isEmpty())
        builder.setWaveformKey("");

      if (Value.isEmpty(builder.getDensity()))
        builder.setDensity(0.5d);

      if (Value.isEmpty(builder.getStart()))
        builder.setStart(0.0d);

      if (Value.isEmpty(builder.getLength()))
        builder.setLength(0.0d);

      Value.require(builder.getTempo(), "Tempo");
      Value.requireNonZero(builder.getTempo(), "Tempo");

      Value.require(builder.getPitch(), "Pitch");
      Value.requireNonZero(builder.getPitch(), "Pitch");

      return builder;

    } catch (ValueException e) {
      throw new DAOException(e);
    }
  }

}
