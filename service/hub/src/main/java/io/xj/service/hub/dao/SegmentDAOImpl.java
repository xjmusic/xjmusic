// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.dao;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.lib.rest_api.PayloadFactory;
import io.xj.lib.rest_api.RestApiException;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.entity.Entity;
import io.xj.service.hub.persistence.AmazonProvider;
import io.xj.service.hub.model.Chain;
import io.xj.service.hub.model.Segment;
import io.xj.service.hub.model.SegmentChoice;
import io.xj.service.hub.model.SegmentChoiceArrangement;
import io.xj.service.hub.model.SegmentChoiceArrangementPick;
import io.xj.service.hub.model.SegmentChord;
import io.xj.service.hub.model.SegmentMeme;
import io.xj.service.hub.model.SegmentMessage;
import io.xj.service.hub.model.SegmentState;
import io.xj.service.hub.persistence.SQLDatabaseProvider;
import io.xj.service.hub.tables.records.SegmentRecord;
import org.jooq.DSLContext;
import org.jooq.TableRecord;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.xj.service.hub.Tables.CHAIN;
import static io.xj.service.hub.Tables.SEGMENT;
import static io.xj.service.hub.Tables.SEGMENT_CHOICE;
import static io.xj.service.hub.Tables.SEGMENT_CHOICE_ARRANGEMENT;
import static io.xj.service.hub.Tables.SEGMENT_CHOICE_ARRANGEMENT_PICK;
import static io.xj.service.hub.Tables.SEGMENT_CHORD;
import static io.xj.service.hub.Tables.SEGMENT_MEME;
import static io.xj.service.hub.Tables.SEGMENT_MESSAGE;

public class SegmentDAOImpl extends DAOImpl<Segment> implements SegmentDAO {
  private final AmazonProvider amazonProvider;
  private final int playBufferAheadSeconds;
  private final int playBufferDelaySeconds;
  private final String segmentFileBucket;
  private int limitSegmentReadSize;

  @Inject
  public SegmentDAOImpl(
    PayloadFactory payloadFactory,
    SQLDatabaseProvider dbProvider,
    AmazonProvider amazonProvider,
    Config config
  ) {
    super(payloadFactory);
    this.amazonProvider = amazonProvider;
    this.dbProvider = dbProvider;

    playBufferAheadSeconds = config.getInt("play.bufferAheadSeconds");
    playBufferDelaySeconds = config.getInt("play.bufferDelaySeconds");
    limitSegmentReadSize = config.getInt("segment.limitReadSize");
    segmentFileBucket = config.getString("segment.fileBucket");
  }

  @Override
  public Segment create(Access access, Segment entity) throws HubException, RestApiException, ValueException {
// top-level access
    requireTopLevel(access);

    entity.validate();// [#126] Segments are always readMany in PLANNED state
    entity.setStateEnum(SegmentState.Planned);

    // Chain ID and offset are read-only, set at creation
    requireNotExists("Segment at same offset in Chain", dbProvider.getDSL().selectCount().from(SEGMENT)
      .where(SEGMENT.CHAIN_ID.eq(entity.getChainId()))
      .and(SEGMENT.OFFSET.eq(entity.getOffset()))
      .fetchOne(0, int.class));

    return modelFrom(Segment.class, executeCreate(dbProvider.getDSL(), SEGMENT, entity));
  }

  @Override
  public Segment readOne(Access access, UUID id) throws HubException {
    if (access.isTopLevel())
      return modelFrom(Segment.class, dbProvider.getDSL().selectFrom(SEGMENT)
        .where(SEGMENT.ID.eq(id))
        .fetchOne());
    else
      return modelFrom(Segment.class, dbProvider.getDSL().select(SEGMENT.fields())
        .from(SEGMENT)
        .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
        .where(SEGMENT.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne());
  }

  @Override
  public Segment readOneAtChainOffset(Access access, UUID chainId, Long offset) throws HubException {
    requireTopLevel(access);
    return modelFrom(Segment.class, dbProvider.getDSL().selectFrom(SEGMENT)
      .where(SEGMENT.OFFSET.eq(offset))
      .and(SEGMENT.CHAIN_ID.eq(chainId))
      .fetchOne());
  }

  @Override
  public Segment readOneInState(Access access, UUID chainId, SegmentState segmentState, Instant segmentBeginBefore) throws HubException {
    requireTopLevel(access);
    return modelFrom(Segment.class, dbProvider.getDSL().select(SEGMENT.fields()).from(SEGMENT)
      .where(SEGMENT.CHAIN_ID.eq(chainId))
      .and(SEGMENT.STATE.eq(segmentState.toString()))
      .and(SEGMENT.BEGIN_AT.lessOrEqual(Timestamp.from(segmentBeginBefore)))
      .orderBy(SEGMENT.OFFSET.asc())
      .limit(1)
      .fetchOne());
  }

  @Override
  public Collection<Segment> readAll(String chainEmbedKey) throws HubException {
    return modelsFrom(Segment.class, dbProvider.getDSL().select(SEGMENT.fields())
      .from(SEGMENT)
      .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
      .where(CHAIN.EMBED_KEY.eq(Chain.formatEmbedKey(chainEmbedKey)))
      .orderBy(SEGMENT.OFFSET.desc())
      .limit(limitSegmentReadSize)
      .fetch());
  }

  @Override
  public <N extends Entity> Collection<N> readAllSubEntities(Access access, Collection<UUID> segmentIds, Boolean includePicks) throws HubException {
    requireTopLevel(access);
    DSLContext db = dbProvider.getDSL();
    Collection<N> entities = Lists.newArrayList();

    if (includePicks)
      modelsFrom(SegmentChoiceArrangementPick.class, db.select(SEGMENT_CHOICE_ARRANGEMENT_PICK.fields())
        .from(SEGMENT_CHOICE_ARRANGEMENT_PICK)
        .where(SEGMENT_CHOICE_ARRANGEMENT_PICK.SEGMENT_ID.in(segmentIds))
        .fetch()).forEach(e -> entities.add((N) e));

    modelsFrom(SegmentChoiceArrangement.class, db.select(SEGMENT_CHOICE_ARRANGEMENT.fields())
      .from(SEGMENT_CHOICE_ARRANGEMENT)
      .where(SEGMENT_CHOICE_ARRANGEMENT.SEGMENT_ID.in(segmentIds))
      .fetch()).forEach(e -> entities.add((N) e));

    modelsFrom(SegmentChoice.class, db.select(SEGMENT_CHOICE.fields())
      .from(SEGMENT_CHOICE)
      .where(SEGMENT_CHOICE.SEGMENT_ID.in(segmentIds))
      .fetch()).forEach(e -> entities.add((N) e));

    modelsFrom(SegmentMeme.class, db.select(SEGMENT_MEME.fields())
      .from(SEGMENT_MEME)
      .where(SEGMENT_MEME.SEGMENT_ID.in(segmentIds))
      .fetch()).forEach(e -> entities.add((N) e));

    modelsFrom(SegmentChord.class, db.select(SEGMENT_CHORD.fields())
      .from(SEGMENT_CHORD)
      .where(SEGMENT_CHORD.SEGMENT_ID.in(segmentIds))
      .fetch()).forEach(e -> entities.add((N) e));

    return entities;

  }

  @Override
  public <N extends Entity> void createAllSubEntities(Access access, Collection<N> entities) throws HubException, RestApiException {
    requireTopLevel(access);
    DSLContext db = dbProvider.getDSL();
    Collection<? extends TableRecord<?>> records = Lists.newArrayList();
    records.addAll(recordsFrom(db, SEGMENT_MEME, filter(entities, SegmentMeme.class)));
    records.addAll(recordsFrom(db, SEGMENT_CHORD, filter(entities, SegmentChord.class)));
    records.addAll(recordsFrom(db, SEGMENT_MESSAGE, filter(entities, SegmentMessage.class)));
    records.addAll(recordsFrom(db, SEGMENT_CHOICE, filter(entities, SegmentChoice.class))); // before arrangement
    records.addAll(recordsFrom(db, SEGMENT_CHOICE_ARRANGEMENT, filter(entities, SegmentChoiceArrangement.class))); // before pick
    records.addAll(recordsFrom(db, SEGMENT_CHOICE_ARRANGEMENT_PICK, filter(entities, SegmentChoiceArrangementPick.class)));
    int[] rows = db.batchInsert(records).execute();
    if (!Objects.equals(rows.length, entities.size()))
      throw new HubException(String.format("Only inserted %d of %d intended rows", rows.length, entities.size()));

  }

  @Override
  public Collection<Segment> readMany(Access access, Collection<UUID> parentIds) throws HubException {
    if (access.isTopLevel())
      return modelsFrom(Segment.class, dbProvider.getDSL().select(SEGMENT.fields())
        .from(SEGMENT)
        .where(SEGMENT.CHAIN_ID.in(parentIds))
        .orderBy(SEGMENT.OFFSET.desc())
        .limit(limitSegmentReadSize)
        .fetch());
    else
      return modelsFrom(Segment.class, dbProvider.getDSL().select(SEGMENT.fields())
        .from(SEGMENT)
        .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
        .where(SEGMENT.CHAIN_ID.in(parentIds))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(SEGMENT.OFFSET.desc())
        .limit(limitSegmentReadSize)
        .fetch());
  }

  @Override
  public Collection<Segment> readAllFromOffset(Access access, UUID chainId, Long fromOffset) throws HubException {
    return readAllFromToOffset(access, chainId, fromOffset, fromOffset + limitSegmentReadSize);
  }

  @Override
  public Collection<Segment> readAllFromToOffset(Access access, UUID chainId, Long fromOffset, Long toOffset) throws HubException {
    if (0 > toOffset)
      return Lists.newArrayList();

    if (access.isTopLevel())
      return modelsFrom(Segment.class, dbProvider.getDSL().select(SEGMENT.fields())
        .from(SEGMENT)
        .where(SEGMENT.CHAIN_ID.eq(chainId))
        .and(SEGMENT.OFFSET.greaterOrEqual(fromOffset))
        .and(SEGMENT.OFFSET.lessOrEqual(toOffset))
        .orderBy(SEGMENT.OFFSET.desc())
        .limit(limitSegmentReadSize)
        .fetch());
    else
      return modelsFrom(Segment.class, dbProvider.getDSL().select(SEGMENT.fields())
        .from(SEGMENT)
        .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
        .where(SEGMENT.CHAIN_ID.eq(chainId))
        .and(SEGMENT.OFFSET.greaterOrEqual(fromOffset))
        .and(SEGMENT.OFFSET.lessOrEqual(toOffset))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(SEGMENT.OFFSET.desc())
        .limit(limitSegmentReadSize)
        .fetch());
  }

  @Override
  public Collection<Segment> readAllInState(Access access, UUID chainId, SegmentState state) throws HubException {
    requireTopLevel(access);

    return modelsFrom(Segment.class, dbProvider.getDSL().select(SEGMENT.fields())
      .from(SEGMENT)
      .where(SEGMENT.CHAIN_ID.eq(chainId))
      .and(SEGMENT.STATE.eq(state.toString()))
      .orderBy(SEGMENT.OFFSET.desc())
      .limit(limitSegmentReadSize)
      .fetch());
  }

  @Override
  public Collection<Segment> readAllFromOffset(String chainEmbedKey, Long fromOffset) throws HubException {
// so "of offset zero" means of offset 0 to offset N
    Long maxOffset = fromOffset + limitSegmentReadSize;

    return modelsFrom(Segment.class, dbProvider.getDSL().select(SEGMENT.fields())
      .from(SEGMENT)
      .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
      .where(CHAIN.EMBED_KEY.eq(Chain.formatEmbedKey(chainEmbedKey)))
      .and(SEGMENT.OFFSET.greaterOrEqual(fromOffset))
      .and(SEGMENT.OFFSET.lessOrEqual(maxOffset))
      .orderBy(SEGMENT.OFFSET.desc())
      .limit(limitSegmentReadSize)
      .fetch());
  }

  @Override
  public Collection<Segment> readAllFromSecondsUTC(Access access, UUID chainId, Long fromSecondsUTC) throws HubException {
    Instant from = Instant.ofEpochSecond(fromSecondsUTC);
    Instant maxBeginAt = from.plusSeconds(playBufferAheadSeconds);
    Instant minEndAt = from.minusSeconds(playBufferDelaySeconds);

    if (access.isTopLevel())
      return modelsFrom(Segment.class, dbProvider.getDSL().select(SEGMENT.fields())
        .from(SEGMENT)
        .where(SEGMENT.CHAIN_ID.eq(chainId))
        .and(SEGMENT.BEGIN_AT.lessOrEqual(Timestamp.from(maxBeginAt)))
        .and(SEGMENT.END_AT.greaterOrEqual(Timestamp.from(minEndAt)))
        .orderBy(SEGMENT.OFFSET.asc())
        .limit(limitSegmentReadSize)
        .fetch());
    else
      return modelsFrom(Segment.class, dbProvider.getDSL().select(SEGMENT.fields())
        .from(SEGMENT)
        .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
        .where(SEGMENT.CHAIN_ID.eq(chainId))
        .and(SEGMENT.BEGIN_AT.lessOrEqual(Timestamp.from(maxBeginAt)))
        .and(SEGMENT.END_AT.greaterOrEqual(Timestamp.from(minEndAt)))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(SEGMENT.OFFSET.asc())
        .limit(limitSegmentReadSize)
        .fetch());
  }

  @Override
  public Collection<Segment> readAllFromSecondsUTC(String chainEmbedKey, Long fromSecondsUTC) throws HubException {
    Instant from = Instant.ofEpochSecond(fromSecondsUTC);
    Instant maxBeginAt = from.plusSeconds(playBufferAheadSeconds);
    Instant minEndAt = from.minusSeconds(playBufferDelaySeconds);

    return modelsFrom(Segment.class, dbProvider.getDSL().select(SEGMENT.fields())
      .from(SEGMENT)
      .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
      .where(CHAIN.EMBED_KEY.eq(Chain.formatEmbedKey(chainEmbedKey)))
      .and(SEGMENT.BEGIN_AT.lessOrEqual(Timestamp.from(maxBeginAt)))
      .and(SEGMENT.END_AT.greaterOrEqual(Timestamp.from(minEndAt)))
      .orderBy(SEGMENT.OFFSET.desc())
      .limit(limitSegmentReadSize)
      .fetch());
  }

  @Override
  public void update(Access access, UUID id, Segment entity) throws HubException, RestApiException, ValueException {
    requireTopLevel(access);
    DSLContext db = dbProvider.getDSL();

    // validate and cache to-state
    entity.validate();
    SegmentState toState = entity.getState();

    // fetch existing segment; further logic is based on its current state
    SegmentRecord segmentRecord = db.selectFrom(SEGMENT).where(SEGMENT.ID.eq(id)).fetchOne();
    requireExists("Segment #" + id, segmentRecord);

    // logic based on existing Segment State
    SegmentState.protectTransition(SegmentState.validate(segmentRecord.getState()), toState);

    // [#128] cannot change chainId of a segment
    Object updateChainId = entity.getChainId();
    if (Value.isNonNull(updateChainId) && !Objects.equals(updateChainId, segmentRecord.getChainId()))
      throw new HubException("cannot change chainId create a segment");

    // by only updating the chain from the expected state,
    // this prevents the state from being updated multiple times,
    // for example in the case of duplicate work
    SegmentRecord updatedRecord = db.newRecord(SEGMENT);
    updatedRecord.setId(id);
    setAll(updatedRecord, entity);
    int rowsAffected = db.update(SEGMENT)
      .set(updatedRecord)
      .where(SEGMENT.ID.eq(id))
      .and(SEGMENT.STATE.eq(segmentRecord.getState()))
      .execute();

    if (0 == rowsAffected)
      throw new HubException("No records updated.");
  }

  @Override
  public void updateState(Access access, UUID id, SegmentState state) throws HubException, RestApiException, ValueException {
    Segment segment = readOne(access, id);
    segment.setStateEnum(state);
    update(access, id, segment);
  }

  @Override
  public void revert(Access access, UUID id) throws HubException, RestApiException, ValueException {
    requireTopLevel(access);

    Segment segment = readOne(access, id);

    // Destroy child entities of segment-- but not the messages
    destroyChildEntities(dbProvider.getDSL(), access, id, false);

    update(access, id, segment);
  }

  @Override
  public void destroy(Access access, UUID id) throws HubException {
    requireTopLevel(access);
    DSLContext db = dbProvider.getDSL();

    // Segment
    SegmentRecord segment = db.selectFrom(SEGMENT)
      .where(SEGMENT.ID.eq(id))
      .fetchOne();
    requireExists("Segment #" + id, segment);

    // Only Delete segment waveform of S3 if non-null
    String waveformKey = segment.get(SEGMENT.WAVEFORM_KEY);
    if (Objects.nonNull(waveformKey)) {
      amazonProvider.deleteS3Object(
        segmentFileBucket,
        waveformKey);
    }

    // Destroy ALL child entities of segment
    destroyChildEntities(db, access, id, true);

    // Delete Segment
    db.deleteFrom(SEGMENT)
      .where(SEGMENT.ID.eq(id))
      .execute();
  }

  @Override
  public Segment newInstance() {
    return new Segment();
  }

  /**
   Destroy all child entities of segment

   @param db              DSL content
   @param access          control
   @param id              segment to destroy child entities of
   @param destroyMessages true if we also want to include the segment messages (false to preserve the messages)
   */
  private void destroyChildEntities(DSLContext db, Access access, UUID id, Boolean destroyMessages) throws HubException {
    requireTopLevel(access);

    db.deleteFrom(SEGMENT_CHOICE_ARRANGEMENT_PICK)
      .where(SEGMENT_CHOICE_ARRANGEMENT_PICK.SEGMENT_ID.eq(id))
      .execute();

    db.deleteFrom(SEGMENT_CHOICE_ARRANGEMENT)
      .where(SEGMENT_CHOICE_ARRANGEMENT.SEGMENT_ID.eq(id))
      .execute();

    db.deleteFrom(SEGMENT_CHOICE)
      .where(SEGMENT_CHOICE.SEGMENT_ID.eq(id))
      .execute();

    db.deleteFrom(SEGMENT_MEME)
      .where(SEGMENT_MEME.SEGMENT_ID.eq(id))
      .execute();

    db.deleteFrom(SEGMENT_CHORD)
      .where(SEGMENT_CHORD.SEGMENT_ID.eq(id))
      .execute();

    if (destroyMessages)
      db.deleteFrom(SEGMENT_MESSAGE)
        .where(SEGMENT_MESSAGE.SEGMENT_ID.eq(id))
        .execute();
  }

  /**
   Filter a collection of object to only one class

   @param objects     to filter source
   @param entityClass only allow these to pass through
   @param <N>         type of entities
   @return collection of only specified class of entities
   */
  private <N> Collection<N> filter(Collection<N> objects, Class<?> entityClass) {
    return objects.stream().filter(e -> e.getClass().equals(entityClass)).collect(Collectors.toList());
  }
}
