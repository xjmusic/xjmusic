// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.dao.ArrangementDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.arrangement.Arrangement;
import io.outright.xj.core.tables.records.ArrangementRecord;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.Map;

import static io.outright.xj.core.Tables.ARRANGEMENT;
import static io.outright.xj.core.Tables.CHAIN;
import static io.outright.xj.core.Tables.CHOICE;
import static io.outright.xj.core.Tables.LINK;
import static io.outright.xj.core.tables.Pick.PICK;

public class ArrangementDAOImpl extends DAOImpl implements ArrangementDAO {

  @Inject
  public ArrangementDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public ArrangementRecord create(Access access, Arrangement entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public ArrangementRecord readOne(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneRecord(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Result<ArrangementRecord> readAll(Access access, ULong choiceId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, choiceId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, ULong id, Arrangement entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, id, entity);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(Access access, ULong arrangementId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(tx.getContext(), access, arrangementId);
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
  private ArrangementRecord createRecord(DSLContext db, Access access, Arrangement entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    requireTopLevel(access);

    requireExists("Choice", db.select(CHOICE.ID).from(CHOICE)
      .where(CHOICE.ID.eq(entity.getChoiceId()))
      .fetchOne());

    return executeCreate(db, ARRANGEMENT, fieldValues);
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  @Nullable
  private ArrangementRecord readOneRecord(DSLContext db, Access access, ULong id) {
    if (access.isTopLevel())
      return db.selectFrom(ARRANGEMENT)
        .where(ARRANGEMENT.ID.eq(id))
        .fetchOne();
    else
      return recordInto(ARRANGEMENT, db.select(ARRANGEMENT.fields())
        .from(ARRANGEMENT)
        .join(CHOICE).on(CHOICE.ID.eq(ARRANGEMENT.CHOICE_ID))
        .join(LINK).on(LINK.ID.eq(CHOICE.LINK_ID))
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(ARRANGEMENT.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
  }

  /**
   Read all records in parent by id

   @param db       context
   @param access   control
   @param choiceId of parent
   @return array of records
   */
  private Result<ArrangementRecord> readAll(DSLContext db, Access access, ULong choiceId) throws SQLException {
    if (access.isTopLevel())
      return resultInto(ARRANGEMENT, db.select(ARRANGEMENT.fields())
        .from(ARRANGEMENT)
        .where(ARRANGEMENT.CHOICE_ID.eq(choiceId))
        .fetch());
    else
      return resultInto(ARRANGEMENT, db.select(ARRANGEMENT.fields())
        .from(ARRANGEMENT)
        .join(CHOICE).on(CHOICE.ID.eq(ARRANGEMENT.CHOICE_ID))
        .join(LINK).on(LINK.ID.eq(CHOICE.LINK_ID))
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(ARRANGEMENT.CHOICE_ID.eq(choiceId))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetch());
  }

  /**
   Update a record

   @param db     context
   @param access control
   @param id     of record
   @param entity to update with
   @throws BusinessException if a Business Rule is violated
   */
  private void update(DSLContext db, Access access, ULong id, Arrangement entity) throws BusinessException, DatabaseException {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();
    fieldValues.put(ARRANGEMENT.ID, id);

    requireTopLevel(access);

    requireExists("existing Arrangement with immutable Choice membership",
      db.selectFrom(ARRANGEMENT)
        .where(ARRANGEMENT.ID.eq(id))
        .and(ARRANGEMENT.CHOICE_ID.eq(entity.getChoiceId()))
        .fetchOne());

    if (executeUpdate(db, ARRANGEMENT, fieldValues) == 0)
      throw new BusinessException("No records updated.");
  }

  /**
   Delete a Arrangement

   @param db     context
   @param access control
   @param id     to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private void delete(DSLContext db, Access access, ULong id) throws Exception {
    requireTopLevel(access);

    requireExists("Arrangement", db.selectFrom(ARRANGEMENT)
      .where(ARRANGEMENT.ID.eq(id))
      .fetchOne());

    requireNotExists("Pick", db.selectFrom(PICK)
      .where(PICK.ARRANGEMENT_ID.eq(id))
      .fetch());

    db.deleteFrom(ARRANGEMENT)
      .where(ARRANGEMENT.ID.eq(id))
      .execute();
  }

}
