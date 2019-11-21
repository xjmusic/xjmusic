// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.xj.core.access.Access;
import io.xj.core.config.Config;
import io.xj.core.dao.DAORecord;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.entity.Entity;
import io.xj.core.exception.CoreException;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.model.Chain;
import io.xj.core.model.Segment;
import io.xj.core.model.SegmentChoice;
import io.xj.core.model.SegmentChoiceArrangement;
import io.xj.core.model.SegmentChoiceArrangementPick;
import io.xj.core.model.SegmentChord;
import io.xj.core.model.SegmentMeme;
import io.xj.core.model.SegmentMessage;
import io.xj.core.model.SegmentState;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.tables.records.SegmentRecord;
import org.jooq.DSLContext;
import org.jooq.TableRecord;

import java.sql.Connection;
import java.sql.SQLException;
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

  @Inject
  public SegmentDAOImpl(
    SQLDatabaseProvider dbProvider,
    AmazonProvider amazonProvider
  ) {
    this.amazonProvider = amazonProvider;
    this.dbProvider = dbProvider;
  }

  /**
   Update a record

   @param connection to SQL database
   @param access     control
   @param id         of record
   @throws CoreException if a Business Rule is violated
   */
  private void update(Connection connection, Access access, UUID id, Segment entity) throws CoreException {
    requireTopLevel(access);
    DSLContext db = DAORecord.DSL(connection);

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
    DAORecord.setAll(updatedRecord, entity);
    int rowsAffected = db.update(SEGMENT)
      .set(updatedRecord)
      .where(SEGMENT.ID.eq(id))
      .and(SEGMENT.STATE.eq(segmentRecord.getState()))
      .execute();

    if (0 == rowsAffected)
      throw new CoreException("No records updated.");

  }

  /**
   Read one record

   @param connection to SQL database
   @param access     control
   @param id         of record
   @return record
   */
  private Segment readOne(Connection connection, Access access, UUID id) throws CoreException {
    if (access.isTopLevel())
      return DAORecord.modelFrom(Segment.class, DAORecord.DSL(connection).selectFrom(SEGMENT)
        .where(SEGMENT.ID.eq(id))
        .fetchOne());
    else
      return DAORecord.modelFrom(Segment.class, DAORecord.DSL(connection).select(SEGMENT.fields())
        .from(SEGMENT)
        .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
        .where(SEGMENT.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne());
  }

  /**
   Read all records in parent by id, beginning and ending at particular offsets

   @param connection to SQL database
   @param access     control
   @param chainId    of parent
   @param fromOffset to read segments
   @param toOffset   to read segments
   @return array of records
   */
  private Collection<Segment> readAllFromToOffset(Connection connection, Access access, UUID chainId, Long fromOffset, Long toOffset) throws CoreException {
    if (access.isTopLevel())
      return DAORecord.modelsFrom(Segment.class, DAORecord.DSL(connection).select(SEGMENT.fields())
        .from(SEGMENT)
        .where(SEGMENT.CHAIN_ID.eq(chainId))
        .and(SEGMENT.OFFSET.greaterOrEqual(fromOffset))
        .and(SEGMENT.OFFSET.lessOrEqual(toOffset))
        .orderBy(SEGMENT.OFFSET.desc())
        .limit(Config.getLimitSegmentReadSize())
        .fetch());
    else
      return DAORecord.modelsFrom(Segment.class, DAORecord.DSL(connection).select(SEGMENT.fields())
        .from(SEGMENT)
        .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
        .where(SEGMENT.CHAIN_ID.eq(chainId))
        .and(SEGMENT.OFFSET.greaterOrEqual(fromOffset))
        .and(SEGMENT.OFFSET.lessOrEqual(toOffset))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(SEGMENT.OFFSET.desc())
        .limit(Config.getLimitSegmentReadSize())
        .fetch());
  }

  @Override
  public Segment create(Access access, Segment entity) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      // top-level access
      requireTopLevel(access);

      entity.validate();

      // [#126] Segments are always readMany in PLANNED state
      entity.setStateEnum(SegmentState.Planned);

      // Chain ID and offset are read-only, set at creation
      requireNotExists("Segment at same offset in Chain", DAORecord.DSL(connection).selectCount().from(SEGMENT)
        .where(SEGMENT.CHAIN_ID.eq(entity.getChainId()))
        .and(SEGMENT.OFFSET.eq(entity.getOffset()))
        .fetchOne(0, int.class));

      return DAORecord.modelFrom(Segment.class, executeCreate(connection, SEGMENT, entity));
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public Segment readOne(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      return readOne(connection, access, id);
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public Segment readOneAtChainOffset(Access access, UUID chainId, Long offset) throws CoreException {
    requireTopLevel(access);
    try (Connection connection = dbProvider.getConnection()) {
      return DAORecord.modelFrom(Segment.class, DAORecord.DSL(connection).selectFrom(SEGMENT)
        .where(SEGMENT.OFFSET.eq(offset))
        .and(SEGMENT.CHAIN_ID.eq(chainId))
        .fetchOne());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public Segment readOneInState(Access access, UUID chainId, SegmentState segmentState, Instant segmentBeginBefore) throws CoreException {
    requireTopLevel(access);
    try (Connection connection = dbProvider.getConnection()) {
      return DAORecord.modelFrom(Segment.class, DAORecord.DSL(connection).select(SEGMENT.fields()).from(SEGMENT)
        .where(SEGMENT.CHAIN_ID.eq(chainId))
        .and(SEGMENT.STATE.eq(segmentState.toString()))
        .and(SEGMENT.BEGIN_AT.lessOrEqual(Timestamp.from(segmentBeginBefore)))
        .orderBy(SEGMENT.OFFSET.asc())
        .limit(1)
        .fetchOne());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public Collection<Segment> readAll(String chainEmbedKey) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      return DAORecord.modelsFrom(Segment.class, DAORecord.DSL(connection).select(SEGMENT.fields())
        .from(SEGMENT)
        .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
        .where(CHAIN.EMBED_KEY.eq(Chain.formatEmbedKey(chainEmbedKey)))
        .orderBy(SEGMENT.OFFSET.desc())
        .limit(Config.getLimitSegmentReadSize())
        .fetch());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public <N extends Entity> Collection<N> readAllSubEntities(Access access, Collection<UUID> segmentIds) throws CoreException {
    requireTopLevel(access);
    try (Connection connection = dbProvider.getConnection()) {
      DSLContext db = DAORecord.DSL(connection);
      Collection<N> entities = Lists.newArrayList();

      DAORecord.modelsFrom(SegmentChoiceArrangementPick.class, db.select(SEGMENT_CHOICE_ARRANGEMENT_PICK.fields())
        .from(SEGMENT_CHOICE_ARRANGEMENT_PICK)
        .where(SEGMENT_CHOICE_ARRANGEMENT_PICK.SEGMENT_ID.in(segmentIds))
        .fetch()).forEach(e -> entities.add((N) e));

      DAORecord.modelsFrom(SegmentChoiceArrangement.class, db.select(SEGMENT_CHOICE_ARRANGEMENT.fields())
        .from(SEGMENT_CHOICE_ARRANGEMENT)
        .where(SEGMENT_CHOICE_ARRANGEMENT.SEGMENT_ID.in(segmentIds))
        .fetch()).forEach(e -> entities.add((N) e));

      DAORecord.modelsFrom(SegmentChoice.class, db.select(SEGMENT_CHOICE.fields())
        .from(SEGMENT_CHOICE)
        .where(SEGMENT_CHOICE.SEGMENT_ID.in(segmentIds))
        .fetch()).forEach(e -> entities.add((N) e));

      DAORecord.modelsFrom(SegmentMeme.class, db.select(SEGMENT_MEME.fields())
        .from(SEGMENT_MEME)
        .where(SEGMENT_MEME.SEGMENT_ID.in(segmentIds))
        .fetch()).forEach(e -> entities.add((N) e));

      DAORecord.modelsFrom(SegmentChord.class, db.select(SEGMENT_CHORD.fields())
        .from(SEGMENT_CHORD)
        .where(SEGMENT_CHORD.SEGMENT_ID.in(segmentIds))
        .fetch()).forEach(e -> entities.add((N) e));

      return entities;

    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }

  }

  @Override
  public <N extends Entity> void createAllSubEntities(Access access, Collection<N> entities) throws CoreException {
    requireTopLevel(access);
    try (Connection connection = dbProvider.getConnection()) {
      DSLContext db = DAORecord.DSL(connection);
      Collection<? extends TableRecord<?>> records = Lists.newArrayList();
      records.addAll(DAORecord.recordsFrom(db, SEGMENT_MEME, Entity.filter(entities, SegmentMeme.class)));
      records.addAll(DAORecord.recordsFrom(db, SEGMENT_CHORD, Entity.filter(entities, SegmentChord.class)));
      records.addAll(DAORecord.recordsFrom(db, SEGMENT_MESSAGE, Entity.filter(entities, SegmentMessage.class)));
      records.addAll(DAORecord.recordsFrom(db, SEGMENT_CHOICE, Entity.filter(entities, SegmentChoice.class))); // before arrangement
      records.addAll(DAORecord.recordsFrom(db, SEGMENT_CHOICE_ARRANGEMENT, Entity.filter(entities, SegmentChoiceArrangement.class))); // before pick
      records.addAll(DAORecord.recordsFrom(db, SEGMENT_CHOICE_ARRANGEMENT_PICK, Entity.filter(entities, SegmentChoiceArrangementPick.class)));
      int[] rows = db.batchInsert(records).execute();
      if (!Objects.equals(rows.length, entities.size()))
        throw new CoreException(String.format("Only inserted %d of %d intended rows", rows.length, entities.size()));

    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public Collection<Segment> readMany(Access access, Collection<UUID> parentIds) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      if (access.isTopLevel())
        return DAORecord.modelsFrom(Segment.class, DAORecord.DSL(connection).select(SEGMENT.fields())
          .from(SEGMENT)
          .where(SEGMENT.CHAIN_ID.in(parentIds))
          .orderBy(SEGMENT.OFFSET.desc())
          .limit(Config.getLimitSegmentReadSize())
          .fetch());
      else
        return DAORecord.modelsFrom(Segment.class, DAORecord.DSL(connection).select(SEGMENT.fields())
          .from(SEGMENT)
          .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
          .where(SEGMENT.CHAIN_ID.in(parentIds))
          .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
          .orderBy(SEGMENT.OFFSET.desc())
          .limit(Config.getLimitSegmentReadSize())
          .fetch());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public Collection<Segment> readAllFromOffset(Access access, UUID chainId, Long fromOffset) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      return readAllFromToOffset(connection, access, chainId, fromOffset, fromOffset + Config.getLimitSegmentReadSize());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public Collection<Segment> readAllFromToOffset(Access access, UUID chainId, Long fromOffset, Long toOffset) throws CoreException {
    if (0 > toOffset)
      return Lists.newArrayList();

    try (Connection connection = dbProvider.getConnection()) {
      return readAllFromToOffset(connection, access, chainId, fromOffset, toOffset);
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public Collection<Segment> readAllInState(Access access, UUID chainId, SegmentState state) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireTopLevel(access);

      return DAORecord.modelsFrom(Segment.class, DAORecord.DSL(connection).select(SEGMENT.fields())
        .from(SEGMENT)
        .where(SEGMENT.CHAIN_ID.eq(chainId))
        .and(SEGMENT.STATE.eq(state.toString()))
        .orderBy(SEGMENT.OFFSET.desc())
        .limit(Config.getLimitSegmentReadSize())
        .fetch());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public Collection<Segment> readAllFromOffset(String chainEmbedKey, Long fromOffset) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      // so "of offset zero" means of offset 0 to offset N
      Long maxOffset = fromOffset + Config.getLimitSegmentReadSize();

      return DAORecord.modelsFrom(Segment.class, DAORecord.DSL(connection).select(SEGMENT.fields())
        .from(SEGMENT)
        .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
        .where(CHAIN.EMBED_KEY.eq(Chain.formatEmbedKey(chainEmbedKey)))
        .and(SEGMENT.OFFSET.greaterOrEqual(fromOffset))
        .and(SEGMENT.OFFSET.lessOrEqual(maxOffset))
        .orderBy(SEGMENT.OFFSET.desc())
        .limit(Config.getLimitSegmentReadSize())
        .fetch());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public Collection<Segment> readAllFromSecondsUTC(Access access, UUID chainId, Long fromSecondsUTC) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      Instant from = Instant.ofEpochSecond(fromSecondsUTC);
      Instant maxBeginAt = from.plusSeconds(Config.getPlayBufferAheadSeconds());
      Instant minEndAt = from.minusSeconds(Config.getPlayBufferDelaySeconds());

      if (access.isTopLevel())
        return DAORecord.modelsFrom(Segment.class, DAORecord.DSL(connection).select(SEGMENT.fields())
          .from(SEGMENT)
          .where(SEGMENT.CHAIN_ID.eq(chainId))
          .and(SEGMENT.BEGIN_AT.lessOrEqual(Timestamp.from(maxBeginAt)))
          .and(SEGMENT.END_AT.greaterOrEqual(Timestamp.from(minEndAt)))
          .orderBy(SEGMENT.OFFSET.desc())
          .limit(Config.getLimitSegmentReadSize())
          .fetch());
      else
        return DAORecord.modelsFrom(Segment.class, DAORecord.DSL(connection).select(SEGMENT.fields())
          .from(SEGMENT)
          .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
          .where(SEGMENT.CHAIN_ID.eq(chainId))
          .and(SEGMENT.BEGIN_AT.lessOrEqual(Timestamp.from(maxBeginAt)))
          .and(SEGMENT.END_AT.greaterOrEqual(Timestamp.from(minEndAt)))
          .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
          .orderBy(SEGMENT.OFFSET.desc())
          .limit(Config.getLimitSegmentReadSize())
          .fetch());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public Collection<Segment> readAllFromSecondsUTC(String chainEmbedKey, Long fromSecondsUTC) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      Instant from = Instant.ofEpochSecond(fromSecondsUTC);
      Instant maxBeginAt = from.plusSeconds(Config.getPlayBufferAheadSeconds());
      Instant minEndAt = from.minusSeconds(Config.getPlayBufferDelaySeconds());

      return DAORecord.modelsFrom(Segment.class, DAORecord.DSL(connection).select(SEGMENT.fields())
        .from(SEGMENT)
        .join(CHAIN).on(CHAIN.ID.eq(SEGMENT.CHAIN_ID))
        .where(CHAIN.EMBED_KEY.eq(Chain.formatEmbedKey(chainEmbedKey)))
        .and(SEGMENT.BEGIN_AT.lessOrEqual(Timestamp.from(maxBeginAt)))
        .and(SEGMENT.END_AT.greaterOrEqual(Timestamp.from(minEndAt)))
        .orderBy(SEGMENT.OFFSET.desc())
        .limit(Config.getLimitSegmentReadSize())
        .fetch());
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void update(Access access, UUID id, Segment entity) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      update(connection, access, id, entity);
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void updateState(Access access, UUID id, SegmentState state) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      Segment segment = readOne(connection, access, id);
      segment.setStateEnum(state);
      update(connection, access, id, segment);
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void revert(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireTopLevel(access);

      Segment segment = readOne(connection, access, id);
      // TODO delete all child entities of segment

      update(connection, access, id, segment);
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public void destroy(Access access, UUID id) throws CoreException {
    try (Connection connection = dbProvider.getConnection()) {
      requireTopLevel(access);
      DSLContext db = DAORecord.DSL(connection);

      // Segment
      SegmentRecord segment = db.selectFrom(SEGMENT)
        .where(SEGMENT.ID.eq(id))
        .fetchOne();
      requireExists("Segment #" + id, segment);

      // Only Delete segment waveform of S3 if non-null
      String waveformKey = segment.get(SEGMENT.WAVEFORM_KEY);
      if (Objects.nonNull(waveformKey)) {
        amazonProvider.deleteS3Object(
          Config.getSegmentFileBucket(),
          waveformKey);
      }

      // Delete Segment
      db.deleteFrom(SEGMENT)
        .where(SEGMENT.ID.eq(id))
        .execute();
    } catch (SQLException e) {
      throw new CoreException("SQL Exception", e);
    }
  }

  @Override
  public Segment newInstance() {
    return new Segment();
  }

}
