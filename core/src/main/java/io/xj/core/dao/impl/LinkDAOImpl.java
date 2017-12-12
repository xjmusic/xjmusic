// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.Tables;
import io.xj.core.access.impl.Access;
import io.xj.core.config.Config;
import io.xj.core.config.Exposure;
import io.xj.core.dao.LinkDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.CancelException;
import io.xj.core.exception.ConfigException;
import io.xj.core.exception.DatabaseException;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.model.link.Link;
import io.xj.core.model.link.LinkState;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;
import io.xj.core.tables.records.LinkRecord;
import io.xj.core.transport.CSV;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.UpdateSetFirstStep;
import org.jooq.types.ULong;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.ARRANGEMENT;
import static io.xj.core.Tables.LINK_MEME;
import static io.xj.core.Tables.LINK_MESSAGE;
import static io.xj.core.Tables.PICK;
import static io.xj.core.tables.Chain.CHAIN;
import static io.xj.core.tables.Choice.CHOICE;
import static io.xj.core.tables.Link.LINK;
import static io.xj.core.tables.LinkChord.LINK_CHORD;

public class LinkDAOImpl extends DAOImpl implements LinkDAO {

  private static final long MILLIS_PER_SECOND = 1000;
  private final AmazonProvider amazonProvider;

  @Inject
  public LinkDAOImpl(
    SQLDatabaseProvider dbProvider,
    AmazonProvider amazonProvider
  ) {
    this.amazonProvider = amazonProvider;
    this.dbProvider = dbProvider;
  }

  @Override
  public LinkRecord create(Access access, Link entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public LinkRecord readOne(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Link readOneAtChainOffset(Access access, ULong chainId, ULong offset) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneAtChainOffset(tx.getContext(), access, chainId, offset));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Nullable
  @Override
  public LinkRecord readOneInState(Access access, ULong chainId, LinkState linkState, Timestamp linkBeginBefore) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneInState(tx.getContext(), access, chainId, linkState, linkBeginBefore));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Result<LinkRecord> readAll(Access access, ULong chainId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, chainId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Result<LinkRecord> readAllFromOffset(Access access, ULong chainId, ULong fromOffset) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllFromOffset(tx.getContext(), access, chainId, fromOffset));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Result<LinkRecord> readAllFromSecondsUTC(Access access, ULong chainId, ULong fromSecondsUTC) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllFromSecondsUTC(tx.getContext(), access, chainId, fromSecondsUTC));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Result<LinkRecord> readAllInState(Access access, LinkState state) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInState(tx.getContext(), access, state));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, ULong id, Link entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, id, entity);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void updateState(Access access, ULong id, LinkState state) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      updateState(tx.getContext(), access, id, state);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void destroy(Access access, ULong linkId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      destroy(tx.getContext(), access, linkId);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }


  /**
   * Create a new record
   *
   * @param db     context
   * @param access control
   * @param entity for new record
   * @return newly readMany record
   * @throws BusinessException if a Business Rule is violated
   */
  private LinkRecord create(DSLContext db, Access access, Link entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    // [#126] Links are always readMany in PLANNED state
    fieldValues.put(LINK.STATE, LinkState.Planned);

    // [#267] Link has `waveform_key` referencing xj-link-* S3 bucket object key
    fieldValues.put(LINK.WAVEFORM_KEY, generateKey(entity.getChainId()));

    // top-level access
    requireTopLevel(access);

    return executeCreate(db, LINK, fieldValues);
  }

  /**
   * General a Link URL
   *
   * @param chainId to generate URL for
   * @return URL as string
   */
  private String generateKey(ULong chainId) {
    return amazonProvider.generateKey(
      Exposure.FILE_CHAIN + Exposure.FILE_SEPARATOR +
        chainId + Exposure.FILE_SEPARATOR +
        Exposure.FILE_LINK, Link.FILE_EXTENSION);
  }

  /**
   * Read one record
   *
   * @param db     context
   * @param access control
   * @param id     of record
   * @return record
   */
  private LinkRecord readOne(DSLContext db, Access access, ULong id) {
    if (access.isTopLevel())
      return db.selectFrom(LINK)
        .where(LINK.ID.eq(id))
        .fetchOne();
    else
      return recordInto(LINK, db.select(LINK.fields())
        .from(LINK)
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(LINK.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
  }

  /**
   * Read id for the Link in a Chain at a given offset, if present
   *
   * @param db      context
   * @param access  control
   * @param chainId to fetch a link for
   * @param offset  to fetch link at
   * @return record
   */
  @Nullable
  private Link readOneAtChainOffset(DSLContext db, Access access, ULong chainId, ULong offset) throws BusinessException {
    requireTopLevel(access);
    LinkRecord record = db.selectFrom(LINK)
      .where(LINK.OFFSET.eq(offset))
      .and(LINK.CHAIN_ID.eq(chainId))
      .fetchOne();

    if (Objects.isNull(record)) {
      return null;
    } else {
      return new Link().setFromRecord(record);
    }
  }

  /**
   * Fetch one Link by chainId and state, if present
   *
   * @param db              context
   * @param access          control
   * @param chainId         to find link in
   * @param linkState       linkState to find link in
   * @param linkBeginBefore ahead to look for links
   * @return Link if found
   * @throws BusinessException on failure
   */
  private LinkRecord readOneInState(DSLContext db, Access access, ULong chainId, LinkState linkState, Timestamp linkBeginBefore) throws BusinessException {
    requireTopLevel(access);

    return recordInto(LINK, db.select(LINK.fields()).from(LINK)
      .where(LINK.CHAIN_ID.eq(chainId))
      .and(LINK.STATE.eq(linkState.toString()))
      .and(LINK.BEGIN_AT.lessOrEqual(linkBeginBefore))
      .orderBy(LINK.OFFSET.asc())
      .limit(1)
      .fetchOne());
  }

  /**
   * Read all records in parent by id
   * <p>
   * [#235] Chain Links can only be read N at a time, to avoid hanging the server or client
   *
   * @param db      context
   * @param access  control
   * @param chainId of parent
   * @return array of records
   */
  private Result<LinkRecord> readAll(DSLContext db, Access access, ULong chainId) {
    if (access.isTopLevel())
      return resultInto(LINK, db.select(LINK.fields())
        .from(LINK)
        .where(LINK.CHAIN_ID.eq(chainId))
        .orderBy(LINK.OFFSET.desc())
        .limit(Config.limitLinkReadSize())
        .fetch());
    else
      return resultInto(LINK, db.select(LINK.fields())
        .from(LINK)
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(LINK.CHAIN_ID.eq(chainId))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .orderBy(LINK.OFFSET.desc())
        .limit(Config.limitLinkReadSize())
        .fetch());
  }

  /**
   * Read all records in parent by id, beginning at a particular offset
   * <p>
   * [#235] Chain Links can only be read N at a time, to avoid hanging the server or client
   *
   * @param db      context
   * @param access  control
   * @param chainId of parent
   * @return array of records
   */
  private Result<LinkRecord> readAllFromOffset(DSLContext db, Access access, ULong chainId, ULong fromOffset) {
    // so "from offset zero" means from offset 0 to offset N
    ULong maxOffset = ULong.valueOf(
      fromOffset.toBigInteger().add(
        BigInteger.valueOf(Config.limitLinkReadSize())));

    if (access.isTopLevel())
      return resultInto(LINK, db.select(LINK.fields())
        .from(LINK)
        .where(LINK.CHAIN_ID.eq(chainId))
        .and(LINK.OFFSET.lessOrEqual(maxOffset))
        .orderBy(LINK.OFFSET.desc())
        .limit(Config.limitLinkReadSize())
        .fetch());
    else
      return resultInto(LINK, db.select(LINK.fields())
        .from(LINK)
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(LINK.CHAIN_ID.eq(chainId))
        .and(LINK.OFFSET.lessOrEqual(maxOffset))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .orderBy(LINK.OFFSET.desc())
        .limit(Config.limitLinkReadSize())
        .fetch());
  }

  /**
   * Read all Links that are accessible, starting at a particular time in seconds UTC since epoch.
   * limit buffer ahead seconds readable at once in environment configuration
   * <p>
   * [#278] Chain Player lives in navbar, and handles all playback (audio waveform, link waveform, continuous chain) so the user always has central control over listening.
   *
   * @param db      context
   * @param access  control
   * @param chainId of parent
   * @return array of records
   */
  private Result<LinkRecord> readAllFromSecondsUTC(DSLContext db, Access access, ULong chainId, ULong fromSecondsUTC) {

    // play buffer delay/ahead seconds
    Instant from = new Date(fromSecondsUTC.longValue() * MILLIS_PER_SECOND).toInstant();
    Timestamp maxBeginAt = Timestamp.from(from.plusSeconds(Config.playBufferAheadSeconds()));
    Timestamp minEndAt = Timestamp.from(from.minusSeconds(Config.playBufferDelaySeconds()));

    if (access.isTopLevel())
      return resultInto(LINK, db.select(LINK.fields())
        .from(LINK)
        .where(LINK.CHAIN_ID.eq(chainId))
        .and(LINK.BEGIN_AT.lessOrEqual(maxBeginAt))
        .and(LINK.END_AT.greaterOrEqual(minEndAt))
        .and(LINK.STATE.eq(LinkState.Dubbed.toString()))
        .orderBy(LINK.OFFSET.desc())
        .limit(Config.limitLinkReadSize())
        .fetch());
    else
      return resultInto(LINK, db.select(LINK.fields())
        .from(LINK)
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(LINK.CHAIN_ID.eq(chainId))
        .and(LINK.BEGIN_AT.lessOrEqual(maxBeginAt))
        .and(LINK.END_AT.greaterOrEqual(minEndAt))
        .and(LINK.STATE.eq(LinkState.Dubbed.toString()))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .orderBy(LINK.OFFSET.desc())
        .limit(Config.limitLinkReadSize())
        .fetch());
  }

  /**
   Read all records in a given state

   @return array of records
   @param db     context
   @param access control
   @param state  to read links in
   */
  private Result<LinkRecord> readAllInState(DSLContext db, Access access, LinkState state) throws Exception {
    requireTopLevel(access);
    return resultInto(Tables.LINK, db.select(Tables.LINK.fields())
      .from(Tables.LINK)
      .where(Tables.LINK.STATE.eq(state.toString()))
      .or(Tables.LINK.STATE.eq(state.toString().toLowerCase()))
      .fetch());
  }


  /**
   * Update a record using a model wrapper
   *
   * @param db     context
   * @param access control
   * @param id     of link to update
   * @param entity wrapper
   * @throws BusinessException on failure
   * @throws DatabaseException on failure
   */
  private void update(DSLContext db, Access access, ULong id, Link entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    fieldValues.put(LINK.ID, id);
    update(db, access, id, fieldValues);
  }

  /**
   * Update the state of a record
   *
   * @param db     context
   * @param access control
   * @param id     of record
   * @param state  to update to
   * @throws BusinessException if a Business Rule is violated
   */
  private void updateState(DSLContext db, Access access, ULong id, LinkState state) throws Exception {
    Map<Field, Object> fieldValues = ImmutableMap.of(
      LINK.ID, id,
      LINK.STATE, state.toString()
    );

    update(db, access, id, fieldValues);

    if (0 == executeUpdate(db, LINK, fieldValues))
      throw new BusinessException("No records updated.");
  }

  /**
   * Update a record
   *
   * @param db          context
   * @param access      control
   * @param id          of record
   * @param fieldValues to update with
   * @throws BusinessException if a Business Rule is violated
   */
  private void update(DSLContext db, Access access, ULong id, Map<Field, Object> fieldValues) throws BusinessException, CancelException {
    requireTopLevel(access);

    // validate and cache to-state
    LinkState toState = LinkState.validate(fieldValues.get(LINK.STATE).toString());

    // fetch existing link; further logic is based on its current state
    LinkRecord link = db.selectFrom(LINK).where(LINK.ID.eq(id)).fetchOne();
    requireExists("Link #" + id, link);

    // logic based on existing Link State
    switch (LinkState.validate(link.getState())) {

      case Planned:
        onlyAllowTransitions(toState, LinkState.Planned, LinkState.Crafting);
        break;

      case Crafting:
        onlyAllowTransitions(toState, LinkState.Crafting, LinkState.Crafted, LinkState.Failed);
        break;

      case Crafted:
        onlyAllowTransitions(toState, LinkState.Crafted, LinkState.Dubbing);
        break;

      case Dubbing:
        onlyAllowTransitions(toState, LinkState.Dubbing, LinkState.Dubbed, LinkState.Failed);
        break;

      case Dubbed:
        onlyAllowTransitions(toState, LinkState.Dubbed);
        break;

      case Failed:
        onlyAllowTransitions(toState, LinkState.Failed);
        break;

      default:
        onlyAllowTransitions(toState, LinkState.Planned);
        break;
    }

    // [#128] cannot change chainId of a link
    Object updateChainId = fieldValues.get(LINK.CHAIN_ID);
    if (exists(updateChainId) && !updateChainId.equals(link.getChainId()))
      throw new BusinessException("cannot change chainId of a link");

    // This "change from state to state" complexity
    // is required in order to prevent duplicate
    // state-changes of the same link
    UpdateSetFirstStep<LinkRecord> update = db.update(LINK);
    fieldValues.forEach(update::set);
    int rowsAffected = update.set(LINK.STATE, toState.toString())
      .where(LINK.ID.eq(id))
      .and(LINK.STATE.eq(link.getState()))
      .execute();

    if (0 == rowsAffected)
      throw new BusinessException("No records updated.");

  }

  /**
   * Destroy a Link, its child entities, and S3 object
   * [#301] Link destruction (by Eraseworker) should not spike database CPU
   *
   * @param db     context
   * @param access control
   * @param linkId to delete
   * @throws Exception         if database failure
   * @throws ConfigException   if not configured properly
   * @throws BusinessException if fails business rule
   */
  private void destroy(DSLContext db, Access access, ULong linkId) throws Exception {
    requireTopLevel(access);

    // Link
    LinkRecord link = db.selectFrom(LINK)
      .where(LINK.ID.eq(linkId))
      .fetchOne();
    requireExists("Link #" + linkId, link);

    // Only Delete link waveform from S3 if non-null
    String waveformKey = link.get(LINK.WAVEFORM_KEY);
    if (Objects.nonNull(waveformKey))
      amazonProvider.deleteS3Object(
        Config.linkFileBucket(),
        waveformKey);

    // Read all choice id
    List<ULong> choiceIds = Lists.newArrayList();
    db.select(CHOICE.ID).from(CHOICE)
      .where(CHOICE.LINK_ID.eq(linkId)).fetch()
      .forEach(record -> choiceIds.add(record.get(CHOICE.ID)));

    // Read all arrangement id
    List<ULong> arrangementIds = Lists.newArrayList();
    db.select(ARRANGEMENT.ID).from(ARRANGEMENT)
      .where(ARRANGEMENT.CHOICE_ID.in(choiceIds)).fetch()
      .forEach(record -> arrangementIds.add(record.get(ARRANGEMENT.ID)));

    // Delete Picks in arrangements
    db.deleteFrom(PICK)
      .where(PICK.ARRANGEMENT_ID.in(arrangementIds))
      .execute();

    // Delete Arrangements
    db.deleteFrom(ARRANGEMENT)
      .where(ARRANGEMENT.ID.in(arrangementIds))
      .execute();

    // Delete Choices
    db.deleteFrom(CHOICE)
      .where(CHOICE.LINK_ID.eq(linkId)).execute();

    // Delete Link Chords
    db.deleteFrom(LINK_CHORD)
      .where(LINK_CHORD.LINK_ID.eq(linkId))
      .execute();

    // Delete Link Memes
    db.deleteFrom(LINK_MEME)
      .where(LINK_MEME.LINK_ID.eq(linkId))
      .execute();

    // Delete Link Messages
    db.deleteFrom(LINK_MESSAGE)
      .where(LINK_MESSAGE.LINK_ID.eq(linkId))
      .execute();

    // Delete Link
    db.deleteFrom(LINK)
      .where(LINK.ID.eq(linkId))
      .execute();

  }

  /**
   * Require state is in an array of states
   *
   * @param toState       to check
   * @param allowedStates required to be in
   * @throws CancelException if not in required states
   */
  private static void onlyAllowTransitions(LinkState toState, LinkState... allowedStates) throws CancelException {
    List<String> allowedStateNames = Lists.newArrayList();
    for (LinkState search : allowedStates) {
      allowedStateNames.add(search.toString());
      if (Objects.equals(search, toState)) {
        return;
      }
    }
    throw new CancelException(String.format("transition to %s not in allowed (%s)",
      toState, CSV.join(allowedStateNames)));
  }


}
