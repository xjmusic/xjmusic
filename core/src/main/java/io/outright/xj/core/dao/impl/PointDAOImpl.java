// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.dao.PointDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.point.Point;
import io.outright.xj.core.tables.records.PointRecord;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.Map;

import static io.outright.xj.core.Tables.ARRANGEMENT;
import static io.outright.xj.core.Tables.LINK;
import static io.outright.xj.core.tables.Chain.CHAIN;
import static io.outright.xj.core.tables.Choice.CHOICE;
import static io.outright.xj.core.tables.Morph.MORPH;
import static io.outright.xj.core.tables.Point.POINT;

public class PointDAOImpl extends DAOImpl implements PointDAO {

  @Inject
  public PointDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public PointRecord create(Access access, Point entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public PointRecord readOne(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneRecord(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Result<PointRecord> readAll(Access access, ULong morphId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, morphId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, ULong id, Point entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, id, entity);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(Access access, ULong pointId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(tx.getContext(), access, pointId);
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
  private PointRecord createRecord(DSLContext db, Access access, Point entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    requireTopLevel(access);

    requireExists("Morph", db.select(MORPH.ID).from(MORPH)
      .where(MORPH.ID.eq(entity.getMorphId()))
      .fetchOne());

    return executeCreate(db, POINT, fieldValues);
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private PointRecord readOneRecord(DSLContext db, Access access, ULong id) {
    if (access.isTopLevel())
      return db.selectFrom(POINT)
        .where(POINT.ID.eq(id))
        .fetchOne();
    else
      return recordInto(POINT, db.select(POINT.fields())
        .from(POINT)
        .join(MORPH).on(MORPH.ID.eq(POINT.MORPH_ID))
        .join(ARRANGEMENT).on(ARRANGEMENT.ID.eq(MORPH.ARRANGEMENT_ID))
        .join(CHOICE).on(CHOICE.ID.eq(ARRANGEMENT.CHOICE_ID))
        .join(LINK).on(LINK.ID.eq(CHOICE.LINK_ID))
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(POINT.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
  }

  /**
   Read all records in parent by id

   @param db      context
   @param access  control
   @param morphId of parent
   @return array of records
   */
  private Result<PointRecord> readAll(DSLContext db, Access access, ULong morphId) throws SQLException {
    if (access.isTopLevel())
      return resultInto(POINT, db.select(POINT.fields())
        .from(POINT)
        .where(POINT.MORPH_ID.eq(morphId))
        .fetch());
    else
      return resultInto(POINT, db.select(POINT.fields())
        .from(POINT)
        .join(MORPH).on(MORPH.ID.eq(POINT.MORPH_ID))
        .join(ARRANGEMENT).on(ARRANGEMENT.ID.eq(MORPH.ARRANGEMENT_ID))
        .join(CHOICE).on(CHOICE.ID.eq(ARRANGEMENT.CHOICE_ID))
        .join(LINK).on(LINK.ID.eq(CHOICE.LINK_ID))
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(POINT.MORPH_ID.eq(morphId))
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
  private void update(DSLContext db, Access access, ULong id, Point entity) throws BusinessException, DatabaseException {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();
    fieldValues.put(POINT.ID, id);

    requireTopLevel(access);

    requireExists("existing Point with immutable Morph membership",
      db.selectFrom(POINT)
        .where(POINT.ID.eq(id))
        .and(POINT.MORPH_ID.eq(entity.getMorphId()))
        .fetchOne());

    if (executeUpdate(db, POINT, fieldValues) == 0)
      throw new BusinessException("No records updated.");
  }

  /**
   Delete a Point

   @param db     context
   @param access control
   @param id     to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private void delete(DSLContext db, Access access, ULong id) throws Exception {
    requireTopLevel(access);

    requireExists("Point", db.selectFrom(POINT)
      .where(POINT.ID.eq(id))
      .fetchOne());

    db.deleteFrom(POINT)
      .where(POINT.ID.eq(id))
      .execute();
  }

}
