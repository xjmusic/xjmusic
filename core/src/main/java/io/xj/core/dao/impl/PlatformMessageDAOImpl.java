// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.PlatformMessageDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.platform_message.PlatformMessage;
import io.xj.core.model.role.Role;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;
import io.xj.core.tables.records.PlatformMessageRecord;
import io.xj.core.timestamp.TimestampUTC;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.util.Map;

import static io.xj.core.Tables.PLATFORM_MESSAGE;

public class PlatformMessageDAOImpl extends DAOImpl implements PlatformMessageDAO {
  private static final Long secondsPerDay = 86400L;

  @Inject
  public PlatformMessageDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public PlatformMessageRecord create(Access access, PlatformMessage entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public PlatformMessageRecord readOne(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneRecord(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Result<PlatformMessageRecord> readAllPreviousDays(Access access, Integer previousDays) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllPreviousDays(tx.getContext(), access, previousDays));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(tx.getContext(), access, id);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  /**
   Create a record

   @param db     context
   @param access control
   @param entity for new record
   @return newly readMany record
   @throws BusinessException on failure
   */
  private PlatformMessageRecord createRecord(DSLContext db, Access access, PlatformMessage entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    requireRole("platform access", access, Role.ADMIN, Role.ENGINEER);

    return executeCreate(db, PLATFORM_MESSAGE, fieldValues);
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  @Nullable
  private PlatformMessageRecord readOneRecord(DSLContext db, Access access, ULong id) throws Exception {
    requireRole("platform access", access, Role.ADMIN, Role.ENGINEER);

    return db.selectFrom(PLATFORM_MESSAGE)
      .where(PLATFORM_MESSAGE.ID.eq(id))
      .fetchOne();
  }

  /**
   Read all records in parent record by id

   @param db           context
   @param access       control
   @param previousDays of parent
   @return array of records
   */
  private Result<PlatformMessageRecord> readAllPreviousDays(DSLContext db, Access access, Integer previousDays) throws Exception {
    requireRole("platform access", access, Role.ADMIN, Role.ENGINEER);

    return resultInto(PLATFORM_MESSAGE, db.select(PLATFORM_MESSAGE.fields())
      .from(PLATFORM_MESSAGE)
      .where(PLATFORM_MESSAGE.CREATED_AT.ge(TimestampUTC.nowMinusSeconds(previousDays * secondsPerDay)))
      .orderBy(PLATFORM_MESSAGE.CREATED_AT.desc())
      .fetch());
  }

  /**
   Delete an PlatformMessage

   @param db context
   @param id to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private void delete(DSLContext db, Access access, ULong id) throws Exception {
    requireTopLevel(access);

    requireExists("PlatformMessage", db.selectCount().from(PLATFORM_MESSAGE)
      .where(PLATFORM_MESSAGE.ID.eq(id))
      .fetchOne(0, int.class));

    db.deleteFrom(PLATFORM_MESSAGE)
      .where(PLATFORM_MESSAGE.ID.eq(id))
      .execute();
  }

}
