// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.app.access.impl.Access;
import io.xj.core.app.exception.BusinessException;
import io.xj.core.app.exception.ConfigException;
import io.xj.core.app.exception.DatabaseException;
import io.xj.core.dao.PickDAO;
import io.xj.core.database.sql.impl.SQLConnection;
import io.xj.core.database.sql.SQLDatabaseProvider;
import io.xj.core.model.pick.Pick;
import io.xj.core.tables.records.PickRecord;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.Map;

import static io.xj.core.Tables.ARRANGEMENT;
import static io.xj.core.Tables.CHAIN;
import static io.xj.core.Tables.CHOICE;
import static io.xj.core.Tables.LINK;
import static io.xj.core.Tables.PICK;

public class PickDAOImpl extends DAOImpl implements PickDAO {

  @Inject
  public PickDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public PickRecord create(Access access, Pick entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public PickRecord readOne(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneRecord(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Result<PickRecord> readAll(Access access, ULong arrangementId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, arrangementId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Result<PickRecord> readAllInLink(Access access, ULong linkId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInLink(tx.getContext(), access, linkId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, ULong id, Pick entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, id, entity);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(Access access, ULong pickId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(tx.getContext(), access, pickId);
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
  private PickRecord createRecord(DSLContext db, Access access, Pick entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    requireTopLevel(access);

    requireExists("Arrangement", db.select(ARRANGEMENT.ID).from(ARRANGEMENT)
      .where(ARRANGEMENT.ID.eq(entity.getArrangementId()))
      .fetchOne());

    return executeCreate(db, PICK, fieldValues);
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private PickRecord readOneRecord(DSLContext db, Access access, ULong id) {
    if (access.isTopLevel())
      return db.selectFrom(PICK)
        .where(PICK.ID.eq(id))
        .fetchOne();
    else
      return recordInto(PICK, db.select(PICK.fields())
        .from(PICK)
        .join(ARRANGEMENT).on(ARRANGEMENT.ID.eq(PICK.ARRANGEMENT_ID))
        .join(CHOICE).on(CHOICE.ID.eq(ARRANGEMENT.CHOICE_ID))
        .join(LINK).on(LINK.ID.eq(CHOICE.LINK_ID))
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(PICK.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
  }

  /**
   Read all records in parent by id

   @param db            context
   @param access        control
   @param arrangementId of parent
   @return array of records
   */
  private Result<PickRecord> readAll(DSLContext db, Access access, ULong arrangementId) throws SQLException {
    if (access.isTopLevel())
      return resultInto(PICK, db.select(PICK.fields())
        .from(PICK)
        .where(PICK.ARRANGEMENT_ID.eq(arrangementId))
        .fetch());
    else
      return resultInto(PICK, db.select(PICK.fields())
        .from(PICK)
        .join(ARRANGEMENT).on(ARRANGEMENT.ID.eq(PICK.ARRANGEMENT_ID))
        .join(CHOICE).on(CHOICE.ID.eq(ARRANGEMENT.CHOICE_ID))
        .join(LINK).on(LINK.ID.eq(CHOICE.LINK_ID))
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(PICK.ARRANGEMENT_ID.eq(arrangementId))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetch());
  }

  /**
   Read all records in parent's parent's parent (link) by id

   @param db            context
   @param access        control
   @param linkId of parent
   @return array of records
   */
  private Result<PickRecord> readAllInLink(DSLContext db, Access access, ULong linkId) throws SQLException, BusinessException {
    requireTopLevel(access);
    return resultInto(PICK, db.select(PICK.fields())
      .from(PICK)
      .join(ARRANGEMENT).on(ARRANGEMENT.ID.eq(PICK.ARRANGEMENT_ID))
      .join(CHOICE).on(CHOICE.ID.eq(ARRANGEMENT.CHOICE_ID))
      .where(CHOICE.LINK_ID.eq(linkId))
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
  private void update(DSLContext db, Access access, ULong id, Pick entity) throws BusinessException, DatabaseException {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();
    fieldValues.put(PICK.ID, id);

    requireTopLevel(access);

    requireExists("existing Pick with immutable Arrangement membership",
      db.selectFrom(PICK)
        .where(PICK.ID.eq(id))
        .and(PICK.ARRANGEMENT_ID.eq(entity.getArrangementId()))
        .fetchOne());

    if (executeUpdate(db, PICK, fieldValues) == 0)
      throw new BusinessException("No records updated.");
  }

  /**
   Delete a Pick

   @param db     context
   @param access control
   @param id     to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private void delete(DSLContext db, Access access, ULong id) throws Exception {
    requireTopLevel(access);

    requireExists("Pick", db.selectFrom(PICK)
      .where(PICK.ID.eq(id))
      .fetchOne());

    db.deleteFrom(PICK)
      .where(PICK.ID.eq(id))
      .execute();
  }

}
