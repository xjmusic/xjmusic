// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.filestore.S3UploadPolicy;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.music.Accidental;
import io.xj.lib.util.StringUtils;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.ValueUtils;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import org.jetbrains.annotations.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static io.xj.hub.Tables.ACCOUNT;
import static io.xj.hub.Tables.INSTRUMENT;
import static io.xj.hub.Tables.INSTRUMENT_AUDIO;
import static io.xj.hub.Tables.LIBRARY;

@Service
public class InstrumentAudioManagerImpl extends HubPersistenceServiceImpl implements InstrumentAudioManager {
  static final String DEFAULT_EVENT = "X";
  final InstrumentManager instrumentManager;
  final FileStoreProvider fileStoreProvider;
  // key special resources (e.g. upload policy)
  String KEY_UPLOAD_ACCESS_KEY = "awsAccessKeyId";
  String KEY_UPLOAD_POLICY = "uploadPolicy";
  String KEY_UPLOAD_URL = "uploadUrl";
  String KEY_WAVEFORM_KEY = "waveformKey";
  String KEY_UPLOAD_POLICY_SIGNATURE = "uploadPolicySignature";
  String KEY_UPLOAD_BUCKET_NAME = "bucketName";
  String KEY_UPLOAD_ACL = "acl";

  public InstrumentAudioManagerImpl(
    EntityFactory entityFactory,
    HubSqlStoreProvider sqlStoreProvider,
    FileStoreProvider fileStoreProvider,
    InstrumentManager instrumentManager
  ) {
    super(entityFactory, sqlStoreProvider);
    this.instrumentManager = instrumentManager;
    this.fileStoreProvider = fileStoreProvider;

    // FUTURE Create instrument audio, provide waveform file extension as query parameter (checked by front-end after selecting the upload file) https://www.pivotaltracker.com/story/show/170288602
//    String waveformFileExtension = env.getAudioFileExtension();
  }

  @Override
  public InstrumentAudio create(HubAccess access, InstrumentAudio rawAudio) throws ManagerException, JsonapiException, ValueException {
    var audio = validate(rawAudio);
    requireArtist(access);

    DSLContext db = sqlStoreProvider.getDSL();
    requireAccessToParent(access, audio);

    return modelFrom(InstrumentAudio.class,
      executeCreate(db, INSTRUMENT_AUDIO, audio));
  }

  @Override
  public Map<String, String> authorizeUpload(HubAccess access, UUID id, String extension) throws ManagerException, FileStoreException, ValueException, JsonapiException {
    DSLContext db = sqlStoreProvider.getDSL();

    var entity = readOne(db, access, id);
    var waveformKey = computeKey(db, entity, extension);

    try (var selectInstrumentAudio = db.select(INSTRUMENT_AUDIO.ID)) {
      // Cannot authorize upload of audio when generated key would overwrite another one in an instrument https://www.pivotaltracker.com/story/show/181848232
      requireNotExists(String.format("Generated key \"%s\" would overwrite existing audio- please change name of audio before uploading file.", waveformKey),
        selectInstrumentAudio
          .from(INSTRUMENT_AUDIO)
          .where(INSTRUMENT_AUDIO.INSTRUMENT_ID.eq(entity.getInstrumentId()))
          .and(INSTRUMENT_AUDIO.WAVEFORM_KEY.eq(waveformKey))
          .and(INSTRUMENT_AUDIO.ID.notEqual(id))
          .fetch());
    } catch (Exception e) {
      throw new ManagerException(e);
    }

    // Update the audio waveform key
    entity.setWaveformKey(waveformKey);
    update(access, id, entity);

    // Authorize the upload
    Map<String, String> uploadAuthorization = new ConcurrentHashMap<>();
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
    return readOne(sqlStoreProvider.getDSL(), access, id);
  }

  @Override
  public String computeKey(DSLContext db, InstrumentAudio instrumentAudio, String extension) throws ManagerException {
    try (var select = db.select(ACCOUNT.NAME, LIBRARY.NAME, INSTRUMENT.NAME);
         var joinLibrary = select
           .from(INSTRUMENT)
           .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID));
         var joinAccount = joinLibrary
           .join(ACCOUNT).on(ACCOUNT.ID.eq(LIBRARY.ACCOUNT_ID))) {
      var fields =
        joinAccount
          .where(INSTRUMENT.ID.eq(instrumentAudio.getInstrumentId()))
          .fetchOne();

      if (Objects.isNull(fields))
        throw new ManagerException(String.format("Failed to retrieve Account, Library, and Instrument[%s]", instrumentAudio.getInstrumentId()));

      return String.format("%s.%s",
        String.join("-",
          StringUtils.toAlphanumericHyphenated((String) fields.get(0)),
          StringUtils.toAlphanumericHyphenated((String) fields.get(1)),
          StringUtils.toAlphanumericHyphenated((String) fields.get(2)),
          StringUtils.toAlphanumericHyphenated(Accidental.replaceWithExplicit(instrumentAudio.getName())),
          StringUtils.toAlphanumericHyphenated(Accidental.replaceWithExplicit(instrumentAudio.getTones()))
        ),
        extension);

    } catch (Exception e) {
      throw new ManagerException(e);
    }
  }

  @Override
  @Nullable
  public Collection<InstrumentAudio> readMany(HubAccess access, Collection<UUID> parentIds) throws ManagerException {
    requireArtist(access);
    if (access.isTopLevel())
      try (var selectInstrumentAudio = sqlStoreProvider.getDSL().selectFrom(INSTRUMENT_AUDIO)) {
        return modelsFrom(InstrumentAudio.class,
          selectInstrumentAudio
            .where(INSTRUMENT_AUDIO.INSTRUMENT_ID.in(parentIds))
            .fetch());
      } catch (Exception e) {
        throw new ManagerException(e);
      }
    else
      try (var selectInstrumentAudio = sqlStoreProvider.getDSL().select(INSTRUMENT_AUDIO.fields());
           var joinInstrument = selectInstrumentAudio
             .from(INSTRUMENT_AUDIO)
             .join(INSTRUMENT).on(INSTRUMENT.ID.eq(INSTRUMENT_AUDIO.INSTRUMENT_ID));
           var joinLibrary = joinInstrument.join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))) {
        return modelsFrom(InstrumentAudio.class,
          joinLibrary
            .where(INSTRUMENT_AUDIO.INSTRUMENT_ID.in(parentIds))
            .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
            .fetch());
      } catch (Exception e) {
        throw new ManagerException(e);
      }
  }

  @Override
  public InstrumentAudio update(HubAccess access, UUID id, InstrumentAudio rawAudio) throws ManagerException, JsonapiException, ValueException {
    var audio = validate(rawAudio);
    requireArtist(access);

    DSLContext db = sqlStoreProvider.getDSL();

    requireAccessToParent(access, audio);

    if (StringUtils.isNullOrEmpty(audio.getWaveformKey()))
      audio.setWaveformKey(readOne(db, access, id).getWaveformKey());
    executeUpdate(db, INSTRUMENT_AUDIO, id, audio);

    return audio;
  }

  @Override
  public void destroy(HubAccess access, UUID id) throws ManagerException {
    DSLContext db = sqlStoreProvider.getDSL();

    requireExists("InstrumentAudio", readOne(db, access, id));

    try (var deleteInstrumentAudio = db.deleteFrom(INSTRUMENT_AUDIO)) {
      deleteInstrumentAudio.where(INSTRUMENT_AUDIO.ID.eq(id))
        .execute();
    } catch (Exception e) {
      throw new ManagerException(e);
    }
  }

  @Override
  public InstrumentAudio newInstance() {
    return new InstrumentAudio();
  }

  @Override
  public InstrumentAudio clone(HubAccess access, UUID rawCloneId, InstrumentAudio to) throws ManagerException {
    requireArtist(access);
    AtomicReference<InstrumentAudio> result = new AtomicReference<>();
    sqlStoreProvider.getDSL().transaction(ctx -> {
      DSLContext db = DSL.using(ctx);

      var from = readOne(db, access, rawCloneId);
      if (Objects.isNull(from))
        throw new ManagerException("Can't clone nonexistent InstrumentAudio");

      // When not set, clone inherits attribute values from original record
      entityFactory.setAllEmptyAttributes(from, to);
      var audio = validate(to);
      requireAccessToParent(access, audio);

      result.set(modelFrom(InstrumentAudio.class, executeCreate(db, INSTRUMENT_AUDIO, audio)));
    });
    return result.get();
  }

  /**
   * Require parent instrument exists of a given possible entity in a DSL context
   *
   * @param access control
   * @param entity to validate
   * @throws ManagerException if parent does not exist
   */
  void requireAccessToParent(HubAccess access, InstrumentAudio entity) throws ManagerException {
    instrumentManager.readOne(access, entity.getInstrumentId());
  }

  /**
   * Read one record with the given DSL context,
   * ensuring audio in instrument in library in access control account ids
   *
   * @param db     DSL context
   * @param access control
   * @param id     of record to read
   * @return entity
   */
  InstrumentAudio readOne(DSLContext db, HubAccess access, UUID id) throws ManagerException {
    requireArtist(access);
    Record record;
    if (access.isTopLevel())
      try (var selectInstrumentAudio = db.selectFrom(INSTRUMENT_AUDIO)) {
        record = selectInstrumentAudio
          .where(INSTRUMENT_AUDIO.ID.eq(id))
          .fetchOne();
      } catch (Exception e) {
        throw new ManagerException(e);
      }
    else
      try (var selectInstrumentAudio = db.select(INSTRUMENT_AUDIO.fields());
           var joinInstrument = selectInstrumentAudio
             .from(INSTRUMENT_AUDIO)
             .join(INSTRUMENT).on(INSTRUMENT.ID.eq(INSTRUMENT_AUDIO.INSTRUMENT_ID));
           var joinLibrary = joinInstrument
             .join(LIBRARY).on(LIBRARY.ID.eq(INSTRUMENT.LIBRARY_ID))) {
        record = joinLibrary
          .where(INSTRUMENT_AUDIO.ID.eq(id))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne();
        requireExists("InstrumentAudio", record);
      } catch (Exception e) {
        throw new ManagerException(e);
      }
    return modelFrom(InstrumentAudio.class, record);
  }

  /**
   * Validate data
   *
   * @param builder to validate
   * @throws ManagerException if invalid
   */
  public InstrumentAudio validate(InstrumentAudio builder) throws ManagerException {
    try {
      ValueUtils.require(builder.getInstrumentId(), "Instrument ID");

      if (Objects.isNull(builder.getName()) || builder.getName().isEmpty())
        throw new ValueException("Name is required.");

      if (Objects.isNull(builder.getWaveformKey()) || builder.getWaveformKey().isEmpty())
        builder.setWaveformKey("");

      if (ValueUtils.isEmpty(builder.getDensity()))
        builder.setDensity(0.5f);

      if (ValueUtils.isEmpty(builder.getTransientSeconds()))
        builder.setTransientSeconds(0.0f);

      if (ValueUtils.isEmpty(builder.getTotalBeats()))
        builder.setTotalBeats(1.0f);

      ValueUtils.require(builder.getTempo(), "Tempo");
      ValueUtils.requireNonZero(builder.getTempo(), "Tempo");

      if (Objects.isNull(builder.getEvent()) || builder.getEvent().isEmpty())
        builder.setEvent(DEFAULT_EVENT);
      builder.setEvent(StringUtils.toEvent(builder.getEvent()));

      return builder;

    } catch (ValueException e) {
      throw new ManagerException(e);
    }
  }

}
