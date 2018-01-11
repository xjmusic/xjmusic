// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.ChoiceDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.SelectOffsetStep;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;
import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.CHOICE;
import static io.xj.core.Tables.PHASE;
import static io.xj.core.tables.Arrangement.ARRANGEMENT;
import static io.xj.core.tables.Chain.CHAIN;
import static io.xj.core.tables.Link.LINK;
import static org.jooq.impl.DSL.groupConcat;

public class ChoiceDAOImpl extends DAOImpl implements ChoiceDAO {
  private static final String KEY_AVAILABLE_PHASE_OFFSETS = "available_phase_offsets";

  @Inject
  public ChoiceDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public Choice create(Access access, Choice entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Choice readOne(Access access, BigInteger choiceId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(choiceId)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Nullable
  @Override
  public Choice readOneLinkPattern(Access access, BigInteger linkId, BigInteger patternId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneLinkPattern(tx.getContext(), access, ULong.valueOf(linkId), ULong.valueOf(patternId)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Choice readOneLinkTypeWithAvailablePhaseOffsets(Access access, BigInteger linkId, PatternType patternType) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneLinkTypeWithAvailablePhaseOffsets(tx.getContext(), access, ULong.valueOf(linkId), patternType));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Collection<Choice> readAll(Access access, BigInteger linkId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, ULong.valueOf(linkId)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Choice> readAllInLinks(Access access, Collection<BigInteger> linkIds) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInLinks(tx.getContext(), access, idCollection(linkIds)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger choiceId, Choice entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, ULong.valueOf(choiceId), entity);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(Access access, BigInteger choiceId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(tx.getContext(), access, ULong.valueOf(choiceId));
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
  private static Choice create(DSLContext db, Access access, Choice entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    requireTopLevel(access);

    requireExists("Link", db.selectCount().from(LINK)
      .where(LINK.ID.eq(ULong.valueOf(entity.getLinkId())))
      .fetchOne(0, int.class));

    return modelFrom(executeCreate(db, CHOICE, fieldValues), Choice.class);
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private static Choice readOne(DSLContext db, Access access, ULong id) throws BusinessException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(CHOICE)
        .where(CHOICE.ID.eq(id))
        .fetchOne(), Choice.class);
    else
      return modelFrom(db.select(CHOICE.fields())
        .from(CHOICE)
        .join(LINK).on(LINK.ID.eq(CHOICE.LINK_ID))
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(CHOICE.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), Choice.class);
  }

  /**
   Read one record binding an pattern to a link

   @param db        context
   @param access    control
   @param linkId    to get choice for
   @param patternId to get choice for
   @return record
   */
  private static Choice readOneLinkPattern(DSLContext db, Access access, ULong linkId, ULong patternId) throws BusinessException {
    requireTopLevel(access);
    return modelFrom(db.selectFrom(CHOICE)
      .where(CHOICE.LINK_ID.eq(linkId))
      .and(CHOICE.PATTERN_ID.eq(patternId))
      .fetchOne(), Choice.class);
  }

  /**
   Read one record

   @param db          context
   @param access      control
   @param linkId      of record
   @param patternType of which to read one link with available offsets
   @return record
   */
  private static Choice readOneLinkTypeWithAvailablePhaseOffsets(DSLContext db, Access access, ULong linkId, PatternType patternType) throws BusinessException {
    requireTopLevel(access);

    SelectOffsetStep<?> query = db.select(
      CHOICE.ID,
      CHOICE.PATTERN_ID,
      CHOICE.LINK_ID,
      CHOICE.TYPE,
      CHOICE.PHASE_OFFSET,
      CHOICE.TRANSPOSE,
      groupConcat(PHASE.OFFSET, ",").as(KEY_AVAILABLE_PHASE_OFFSETS)
    )
      .from(PHASE)
      .join(CHOICE).on(CHOICE.PATTERN_ID.eq(PHASE.PATTERN_ID))
      .where(CHOICE.LINK_ID.eq(linkId))
      .and(CHOICE.TYPE.eq(patternType.toString()))
      .groupBy(CHOICE.ID, CHOICE.LINK_ID, CHOICE.PATTERN_ID, CHOICE.PHASE_OFFSET, CHOICE.TRANSPOSE)
      .limit(1);

    Record record = query.fetchOne();
    Choice model = modelFrom(record, Choice.class);
    if (Objects.nonNull(model)) {
      model.setAvailablePhaseOffsets((String) record.get(KEY_AVAILABLE_PHASE_OFFSETS));
    }

    return model;
  }


  /**
   Read all records in parent by id

   @param db     context
   @param access control
   @param linkId of parent
   @return array of records
   */
  private static Collection<Choice> readAll(DSLContext db, Access access, ULong linkId) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.select(CHOICE.fields()).from(CHOICE)
        .where(CHOICE.LINK_ID.eq(linkId))
        .fetch(), Choice.class);
    else
      return modelsFrom(db.select(CHOICE.fields()).from(CHOICE)
        .join(LINK).on(LINK.ID.eq(CHOICE.LINK_ID))
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(CHOICE.LINK_ID.eq(linkId))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .fetch(), Choice.class);
  }

  /**
   Read all records in parent records by ids

   @param db      context
   @param access  control
   @param linkIds id of parent's parent (the chain)
   @return array of records
   */
  private static Collection<Choice> readAllInLinks(DSLContext db, Access access, Collection<ULong> linkIds) throws Exception {
    requireAccessToLinks(db, access, linkIds);

    return modelsFrom(db.select(CHOICE.fields()).from(CHOICE)
        .where(CHOICE.LINK_ID.in(linkIds))
        .orderBy(CHOICE.TYPE)
        .fetch(), Choice.class);
  }

  /**
   Update a record

   @param db     context
   @param access control
   @param id     of record
   @param entity to update with
   @throws BusinessException if a Business Rule is violated
   */
  private static void update(DSLContext db, Access access, ULong id, Choice entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);
    fieldValues.put(CHOICE.ID, id);

    requireTopLevel(access);

    requireExists("existing Choice with immutable Link membership",
      db.selectCount().from(CHOICE)
        .where(CHOICE.ID.eq(id))
        .and(CHOICE.LINK_ID.eq(ULong.valueOf(entity.getLinkId())))
        .fetchOne(0, int.class));

    if (0 == executeUpdate(db, CHOICE, fieldValues))
      throw new BusinessException("No records updated.");
  }

  /**
   Delete a Choice

   @param db     context
   @param access control
   @param id     to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private static void delete(DSLContext db, Access access, ULong id) throws Exception {
    requireTopLevel(access);

    requireExists("Choice", db.selectCount().from(CHOICE)
      .where(CHOICE.ID.eq(id))
      .fetchOne(0, int.class));

    requireNotExists("Arrangement in Choice", db.selectCount().from(ARRANGEMENT)
      .where(ARRANGEMENT.CHOICE_ID.eq(id))
      .fetchOne(0, int.class));

    db.deleteFrom(CHOICE)
      .where(CHOICE.ID.eq(id))
      .andNotExists(
        db.select(ARRANGEMENT.ID)
          .from(ARRANGEMENT)
          .where(ARRANGEMENT.CHOICE_ID.eq(id))
      )
      .execute();
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(Choice entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(CHOICE.LINK_ID, entity.getLinkId());
    fieldValues.put(CHOICE.PATTERN_ID, entity.getPatternId());
    fieldValues.put(CHOICE.TYPE, entity.getType());
    fieldValues.put(CHOICE.TRANSPOSE, entity.getTranspose());
    fieldValues.put(CHOICE.PHASE_OFFSET, entity.getPhaseOffset());
    return fieldValues;
  }

}
