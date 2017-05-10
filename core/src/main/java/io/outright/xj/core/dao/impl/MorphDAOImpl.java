// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.dao.MorphDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.morph.Morph;
import io.outright.xj.core.tables.records.MorphRecord;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.Map;

import static io.outright.xj.core.Tables.LINK;
import static io.outright.xj.core.tables.Arrangement.ARRANGEMENT;
import static io.outright.xj.core.tables.Chain.CHAIN;
import static io.outright.xj.core.tables.Choice.CHOICE;
import static io.outright.xj.core.tables.Morph.MORPH;
import static io.outright.xj.core.tables.Pick.PICK;
import static io.outright.xj.core.tables.Point.POINT;

public class MorphDAOImpl extends DAOImpl implements MorphDAO {

  @Inject
  public MorphDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public MorphRecord createRecord(Access access, Morph entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public MorphRecord readOneRecord(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneRecord(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Result<MorphRecord> readAll(Access access, ULong arrangementId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, arrangementId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, ULong id, Morph entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, id, entity);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(Access access, ULong morphId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(tx.getContext(), access, morphId);
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
  private MorphRecord createRecord(DSLContext db, Access access, Morph entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    requireTopLevel(access);

    requireExists("Arrangement", db.select(ARRANGEMENT.ID).from(ARRANGEMENT)
      .where(ARRANGEMENT.ID.eq(entity.getArrangementId()))
      .fetchOne());

    return executeCreate(db, MORPH, fieldValues);
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private MorphRecord readOneRecord(DSLContext db, Access access, ULong id) {
    if (access.isTopLevel())
      return db.selectFrom(MORPH)
        .where(MORPH.ID.eq(id))
        .fetchOne();
    else
      return recordInto(MORPH, db.select(MORPH.fields())
        .from(MORPH)
        .join(ARRANGEMENT).on(ARRANGEMENT.ID.eq(MORPH.ARRANGEMENT_ID))
        .join(CHOICE).on(CHOICE.ID.eq(ARRANGEMENT.CHOICE_ID))
        .join(LINK).on(LINK.ID.eq(CHOICE.LINK_ID))
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(MORPH.ID.eq(id))
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
  private Result<MorphRecord> readAll(DSLContext db, Access access, ULong arrangementId) throws SQLException {
    if (access.isTopLevel())
      return resultInto(MORPH, db.select(MORPH.fields())
        .from(MORPH)
        .where(MORPH.ARRANGEMENT_ID.eq(arrangementId))
        .fetch());
    else
      return resultInto(MORPH, db.select(MORPH.fields())
        .from(MORPH)
        .join(ARRANGEMENT).on(ARRANGEMENT.ID.eq(MORPH.ARRANGEMENT_ID))
        .join(CHOICE).on(CHOICE.ID.eq(ARRANGEMENT.CHOICE_ID))
        .join(LINK).on(LINK.ID.eq(CHOICE.LINK_ID))
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(MORPH.ARRANGEMENT_ID.eq(arrangementId))
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
  private void update(DSLContext db, Access access, ULong id, Morph entity) throws BusinessException, DatabaseException {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();
    fieldValues.put(MORPH.ID, id);

    requireTopLevel(access);

    requireExists("existing Morph with immutable Arrangement membership",
      db.selectFrom(MORPH)
        .where(MORPH.ID.eq(id))
        .and(MORPH.ARRANGEMENT_ID.eq(entity.getArrangementId()))
        .fetchOne());

    if (executeUpdate(db, MORPH, fieldValues) == 0)
      throw new BusinessException("No records updated.");
  }

  /**
   Delete a Morph

   @param db     context
   @param access control
   @param id     to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private void delete(DSLContext db, Access access, ULong id) throws Exception {
    requireTopLevel(access);

    requireExists("Morph", db.selectFrom(MORPH)
      .where(MORPH.ID.eq(id))
      .fetchOne());

    requireNotExists("Pick in Morph", db.select(PICK.ID)
      .from(PICK)
      .where(PICK.MORPH_ID.eq(id))
      .fetch());

    requireNotExists("Point in Morph", db.select(POINT.ID)
      .from(POINT)
      .where(POINT.MORPH_ID.eq(id))
      .fetch());

    db.deleteFrom(MORPH)
      .where(MORPH.ID.eq(id))
      .andNotExists(
        db.select(PICK.ID)
          .from(PICK)
          .where(PICK.MORPH_ID.eq(id))
      ).andNotExists(
      db.select(POINT.ID)
        .from(POINT)
        .where(POINT.MORPH_ID.eq(id))
    )
      .execute();
  }

}
