// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.config.Config;
import io.xj.core.config.Exposure;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.CancelException;
import io.xj.core.exception.ConfigException;
import io.xj.core.exception.DatabaseException;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;
import io.xj.core.tables.records.SegmentRecord;
import io.xj.core.transport.CSV;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.UpdateSetFirstStep;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.xj.core.Tables.SEGMENT_MEME;
import static io.xj.core.tables.Arrangement.ARRANGEMENT;
import static io.xj.core.tables.Chain.CHAIN;
import static io.xj.core.tables.Choice.CHOICE;
import static io.xj.core.tables.Segment.SEGMENT;
import static io.xj.core.tables.SegmentChord.SEGMENT_CHORD;
import static io.xj.core.tables.SegmentMessage.SEGMENT_MESSAGE;

public class SegmentDAOImpl extends DAOImpl implements SegmentDAO {
  private static final long MILLIS_PER_SECOND = 1000L;
  private final AmazonProvider amazonProvider;

  @Inject
  public SegmentDAOImpl(
    SQLDatabaseProvider dbProvider,
    AmazonProvider amazonProvider
  ) {
    this.amazonProvider = amazonProvider;
    this.dbProvider = dbProvider;
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private static Segment readOne(DSLContext db, Access access, ULong id) throws BusinessException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(SEGMENT)
        .where(SEGMENT.ID.eq(id))
        .fetchOne(), Segment.class);
    else
      return modelFrom(db.select(SEGMENT.fields())
        .from(SEGMENT)
        .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
        .where(SEGMENT.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), Segment.class);
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
  private static Segment readOneAtChainOffset(DSLContext db, Access access, ULong chainId, ULong offset) throws BusinessException {
    requireTopLevel(access);
    return modelFrom(db.selectFrom(SEGMENT)
      .where(SEGMENT.OFFSET.eq(offset))
      .and(SEGMENT.CHAIN_ID.eq(chainId))
      .fetchOne(), Segment.class);
  }

  /**
   Fetch one Segment by chainId and state, if present

   @param db                 context
   @param access             control
   @param chainId            to find segment in
   @param segmentState       segmentState to find segment in
   @param segmentBeginBefore ahead to look for segments
   @return Segment if found
   @throws BusinessException on failure
   */
  private static Segment readOneInState(DSLContext db, Access access, ULong chainId, SegmentState segmentState, Timestamp segmentBeginBefore) throws BusinessException {
    requireTopLevel(access);

    return modelFrom(db.select(SEGMENT.fields()).from(SEGMENT)
      .where(SEGMENT.CHAIN_ID.eq(chainId))
      .and(SEGMENT.STATE.eq(segmentState.toString()))
      .and(SEGMENT.BEGIN_AT.lessOrEqual(segmentBeginBefore))
      .orderBy(SEGMENT.OFFSET.asc())
      .limit(1)
      .fetchOne(), Segment.class);
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
  private static Collection<Segment> readAll(DSLContext db, Access access, Collection<ULong> chainId) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.select(SEGMENT.fields())
        .from(SEGMENT)
        .where(SEGMENT.CHAIN_ID.in(chainId))
        .orderBy(SEGMENT.OFFSET.desc())
        .limit(Config.limitSegmentReadSize())
        .fetch(), Segment.class);
    else
      return modelsFrom(db.select(SEGMENT.fields())
        .from(SEGMENT)
        .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
        .where(SEGMENT.CHAIN_ID.in(chainId))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(SEGMENT.OFFSET.desc())
        .limit(Config.limitSegmentReadSize())
        .fetch(), Segment.class);
  }

  /**
   Read all records in parent by chain embed key
   <p>
   [#150279540] Unauthenticated public Client wants to access a Chain by embed key (as alias for chain id) in order to provide data for playback.

   @param db            context
   @param chainEmbedKey of parent
   @return array of records
   */
  private static Collection<Segment> readAll(DSLContext db, String chainEmbedKey) throws BusinessException {
    return modelsFrom(db.select(SEGMENT.fields())
      .from(SEGMENT)
      .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
      .where(CHAIN.EMBED_KEY.eq(chainEmbedKey))
      .orderBy(SEGMENT.OFFSET.desc())
      .limit(Config.limitSegmentReadSize())
      .fetch(), Segment.class);
  }

  /**
   Read all records in parent by id, beginning at a particular offset
   <p>
   [#235] Chain Segments can only be read N at a time, to avoid hanging the server or client

   @param db         context
   @param access     control
   @param chainId    of parent
   @param fromOffset to read segments
   @return array of records
   */
  private static Collection<Segment> readAllFromOffset(DSLContext db, Access access, ULong chainId, ULong fromOffset) throws BusinessException {
    // so "from offset zero" means from offset 0 to offset N
    ULong maxOffset = ULong.valueOf(
      fromOffset.toBigInteger().add(
        BigInteger.valueOf((long) Config.limitSegmentReadSize())));

    if (access.isTopLevel())
      return modelsFrom(db.select(SEGMENT.fields())
        .from(SEGMENT)
        .where(SEGMENT.CHAIN_ID.eq(chainId))
        .and(SEGMENT.OFFSET.greaterOrEqual(fromOffset))
        .and(SEGMENT.OFFSET.lessOrEqual(maxOffset))
        .orderBy(SEGMENT.OFFSET.desc())
        .limit(Config.limitSegmentReadSize())
        .fetch(), Segment.class);
    else
      return modelsFrom(db.select(SEGMENT.fields())
        .from(SEGMENT)
        .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
        .where(SEGMENT.CHAIN_ID.eq(chainId))
        .and(SEGMENT.OFFSET.greaterOrEqual(fromOffset))
        .and(SEGMENT.OFFSET.lessOrEqual(maxOffset))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(SEGMENT.OFFSET.desc())
        .limit(Config.limitSegmentReadSize())
        .fetch(), Segment.class);
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
  private static Collection<Segment> readAllFromOffset(DSLContext db, String chainEmbedKey, ULong fromOffset) throws BusinessException {
    // so "from offset zero" means from offset 0 to offset N
    ULong maxOffset = ULong.valueOf(
      fromOffset.toBigInteger().add(
        BigInteger.valueOf((long) Config.limitSegmentReadSize())));

    return modelsFrom(db.select(SEGMENT.fields())
      .from(SEGMENT)
      .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
      .where(CHAIN.EMBED_KEY.eq(chainEmbedKey))
      .and(SEGMENT.OFFSET.greaterOrEqual(fromOffset))
      .and(SEGMENT.OFFSET.lessOrEqual(maxOffset))
      .orderBy(SEGMENT.OFFSET.desc())
      .limit(Config.limitSegmentReadSize())
      .fetch(), Segment.class);
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
  private static Collection<Segment> readAllFromSecondsUTC(DSLContext db, Access access, ULong chainId, ULong fromSecondsUTC) throws BusinessException {
    // play buffer delay/ahead seconds
    Instant from = new Date(fromSecondsUTC.longValue() * MILLIS_PER_SECOND).toInstant();
    Timestamp maxBeginAt = Timestamp.from(from.plusSeconds((long) Config.playBufferAheadSeconds()));
    Timestamp minEndAt = Timestamp.from(from.minusSeconds((long) Config.playBufferDelaySeconds()));

    if (access.isTopLevel())
      return modelsFrom(db.select(SEGMENT.fields())
        .from(SEGMENT)
        .where(SEGMENT.CHAIN_ID.eq(chainId))
        .and(SEGMENT.BEGIN_AT.lessOrEqual(maxBeginAt))
        .and(SEGMENT.END_AT.greaterOrEqual(minEndAt))
        .orderBy(SEGMENT.OFFSET.desc())
        .limit(Config.limitSegmentReadSize())
        .fetch(), Segment.class);
    else
      return modelsFrom(db.select(SEGMENT.fields())
        .from(SEGMENT)
        .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
        .where(SEGMENT.CHAIN_ID.eq(chainId))
        .and(SEGMENT.BEGIN_AT.lessOrEqual(maxBeginAt))
        .and(SEGMENT.END_AT.greaterOrEqual(minEndAt))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(SEGMENT.OFFSET.desc())
        .limit(Config.limitSegmentReadSize())
        .fetch(), Segment.class);
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
  private static Collection<Segment> readAllFromSecondsUTC(DSLContext db, String chainEmbedKey, ULong fromSecondsUTC) throws BusinessException {
    // play buffer delay/ahead seconds
    Instant from = new Date(fromSecondsUTC.longValue() * MILLIS_PER_SECOND).toInstant();
    Timestamp maxBeginAt = Timestamp.from(from.plusSeconds((long) Config.playBufferAheadSeconds()));
    Timestamp minEndAt = Timestamp.from(from.minusSeconds((long) Config.playBufferDelaySeconds()));

    return modelsFrom(db.select(SEGMENT.fields())
      .from(SEGMENT)
      .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
      .where(CHAIN.EMBED_KEY.eq(chainEmbedKey))
      .and(SEGMENT.BEGIN_AT.lessOrEqual(maxBeginAt))
      .and(SEGMENT.END_AT.greaterOrEqual(minEndAt))
      .orderBy(SEGMENT.OFFSET.desc())
      .limit(Config.limitSegmentReadSize())
      .fetch(), Segment.class);
  }

  /**
   Update a record using a model wrapper

   @param db     context
   @param access control
   @param id     of segment to update
   @param entity wrapper
   @throws BusinessException on failure
   @throws DatabaseException on failure
   */
  private static void updateAllFields(DSLContext db, Access access, ULong id, Segment entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    fieldValues.put(SEGMENT.ID, id);
    update(db, access, id, fieldValues);
  }

  /**
   Update the state of a record

   @param db     context
   @param access control
   @param id     of record
   @param state  to update to
   @throws BusinessException if a Business Rule is violated
   */
  private static void updateState(DSLContext db, Access access, ULong id, SegmentState state) throws Exception {
    Map<Field, Object> fieldValues = ImmutableMap.of(
      SEGMENT.ID, id,
      SEGMENT.STATE, state.toString()
    );

    update(db, access, id, fieldValues);

    if (0 == executeUpdate(db, SEGMENT, fieldValues))
      throw new BusinessException("No records updated.");
  }

  /**
   Update a record

   @param db          context
   @param access      control
   @param id          of record
   @param fieldValues to update with
   @throws BusinessException if a Business Rule is violated
   */
  private static void update(DSLContext db, Access access, ULong id, Map<Field, Object> fieldValues) throws BusinessException, CancelException {
    requireTopLevel(access);

    // validate and cache to-state
    SegmentState toState = SegmentState.validate(fieldValues.get(SEGMENT.STATE).toString());

    // fetch existing segment; further logic is based on its current state
    SegmentRecord segmentRecord = db.selectFrom(SEGMENT).where(SEGMENT.ID.eq(id)).fetchOne();
    requireExists("Segment #" + id, segmentRecord);

    // logic based on existing Segment State
    switch (SegmentState.validate(segmentRecord.getState())) {

      case Planned:
        onlyAllowTransitions(toState, SegmentState.Planned, SegmentState.Crafting);
        break;

      case Crafting:
        onlyAllowTransitions(toState, SegmentState.Crafting, SegmentState.Crafted, SegmentState.Dubbing, SegmentState.Failed);
        break;

      case Crafted:
        onlyAllowTransitions(toState, SegmentState.Crafted, SegmentState.Dubbing);
        break;

      case Dubbing:
        onlyAllowTransitions(toState, SegmentState.Dubbing, SegmentState.Dubbed, SegmentState.Failed);
        break;

      case Dubbed:
        onlyAllowTransitions(toState, SegmentState.Dubbed);
        break;

      case Failed:
        onlyAllowTransitions(toState, SegmentState.Failed);
        break;

      default:
        onlyAllowTransitions(toState, SegmentState.Planned);
        break;
    }

    // [#128] cannot change chainId of a segment
    Object updateChainId = fieldValues.get(SEGMENT.CHAIN_ID);
    if (isNonNull(updateChainId) && !Objects.equals(ULong.valueOf(String.valueOf(updateChainId)), segmentRecord.getChainId()))
      throw new BusinessException("cannot change chainId of a segment");

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
      throw new BusinessException("No records updated.");

  }

  /**
   Require state is in an array of states

   @param toState       to check
   @param allowedStates required to be in
   @throws CancelException if not in required states
   */
  private static void onlyAllowTransitions(SegmentState toState, SegmentState... allowedStates) throws CancelException {
    List<String> allowedStateNames = Lists.newArrayList();
    for (SegmentState search : allowedStates) {
      allowedStateNames.add(search.toString());
      if (Objects.equals(search, toState)) {
        return;
      }
    }
    throw new CancelException(String.format("transition to %s not in allowed (%s)",
      toState, CSV.join(allowedStateNames)));
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(Segment entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(SEGMENT.STATE, entity.getState());
    fieldValues.put(SEGMENT.BEGIN_AT, entity.getBeginAt());
    fieldValues.put(SEGMENT.END_AT, valueOrNull(entity.getEndAt()));
    fieldValues.put(SEGMENT.TOTAL, valueOrNull(entity.getTotal()));
    fieldValues.put(SEGMENT.DENSITY, valueOrNull(entity.getDensity()));
    fieldValues.put(SEGMENT.KEY, valueOrNull(entity.getKey()));
    fieldValues.put(SEGMENT.TEMPO, valueOrNull(entity.getTempo()));
    // Exclude SEGMENT.WAVEFORM_KEY, SEGMENT.CHAIN_ID and SEGMENT.OFFSET because they are read-only
    return fieldValues;
  }

  @Override
  public Segment create(Access access, Segment entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Segment readOne(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Segment readOneAtChainOffset(Access access, BigInteger chainId, BigInteger offset) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneAtChainOffset(tx.getContext(), access, ULong.valueOf(chainId), ULong.valueOf(offset)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Nullable
  @Override
  public Segment readOneInState(Access access, BigInteger chainId, SegmentState segmentState, Timestamp segmentBeginBefore) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneInState(tx.getContext(), access, ULong.valueOf(chainId), segmentState, segmentBeginBefore));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Segment> readAll(String chainEmbedKey) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), chainEmbedKey));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Segment> readAll(Access access, Collection<BigInteger> parentIds) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, uLongValuesOf(parentIds)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Segment> readAllFromOffset(Access access, BigInteger chainId, BigInteger fromOffset) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllFromOffset(tx.getContext(), access, ULong.valueOf(chainId), ULong.valueOf(fromOffset)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Segment> readAllFromOffset(String chainEmbedKey, BigInteger fromOffset) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllFromOffset(tx.getContext(), chainEmbedKey, ULong.valueOf(fromOffset)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Segment> readAllFromSecondsUTC(Access access, BigInteger chainId, BigInteger fromSecondsUTC) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllFromSecondsUTC(tx.getContext(), access, ULong.valueOf(chainId), ULong.valueOf(fromSecondsUTC)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Segment> readAllFromSecondsUTC(String chainEmbedKey, BigInteger fromSecondsUTC) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllFromSecondsUTC(tx.getContext(), chainEmbedKey, ULong.valueOf(fromSecondsUTC)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, Segment entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      updateAllFields(tx.getContext(), access, ULong.valueOf(id), entity);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void updateState(Access access, BigInteger id, SegmentState state) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      updateState(tx.getContext(), access, ULong.valueOf(id), state);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void destroy(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      destroy(tx.getContext(), access, ULong.valueOf(id));
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  /**
   Create a new record

   @param db     context
   @param access control
   @param entity for new record
   @return newly readMany record
   @throws BusinessException if a Business Rule is violated
   */
  private Segment create(DSLContext db, Access access, Segment entity) throws Exception {
    // top-level access
    requireTopLevel(access);

    entity.validate();
    Map<Field, Object> fieldValues = fieldValueMap(entity);

    // [#126] Segments are always readMany in PLANNED state
    fieldValues.put(SEGMENT.STATE, SegmentState.Planned);

    // [#267] Segment has `waveform_key` referencing xj-segment-* S3 bucket object key
    fieldValues.put(SEGMENT.WAVEFORM_KEY, generateKey(entity.getChainId()));

    // Chain ID and offset are read-only, set at creation
    requireNotExists("Segment at same offset in Chain", db.selectCount().from(SEGMENT)
      .where(SEGMENT.CHAIN_ID.eq(ULong.valueOf(entity.getChainId())))
      .and(SEGMENT.OFFSET.eq(ULong.valueOf(entity.getOffset())))
      .fetchOne(0, int.class));
    fieldValues.put(SEGMENT.CHAIN_ID, entity.getChainId());
    fieldValues.put(SEGMENT.OFFSET, entity.getOffset());

    return modelFrom(executeCreate(db, SEGMENT, fieldValues), Segment.class);
  }

  /**
   General a Segment URL

   @param chainId to generate URL for
   @return URL as string
   */
  private String generateKey(BigInteger chainId) {
    return amazonProvider.generateKey(
      Exposure.FILE_CHAIN + Exposure.FILE_SEPARATOR +
        chainId + Exposure.FILE_SEPARATOR +
        Exposure.FILE_SEGMENT, Segment.FILE_EXTENSION);
  }

  /**
   Destroy a Segment, its child entities, and S3 object
   [#301] Segment destruction (by Eraseworker) should not spike database CPU

   @param db        context
   @param access    control
   @param segmentId to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private void destroy(DSLContext db, Access access, ULong segmentId) throws Exception {
    requireTopLevel(access);

    // Segment
    SegmentRecord segment = db.selectFrom(SEGMENT)
      .where(SEGMENT.ID.eq(segmentId))
      .fetchOne();
    requireExists("Segment #" + segmentId, segment);

    // Only Delete segment waveform from S3 if non-null
    String waveformKey = segment.get(SEGMENT.WAVEFORM_KEY);
    if (Objects.nonNull(waveformKey))
      amazonProvider.deleteS3Object(
        Config.segmentFileBucket(),
        waveformKey);

    // Read all choice id
    List<ULong> choiceIds = db.select(CHOICE.ID).from(CHOICE)
      .where(CHOICE.SEGMENT_ID.eq(segmentId)).fetch().stream().map(record -> record.get(CHOICE.ID)).collect(Collectors.toList());

    // Read all arrangement id
    List<ULong> arrangementIds = db.select(ARRANGEMENT.ID).from(ARRANGEMENT)
      .where(ARRANGEMENT.CHOICE_ID.in(choiceIds)).fetch().stream().map(record -> record.get(ARRANGEMENT.ID)).collect(Collectors.toList());

    // Delete Arrangements
    db.deleteFrom(ARRANGEMENT)
      .where(ARRANGEMENT.ID.in(arrangementIds))
      .execute();

    // Delete Choices
    db.deleteFrom(CHOICE)
      .where(CHOICE.SEGMENT_ID.eq(segmentId)).execute();

    // Delete Segment Chords
    db.deleteFrom(SEGMENT_CHORD)
      .where(SEGMENT_CHORD.SEGMENT_ID.eq(segmentId))
      .execute();

    // Delete Segment Memes
    db.deleteFrom(SEGMENT_MEME)
      .where(SEGMENT_MEME.SEGMENT_ID.eq(segmentId))
      .execute();

    // Delete Segment Messages
    db.deleteFrom(SEGMENT_MESSAGE)
      .where(SEGMENT_MESSAGE.SEGMENT_ID.eq(segmentId))
      .execute();

    // Delete Segment
    db.deleteFrom(SEGMENT)
      .where(SEGMENT.ID.eq(segmentId))
      .execute();

  }

}
