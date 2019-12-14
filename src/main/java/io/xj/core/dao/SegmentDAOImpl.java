// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.core.access.Access;
import io.xj.core.entity.Entity;
import io.xj.core.exception.CoreException;
import io.xj.core.external.AmazonProvider;
import io.xj.core.model.Chain;
import io.xj.core.model.Segment;
import io.xj.core.model.SegmentChoice;
import io.xj.core.model.SegmentChoiceArrangement;
import io.xj.core.model.SegmentChoiceArrangementPick;
import io.xj.core.model.SegmentChord;
import io.xj.core.model.SegmentMeme;
import io.xj.core.model.SegmentMessage;
import io.xj.core.model.SegmentState;
import io.xj.core.persistence.SQLDatabaseProvider;
import io.xj.core.tables.records.SegmentRecord;
import org.jooq.DSLContext;
import org.jooq.TableRecord;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import static io.xj.core.Tables.CHAIN;
import static io.xj.core.Tables.SEGMENT;
import static io.xj.core.Tables.SEGMENT_CHOICE;
import static io.xj.core.Tables.SEGMENT_CHOICE_ARRANGEMENT;
import static io.xj.core.Tables.SEGMENT_CHOICE_ARRANGEMENT_PICK;
import static io.xj.core.Tables.SEGMENT_CHORD;
import static io.xj.core.Tables.SEGMENT_MEME;
import static io.xj.core.Tables.SEGMENT_MESSAGE;

public class SegmentDAOImpl extends DAOImpl<Segment> implements SegmentDAO {
  private final AmazonProvider amazonProvider;
  private final int playBufferAheadSeconds;
  private final int playBufferDelaySeconds;
  private final String segmentFileBucket;
  private int limitSegmentReadSize;

  @Inject
  public SegmentDAOImpl(
    SQLDatabaseProvider dbProvider,
    AmazonProvider amazonProvider,
    Config config
  ) {
    this.amazonProvider = amazonProvider;
    this.dbProvider = dbProvider;

    playBufferAheadSeconds = config.getInt("play.bufferAheadSeconds");
    playBufferDelaySeconds = config.getInt("play.bufferDelaySeconds");
    limitSegmentReadSize = config.getInt("segment.limitReadSize");
    segmentFileBucket = config.getString("segment.fileBucket");
  }


  @Override
  public Segment create(Access access, Segment entity) throws CoreException {
// top-level access
    requireTopLevel(access);

    entity.validate();

    // [#126] Segments are always readMany in PLANNED state
    entity.setStateEnum(SegmentState.Planned);

    // Chain ID and offset are read-only, set at creation
    requireNotExists("Segment at same offset in Chain", dbProvider.getDSL().selectCount().from(SEGMENT)
      .where(SEGMENT.CHAIN_ID.eq(entity.getChainId()))
      .and(SEGMENT.OFFSET.eq(entity.getOffset()))
      .fetchOne(0, int.class));

    return DAO.modelFrom(Segment.class, executeCreate(dbProvider.getDSL(), SEGMENT, entity));
  }

  @Override
  public Segment readOne(Access access, UUID id) throws CoreException {
    if (access.isTopLevel())
      return DAO.modelFrom(Segment.class, dbProvider.getDSL().selectFrom(SEGMENT)
        .where(SEGMENT.ID.eq(id))
        .fetchOne());
    else
      return DAO.modelFrom(Segment.class, dbProvider.getDSL().select(SEGMENT.fields())
        .from(SEGMENT)
        .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
        .where(SEGMENT.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne());
  }

  @Override
  public Segment readOneAtChainOffset(Access access, UUID chainId, Long offset) throws CoreException {
    requireTopLevel(access);
    return DAO.modelFrom(Segment.class, dbProvider.getDSL().selectFrom(SEGMENT)
      .where(SEGMENT.OFFSET.eq(offset))
      .and(SEGMENT.CHAIN_ID.eq(chainId))
      .fetchOne());
  }

  @Override
  public Segment readOneInState(Access access, UUID chainId, SegmentState segmentState, Instant segmentBeginBefore) throws CoreException {
    requireTopLevel(access);
    return DAO.modelFrom(Segment.class, dbProvider.getDSL().select(SEGMENT.fields()).from(SEGMENT)
      .where(SEGMENT.CHAIN_ID.eq(chainId))
      .and(SEGMENT.STATE.eq(segmentState.toString()))
      .and(SEGMENT.BEGIN_AT.lessOrEqual(Timestamp.from(segmentBeginBefore)))
      .orderBy(SEGMENT.OFFSET.asc())
      .limit(1)
      .fetchOne());
  }

  @Override
  public Collection<Segment> readAll(String chainEmbedKey) throws CoreException {
    return DAO.modelsFrom(Segment.class, dbProvider.getDSL().select(SEGMENT.fields())
      .from(SEGMENT)
      .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
      .where(CHAIN.EMBED_KEY.eq(Chain.formatEmbedKey(chainEmbedKey)))
      .orderBy(SEGMENT.OFFSET.desc())
      .limit(limitSegmentReadSize)
      .fetch());
  }

  @Override
  public <N extends Entity> Collection<N> readAllSubEntities(Access access, Collection<UUID> segmentIds, boolean includePicks) throws CoreException {
    requireTopLevel(access);
    DSLContext db = dbProvider.getDSL();
    Collection<N> entities = Lists.newArrayList();

    if (includePicks)
      DAO.modelsFrom(SegmentChoiceArrangementPick.class, db.select(SEGMENT_CHOICE_ARRANGEMENT_PICK.fields())
        .from(SEGMENT_CHOICE_ARRANGEMENT_PICK)
        .where(SEGMENT_CHOICE_ARRANGEMENT_PICK.SEGMENT_ID.in(segmentIds))
        .fetch()).forEach(e -> entities.add((N) e));

    DAO.modelsFrom(SegmentChoiceArrangement.class, db.select(SEGMENT_CHOICE_ARRANGEMENT.fields())
      .from(SEGMENT_CHOICE_ARRANGEMENT)
      .where(SEGMENT_CHOICE_ARRANGEMENT.SEGMENT_ID.in(segmentIds))
      .fetch()).forEach(e -> entities.add((N) e));

    DAO.modelsFrom(SegmentChoice.class, db.select(SEGMENT_CHOICE.fields())
      .from(SEGMENT_CHOICE)
      .where(SEGMENT_CHOICE.SEGMENT_ID.in(segmentIds))
      .fetch()).forEach(e -> entities.add((N) e));

    DAO.modelsFrom(SegmentMeme.class, db.select(SEGMENT_MEME.fields())
      .from(SEGMENT_MEME)
      .where(SEGMENT_MEME.SEGMENT_ID.in(segmentIds))
      .fetch()).forEach(e -> entities.add((N) e));

    DAO.modelsFrom(SegmentChord.class, db.select(SEGMENT_CHORD.fields())
      .from(SEGMENT_CHORD)
      .where(SEGMENT_CHORD.SEGMENT_ID.in(segmentIds))
      .fetch()).forEach(e -> entities.add((N) e));

    return entities;

  }

  @Override
  public <N extends Entity> void createAllSubEntities(Access access, Collection<N> entities) throws CoreException {
    requireTopLevel(access);
    DSLContext db = dbProvider.getDSL();
    Collection<? extends TableRecord<?>> records = Lists.newArrayList();
    records.addAll(DAO.recordsFrom(db, SEGMENT_MEME, Entity.filter(entities, SegmentMeme.class)));
    records.addAll(DAO.recordsFrom(db, SEGMENT_CHORD, Entity.filter(entities, SegmentChord.class)));
    records.addAll(DAO.recordsFrom(db, SEGMENT_MESSAGE, Entity.filter(entities, SegmentMessage.class)));
    records.addAll(DAO.recordsFrom(db, SEGMENT_CHOICE, Entity.filter(entities, SegmentChoice.class))); // before arrangement
    records.addAll(DAO.recordsFrom(db, SEGMENT_CHOICE_ARRANGEMENT, Entity.filter(entities, SegmentChoiceArrangement.class))); // before pick
    records.addAll(DAO.recordsFrom(db, SEGMENT_CHOICE_ARRANGEMENT_PICK, Entity.filter(entities, SegmentChoiceArrangementPick.class)));
    int[] rows = db.batchInsert(records).execute();
    if (!Objects.equals(rows.length, entities.size()))
      throw new CoreException(String.format("Only inserted %d of %d intended rows", rows.length, entities.size()));

  }

  @Override
  public Collection<Segment> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    if (access.isTopLevel())
      return DAO.modelsFrom(Segment.class, dbProvider.getDSL().select(SEGMENT.fields())
        .from(SEGMENT)
        .where(SEGMENT.CHAIN_ID.in(parentIds))
        .orderBy(SEGMENT.OFFSET.desc())
        .limit(limitSegmentReadSize)
        .fetch());
    else
      return DAO.modelsFrom(Segment.class, dbProvider.getDSL().select(SEGMENT.fields())
        .from(SEGMENT)
        .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
        .where(SEGMENT.CHAIN_ID.in(parentIds))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(SEGMENT.OFFSET.desc())
        .limit(limitSegmentReadSize)
        .fetch());
  }

  @Override
  public Collection<Segment> readAllFromOffset(Access access, UUID chainId, Long fromOffset) throws CoreException {
    return readAllFromToOffset(access, chainId, fromOffset, fromOffset + limitSegmentReadSize);
  }

  @Override
  public Collection<Segment> readAllFromToOffset(Access access, UUID chainId, Long fromOffset, Long toOffset) throws CoreException {
    if (0 > toOffset)
      return Lists.newArrayList();

    if (access.isTopLevel())
      return DAO.modelsFrom(Segment.class, dbProvider.getDSL().select(SEGMENT.fields())
        .from(SEGMENT)
        .where(SEGMENT.CHAIN_ID.eq(chainId))
        .and(SEGMENT.OFFSET.greaterOrEqual(fromOffset))
        .and(SEGMENT.OFFSET.lessOrEqual(toOffset))
        .orderBy(SEGMENT.OFFSET.desc())
        .limit(limitSegmentReadSize)
        .fetch());
    else
      return DAO.modelsFrom(Segment.class, dbProvider.getDSL().select(SEGMENT.fields())
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
  public Collection<Segment> readAllInState(Access access, UUID chainId, SegmentState state) throws CoreException {
    requireTopLevel(access);

    return DAO.modelsFrom(Segment.class, dbProvider.getDSL().select(SEGMENT.fields())
      .from(SEGMENT)
      .where(SEGMENT.CHAIN_ID.eq(chainId))
      .and(SEGMENT.STATE.eq(state.toString()))
      .orderBy(SEGMENT.OFFSET.desc())
      .limit(limitSegmentReadSize)
      .fetch());
  }

  @Override
  public Collection<Segment> readAllFromOffset(String chainEmbedKey, Long fromOffset) throws CoreException {
// so "of offset zero" means of offset 0 to offset N
    Long maxOffset = fromOffset + limitSegmentReadSize;

    return DAO.modelsFrom(Segment.class, dbProvider.getDSL().select(SEGMENT.fields())
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
  public Collection<Segment> readAllFromSecondsUTC(Access access, UUID chainId, Long fromSecondsUTC) throws CoreException {
    Instant from = Instant.ofEpochSecond(fromSecondsUTC);
    Instant maxBeginAt = from.plusSeconds(playBufferAheadSeconds);
    Instant minEndAt = from.minusSeconds(playBufferDelaySeconds);

    if (access.isTopLevel())
      return DAO.modelsFrom(Segment.class, dbProvider.getDSL().select(SEGMENT.fields())
        .from(SEGMENT)
        .where(SEGMENT.CHAIN_ID.eq(chainId))
        .and(SEGMENT.BEGIN_AT.lessOrEqual(Timestamp.from(maxBeginAt)))
        .and(SEGMENT.END_AT.greaterOrEqual(Timestamp.from(minEndAt)))
        .orderBy(SEGMENT.OFFSET.desc())
        .limit(limitSegmentReadSize)
        .fetch());
    else
      return DAO.modelsFrom(Segment.class, dbProvider.getDSL().select(SEGMENT.fields())
        .from(SEGMENT)
        .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
        .where(SEGMENT.CHAIN_ID.eq(chainId))
        .and(SEGMENT.BEGIN_AT.lessOrEqual(Timestamp.from(maxBeginAt)))
        .and(SEGMENT.END_AT.greaterOrEqual(Timestamp.from(minEndAt)))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(SEGMENT.OFFSET.desc())
        .limit(limitSegmentReadSize)
        .fetch());
  }

  @Override
  public Collection<Segment> readAllFromSecondsUTC(String chainEmbedKey, Long fromSecondsUTC) throws CoreException {
    Instant from = Instant.ofEpochSecond(fromSecondsUTC);
    Instant maxBeginAt = from.plusSeconds(playBufferAheadSeconds);
    Instant minEndAt = from.minusSeconds(playBufferDelaySeconds);

    return DAO.modelsFrom(Segment.class, dbProvider.getDSL().select(SEGMENT.fields())
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
  public void update(Access access, UUID id, Segment entity) throws CoreException {
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
    if (isNonNull(updateChainId) && !Objects.equals(updateChainId, segmentRecord.getChainId()))
      throw new CoreException("cannot change chainId create a segment");

    // by only updating the chain from the expected state,
    // this prevents the state from being updated multiple times,
    // for example in the case of duplicate work
    SegmentRecord updatedRecord = db.newRecord(SEGMENT);
    updatedRecord.setId(id);
    DAO.setAll(updatedRecord, entity);
    int rowsAffected = db.update(SEGMENT)
      .set(updatedRecord)
      .where(SEGMENT.ID.eq(id))
      .and(SEGMENT.STATE.eq(segmentRecord.getState()))
      .execute();

    if (0 == rowsAffected)
      throw new CoreException("No records updated.");
  }

  @Override
  public void updateState(Access access, UUID id, SegmentState state) throws CoreException {
    Segment segment = readOne(access, id);
    segment.setStateEnum(state);
    update(access, id, segment);
  }

  @Override
  public void revert(Access access, UUID id) throws CoreException {
    requireTopLevel(access);

    Segment segment = readOne(access, id);

    // Destroy child entities of segment
    destroyChildEntities(dbProvider.getDSL(), access, id);

    update(access, id, segment);
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
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

    // Destroy child entities of segment
    destroyChildEntities(db, access, id);

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

   @param db     DSL content
   @param access control
   @param id     segment to destroy child entities of
   */
  private void destroyChildEntities(DSLContext db, Access access, UUID id) throws CoreException {
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

    db.deleteFrom(SEGMENT_MESSAGE)
      .where(SEGMENT_MESSAGE.SEGMENT_ID.eq(id))
      .execute();
  }
}
