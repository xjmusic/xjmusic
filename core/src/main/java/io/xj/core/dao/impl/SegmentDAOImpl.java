// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import com.google.api.client.util.Maps;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.xj.core.access.impl.Access;
import io.xj.core.config.Config;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentFactory;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;
import io.xj.core.tables.records.SegmentRecord;
import io.xj.core.transport.GsonProvider;
import io.xj.core.util.Value;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.UpdateSetFirstStep;
import org.jooq.types.ULong;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.tables.Chain.CHAIN;
import static io.xj.core.tables.Segment.SEGMENT;

public class SegmentDAOImpl extends DAOImpl implements SegmentDAO {
  private final SegmentFactory segmentFactory;
  private final GsonProvider gsonProvider;
  private final AmazonProvider amazonProvider;

  @Inject
  public SegmentDAOImpl(
    SQLDatabaseProvider dbProvider,
    GsonProvider gsonProvider,
    AmazonProvider amazonProvider,
    SegmentFactory segmentFactory
  ) {
    this.gsonProvider = gsonProvider;
    this.amazonProvider = amazonProvider;
    this.dbProvider = dbProvider;
    this.segmentFactory = segmentFactory;
  }

  /**
   Update the state of a record

   @param db     context
   @param access control
   @param id     of record
   @param state  to update to
   @throws CoreException if a Business Rule is violated
   */
  private static void updateState(DSLContext db, Access access, ULong id, SegmentState state) throws CoreException {
    Map<Field, Object> fieldValues = ImmutableMap.of(
      SEGMENT.ID, id,
      SEGMENT.STATE, state.toString()
    );

    update(db, access, id, fieldValues);

    if (0 == executeUpdate(db, SEGMENT, fieldValues))
      throw new CoreException("No records updated.");
  }

  /**
   Update a record

   @param db          context
   @param access      control
   @param id          of record
   @param fieldValues to update with
   @throws CoreException if a Business Rule is violated
   */
  private static void update(DSLContext db, Access access, ULong id, Map<Field, Object> fieldValues) throws CoreException {
    requireTopLevel(access);

    // validate and cache to-state
    SegmentState toState = SegmentState.validate(fieldValues.get(SEGMENT.STATE).toString());

    // fetch existing segment; further logic is based on its current state
    SegmentRecord segmentRecord = db.selectFrom(SEGMENT).where(SEGMENT.ID.eq(id)).fetchOne();
    requireExists("Segment #" + id, segmentRecord);

    // logic based on existing Segment State
    SegmentState.protectTransition(SegmentState.validate(segmentRecord.getState()), toState);

    // [#128] cannot change chainId of a segment
    Object updateChainId = fieldValues.get(SEGMENT.CHAIN_ID);
    if (isNonNull(updateChainId) && !Objects.equals(ULong.valueOf(String.valueOf(updateChainId)), segmentRecord.getChainId()))
      throw new CoreException("cannot change chainId of a segment");

    // This "change from state to state" complexity
    // is required in order to prevent duplicate
    // state-changes of the same segment
    UpdateSetFirstStep<SegmentRecord> update = db.update(SEGMENT);
    fieldValues.forEach(update::set);
    int rowsAffected = update.set(SEGMENT.STATE, toState.toString())
      .where(SEGMENT.ID.eq(id))
      .and(SEGMENT.STATE.eq(segmentRecord.getState()))
      .execute();

    if (0 == rowsAffected)
      throw new CoreException("No records updated.");

  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private Segment readOne(DSLContext db, Access access, ULong id) throws CoreException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(SEGMENT)
        .where(SEGMENT.ID.eq(id))
        .fetchOne());
    else
      return modelFrom(db.select(SEGMENT.fields())
        .from(SEGMENT)
        .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
        .where(SEGMENT.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne());
  }

  /**
   Read id for the Segment in a Chain at a given offset, if present

   @param db      context
   @param access  control
   @param chainId to fetch a segment for
   @param offset  to fetch segment at
   @return record
   */
  @Nullable
  private Segment readOneAtChainOffset(DSLContext db, Access access, ULong chainId, ULong offset) throws CoreException {
    requireTopLevel(access);
    return modelFrom(db.selectFrom(SEGMENT)
      .where(SEGMENT.OFFSET.eq(offset))
      .and(SEGMENT.CHAIN_ID.eq(chainId))
      .fetchOne());
  }

  /**
   Fetch one Segment by chainId and state, if present

   @param db                 context
   @param access             control
   @param chainId            to find segment in
   @param segmentState       segmentState to find segment in
   @param segmentBeginBefore ahead to look for segments
   @return Segment if found
   @throws CoreException on failure
   */
  private Segment readOneInState(DSLContext db, Access access, ULong chainId, SegmentState segmentState, Instant segmentBeginBefore) throws CoreException {
    requireTopLevel(access);

    return modelFrom(db.select(SEGMENT.fields()).from(SEGMENT)
      .where(SEGMENT.CHAIN_ID.eq(chainId))
      .and(SEGMENT.STATE.eq(segmentState.toString()))
      .and(SEGMENT.BEGIN_AT.lessOrEqual(Timestamp.from(segmentBeginBefore)))
      .orderBy(SEGMENT.OFFSET.asc())
      .limit(1)
      .fetchOne());
  }

  /**
   Read all records in parent by id
   <p>
   [#235] Chain Segments can only be read N at a time, to avoid hanging the server or client

   @param db      context
   @param access  control
   @param chainId of parent
   @return array of records
   */
  private Collection<Segment> readAll(DSLContext db, Access access, Collection<ULong> chainId) throws CoreException {
    if (access.isTopLevel())
      return modelsFrom(db.select(SEGMENT.fields())
        .from(SEGMENT)
        .where(SEGMENT.CHAIN_ID.in(chainId))
        .orderBy(SEGMENT.OFFSET.desc())
        .limit(Config.limitSegmentReadSize())
        .fetch());
    else
      return modelsFrom(db.select(SEGMENT.fields())
        .from(SEGMENT)
        .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
        .where(SEGMENT.CHAIN_ID.in(chainId))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(SEGMENT.OFFSET.desc())
        .limit(Config.limitSegmentReadSize())
        .fetch());
  }

  /**
   Read all records in parent by chain embed key
   <p>
   [#150279540] Unauthenticated public Client wants to access a Chain by embed key (as alias for chain id) in order to provide data for playback.

   @param db            context
   @param chainEmbedKey of parent
   @return array of records
   */
  private Collection<Segment> readAll(DSLContext db, String chainEmbedKey) throws CoreException {
    return modelsFrom(db.select(SEGMENT.fields())
      .from(SEGMENT)
      .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
      .where(CHAIN.EMBED_KEY.eq(chainEmbedKey))
      .orderBy(SEGMENT.OFFSET.desc())
      .limit(Config.limitSegmentReadSize())
      .fetch());
  }

  /**
   Read all records in parent by id, beginning at a particular offset
   <p>
   [#235] Chain Segments can only be read N at a time, to avoid hanging the server or client
   so "from offset zero" means from offset 0 to offset N

   @param db         context
   @param access     control
   @param chainId    of parent
   @param fromOffset to read segments
   @return array of records
   */
  private Collection<Segment> readAllFromOffset(DSLContext db, Access access, ULong chainId, ULong fromOffset) throws CoreException {
    ULong maxOffset = ULong.valueOf(
      fromOffset.toBigInteger().add(
        BigInteger.valueOf(Config.limitSegmentReadSize())));
    return readAllFromToOffset(db, access, chainId, fromOffset, maxOffset);
  }

  /**
   Read all records in parent by id, beginning and ending at particular offsets

   @param db         context
   @param access     control
   @param chainId    of parent
   @param fromOffset to read segments
   @return array of records
   */
  private Collection<Segment> readAllFromToOffset(DSLContext db, Access access, ULong chainId, ULong fromOffset, ULong toOffset) throws CoreException {
    if (access.isTopLevel())
      return modelsFrom(db.select(SEGMENT.fields())
        .from(SEGMENT)
        .where(SEGMENT.CHAIN_ID.eq(chainId))
        .and(SEGMENT.OFFSET.greaterOrEqual(fromOffset))
        .and(SEGMENT.OFFSET.lessOrEqual(toOffset))
        .orderBy(SEGMENT.OFFSET.desc())
        .limit(Config.limitSegmentReadSize())
        .fetch());
    else
      return modelsFrom(db.select(SEGMENT.fields())
        .from(SEGMENT)
        .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
        .where(SEGMENT.CHAIN_ID.eq(chainId))
        .and(SEGMENT.OFFSET.greaterOrEqual(fromOffset))
        .and(SEGMENT.OFFSET.lessOrEqual(toOffset))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(SEGMENT.OFFSET.desc())
        .limit(Config.limitSegmentReadSize())
        .fetch());
  }

  /**
   Read all records in parent by id, beginning and ending at particular offsets

   @param db      context
   @param access  control
   @param chainId of parent
   @param state   of segments to read
   @return array of records
   */
  private Collection<Segment> readAllInState(DSLContext db, Access access, ULong chainId, SegmentState state) throws CoreException {
    requireTopLevel(access);

    return modelsFrom(db.select(SEGMENT.fields())
      .from(SEGMENT)
      .where(SEGMENT.CHAIN_ID.eq(chainId))
      .and(SEGMENT.STATE.eq(state.toString()))
      .orderBy(SEGMENT.OFFSET.desc())
      .limit(Config.limitSegmentReadSize())
      .fetch());
  }

  /**
   Read all records in parent by id, beginning at a particular offset
   <p>
   [#150279540] Unauthenticated public Client wants to access a Chain by embed key (as alias for chain id) in order to provide data for playback.

   @param db            context
   @param chainEmbedKey of parent
   @param fromOffset    to read segments
   @return array of records
   */
  private Collection<Segment> readAllFromOffset(DSLContext db, String chainEmbedKey, ULong fromOffset) throws CoreException {
    // so "from offset zero" means from offset 0 to offset N
    ULong maxOffset = ULong.valueOf(
      fromOffset.toBigInteger().add(
        BigInteger.valueOf(Config.limitSegmentReadSize())));

    return modelsFrom(db.select(SEGMENT.fields())
      .from(SEGMENT)
      .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
      .where(CHAIN.EMBED_KEY.eq(chainEmbedKey))
      .and(SEGMENT.OFFSET.greaterOrEqual(fromOffset))
      .and(SEGMENT.OFFSET.lessOrEqual(maxOffset))
      .orderBy(SEGMENT.OFFSET.desc())
      .limit(Config.limitSegmentReadSize())
      .fetch());
  }

  /**
   Read all Segments that are accessible, starting at a particular time in seconds UTC since epoch.
   limit buffer ahead seconds readable at once in environment configuration
   <p>
   [#278] Chain Player lives in navbar, and handles all playback (audio waveform, segment waveform, continuous chain) so the user always has central control over listening.

   @param db             context
   @param access         control
   @param chainId        of parent
   @param fromSecondsUTC to read segments
   @return array of records
   */
  private Collection<Segment> readAllFromSecondsUTC(DSLContext db, Access access, ULong chainId, Long fromSecondsUTC) throws CoreException {
    Instant from = Instant.ofEpochSecond(fromSecondsUTC);
    Instant maxBeginAt = from.plusSeconds(Config.playBufferAheadSeconds());
    Instant minEndAt = from.minusSeconds(Config.playBufferDelaySeconds());

    if (access.isTopLevel())
      return modelsFrom(db.select(SEGMENT.fields())
        .from(SEGMENT)
        .where(SEGMENT.CHAIN_ID.eq(chainId))
        .and(SEGMENT.BEGIN_AT.lessOrEqual(Timestamp.from(maxBeginAt)))
        .and(SEGMENT.END_AT.greaterOrEqual(Timestamp.from(minEndAt)))
        .orderBy(SEGMENT.OFFSET.desc())
        .limit(Config.limitSegmentReadSize())
        .fetch());
    else
      return modelsFrom(db.select(SEGMENT.fields())
        .from(SEGMENT)
        .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
        .where(SEGMENT.CHAIN_ID.eq(chainId))
        .and(SEGMENT.BEGIN_AT.lessOrEqual(Timestamp.from(maxBeginAt)))
        .and(SEGMENT.END_AT.greaterOrEqual(Timestamp.from(minEndAt)))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(SEGMENT.OFFSET.desc())
        .limit(Config.limitSegmentReadSize())
        .fetch());
  }

  /**
   Read all Segments that are accessible, starting at a particular time in seconds UTC since epoch.
   limit buffer ahead seconds readable at once in environment configuration
   <p>
   [#150279540] Unauthenticated public Client wants to access a Chain by embed key (as alias for chain id) in order to provide data for playback.

   @param db             context
   @param chainEmbedKey  of parent
   @param fromSecondsUTC to read segments
   @return array of records
   */
  private Collection<Segment> readAllFromSecondsUTC(DSLContext db, String chainEmbedKey, Long fromSecondsUTC) throws CoreException {
    Instant from = Instant.ofEpochSecond(fromSecondsUTC);
    Instant maxBeginAt = from.plusSeconds(Config.playBufferAheadSeconds());
    Instant minEndAt = from.minusSeconds(Config.playBufferDelaySeconds());

    return modelsFrom(db.select(SEGMENT.fields())
      .from(SEGMENT)
      .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
      .where(CHAIN.EMBED_KEY.eq(chainEmbedKey))
      .and(SEGMENT.BEGIN_AT.lessOrEqual(Timestamp.from(maxBeginAt)))
      .and(SEGMENT.END_AT.greaterOrEqual(Timestamp.from(minEndAt)))
      .orderBy(SEGMENT.OFFSET.desc())
      .limit(Config.limitSegmentReadSize())
      .fetch());
  }

  /**
   Transmogrify a jOOQ Result set into a Collection of POJO entities

   @param records to source values from
   @return entity after transmogrification
   @throws CoreException on failure to transmogrify
   */
  <R extends Record> Collection<Segment> modelsFrom(Iterable<R> records) throws CoreException {
    Collection<Segment> models = Lists.newArrayList();
    for (R record : records) {
      models.add(modelFrom(record));
    }
    return models;
  }

  /**
   Transmogrify the field-value pairs from a jOOQ record and set values on the corresponding POJO entity.

   @param record to source field-values from
   @return entity after transmogrification
   */
  <R extends Record> Segment modelFrom(R record) throws CoreException {
    Segment model = segmentFactory.newSegment();
    modelSetTransmogrified(record, model);
    return model;
  }


  /**
   Update a record using a model wrapper

   @param db     context
   @param access control
   @param id     of segment to update
   @param entity wrapper
   @throws CoreException on failure
   @throws CoreException on failure
   */
  private void updateAllFields(DSLContext db, Access access, ULong id, Segment entity) throws CoreException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    fieldValues.put(SEGMENT.ID, id);
    update(db, access, id, fieldValues);
  }

  /**
   Reverts a segment in Planned state, by updating it with a new, empty content. Only the segment messages remain, for purposes of debugging.
   [#158610991] Engineer wants a Segment to be reverted, and re-queued for fabrication, in the event that such a Segment has just failed its fabrication process, in order to ensure Chain fabrication fault tolerance

   @param db        context
   @param access    control
   @param segmentId of record
   @throws CoreException if a Business Rule is violated
   */
  private void revert(DSLContext db, Access access, ULong segmentId) throws CoreException {
    requireTopLevel(access);

    Segment segment = readOne(db, access, segmentId);
    segment.revert();
    updateAllFields(db, access, segmentId, segment);
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private Map<Field, Object> fieldValueMap(Segment entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(SEGMENT.CONTENT, gsonProvider.gson().toJson(entity.getContent()));
    fieldValues.put(SEGMENT.STATE, entity.getState());
    fieldValues.put(SEGMENT.BEGIN_AT, Timestamp.from(entity.getBeginAt().truncatedTo(ChronoUnit.MICROS)));
    Timestamp endTimestamp = Objects.nonNull(entity.getEndAt()) ?
      Timestamp.from(entity.getEndAt().truncatedTo(ChronoUnit.MICROS)) : null;
    fieldValues.put(SEGMENT.END_AT, valueOrNull(endTimestamp));
    fieldValues.put(SEGMENT.TOTAL, valueOrNull(entity.getTotal()));
    fieldValues.put(SEGMENT.DENSITY, valueOrNull(entity.getDensity()));
    fieldValues.put(SEGMENT.KEY, valueOrNull(entity.getKey()));
    fieldValues.put(SEGMENT.TEMPO, valueOrNull(entity.getTempo()));
    fieldValues.put(SEGMENT.WAVEFORM_KEY, valueOrNull(entity.getWaveformKey()));
    // Exclude SEGMENT.CHAIN_ID and SEGMENT.OFFSET because they are read-only
    return fieldValues;
  }

  @Override
  public Segment create(Access access, Segment entity) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Segment readOne(Access access, BigInteger id) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Segment readOneAtChainOffset(Access access, BigInteger chainId, BigInteger offset) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneAtChainOffset(tx.getContext(), access, ULong.valueOf(chainId), ULong.valueOf(offset)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Segment readOneInState(Access access, BigInteger chainId, SegmentState segmentState, Instant segmentBeginBefore) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneInState(tx.getContext(), access, ULong.valueOf(chainId), segmentState, segmentBeginBefore));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Segment> readAll(String chainEmbedKey) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), chainEmbedKey));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Segment> readAll(Access access, Collection<BigInteger> parentIds) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, uLongValuesOf(parentIds)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Segment> readAllFromOffset(Access access, BigInteger chainId, BigInteger fromOffset) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllFromOffset(tx.getContext(), access, ULong.valueOf(chainId), ULong.valueOf(fromOffset)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Segment> readAllFromToOffset(Access access, BigInteger chainId, BigInteger fromOffset, BigInteger toOffset) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    if (Value.isNegative(toOffset)) {
      return tx.success(Lists.newArrayList());
    }
    try {
      return tx.success(readAllFromToOffset(tx.getContext(), access, ULong.valueOf(chainId), Value.safeULong(fromOffset), ULong.valueOf(toOffset)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Segment> readAllInState(Access access, BigInteger chainId, SegmentState state) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInState(tx.getContext(), access, ULong.valueOf(chainId), state));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Segment> readAllFromOffset(String chainEmbedKey, BigInteger fromOffset) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllFromOffset(tx.getContext(), chainEmbedKey, ULong.valueOf(fromOffset)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Segment> readAllFromSecondsUTC(Access access, BigInteger chainId, Long fromSecondsUTC) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllFromSecondsUTC(tx.getContext(), access, ULong.valueOf(chainId), fromSecondsUTC));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Segment> readAllFromSecondsUTC(String chainEmbedKey, Long fromSecondsUTC) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllFromSecondsUTC(tx.getContext(), chainEmbedKey, fromSecondsUTC));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, Segment entity) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      updateAllFields(tx.getContext(), access, ULong.valueOf(id), entity);
      tx.success();
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void updateState(Access access, BigInteger id, SegmentState state) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      updateState(tx.getContext(), access, ULong.valueOf(id), state);
      tx.success();
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void revert(Access access, BigInteger id) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      revert(tx.getContext(), access, ULong.valueOf(id));
      tx.success();
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void destroy(Access access, BigInteger id) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      destroy(tx.getContext(), access, ULong.valueOf(id));
      tx.success();
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  /**
   Create a new record

   @param db     context
   @param access control
   @param entity for new record
   @return newly readMany record
   @throws CoreException if a Business Rule is violated
   */
  private Segment create(DSLContext db, Access access, Segment entity) throws CoreException {
    // top-level access
    requireTopLevel(access);

    entity.validate();
    Map<Field, Object> fieldValues = fieldValueMap(entity);

    // [#126] Segments are always readMany in PLANNED state
    fieldValues.put(SEGMENT.STATE, SegmentState.Planned);


    // Chain ID and offset are read-only, set at creation
    requireNotExists("Segment at same offset in Chain", db.selectCount().from(SEGMENT)
      .where(SEGMENT.CHAIN_ID.eq(ULong.valueOf(entity.getChainId())))
      .and(SEGMENT.OFFSET.eq(ULong.valueOf(entity.getOffset())))
      .fetchOne(0, int.class));
    fieldValues.put(SEGMENT.CHAIN_ID, entity.getChainId());
    fieldValues.put(SEGMENT.OFFSET, entity.getOffset());

    return modelFrom(executeCreate(db, SEGMENT, fieldValues));
  }

  /**
   Destroy a Segment, its child entities, and S3 object
   [#301] Segment destruction (by Eraseworker) should not spike database CPU

   @param db        context
   @param access    control
   @param segmentId to delete
   @throws CoreException if database failure
   @throws CoreException if not configured properly
   */
  private void destroy(DSLContext db, Access access, ULong segmentId) throws CoreException {
    requireTopLevel(access);

    // Segment
    SegmentRecord segment = db.selectFrom(SEGMENT)
      .where(SEGMENT.ID.eq(segmentId))
      .fetchOne();
    requireExists("Segment #" + segmentId, segment);

    // Only Delete segment waveform from S3 if non-null
    String waveformKey = segment.get(SEGMENT.WAVEFORM_KEY);
    if (Objects.nonNull(waveformKey)) {
      amazonProvider.deleteS3Object(
        Config.segmentFileBucket(),
        waveformKey);
    }

    // Delete Segment
    db.deleteFrom(SEGMENT)
      .where(SEGMENT.ID.eq(segmentId))
      .execute();
  }

}
