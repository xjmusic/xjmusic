// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.PlatformMessageDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.platform_message.PlatformMessage;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;
import io.xj.core.timestamp.TimestampUTC;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;
import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;
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
  public PlatformMessage create(Access access, PlatformMessage entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public PlatformMessage readOne(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Collection<PlatformMessage> readAllPreviousDays(Access access, Integer previousDays) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllPreviousDays(tx.getContext(), access, previousDays));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(tx.getContext(), access, ULong.valueOf(id));
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
  private static PlatformMessage create(DSLContext db, Access access, PlatformMessage entity) throws BusinessException {
    requireRole("platform access", access, UserRoleType.Admin, UserRoleType.Engineer);

    entity.validate();
    Map<Field, Object> fieldValues = fieldValueMap(entity);
    return modelFrom(executeCreate(db, PLATFORM_MESSAGE, fieldValues), PlatformMessage.class);
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  @Nullable
  private static PlatformMessage readOne(DSLContext db, Access access, ULong id) throws Exception {
    requireRole("platform access", access, UserRoleType.Admin, UserRoleType.Engineer);

    return modelFrom(db.selectFrom(PLATFORM_MESSAGE)
      .where(PLATFORM_MESSAGE.ID.eq(id))
      .fetchOne(), PlatformMessage.class);
  }

  /**
   Read all records in parent record by id

   @param db           context
   @param access       control
   @param previousDays of parent
   @return array of records
   */
  private static Collection<PlatformMessage> readAllPreviousDays(DSLContext db, Access access, Integer previousDays) throws Exception {
    requireRole("platform access", access, UserRoleType.Admin, UserRoleType.Engineer);

    return modelsFrom(db.select(PLATFORM_MESSAGE.fields())
      .from(PLATFORM_MESSAGE)
      .where(PLATFORM_MESSAGE.CREATED_AT.ge(TimestampUTC.nowMinusSeconds(previousDays * secondsPerDay)))
      .orderBy(PLATFORM_MESSAGE.CREATED_AT.desc())
      .fetch(), PlatformMessage.class);
  }

  /**
   Delete an PlatformMessage

   @param db context
   @param id to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private static void delete(DSLContext db, Access access, ULong id) throws Exception {
    requireTopLevel(access);

    requireExists("PlatformMessage", db.selectCount().from(PLATFORM_MESSAGE)
      .where(PLATFORM_MESSAGE.ID.eq(id))
      .fetchOne(0, int.class));

    db.deleteFrom(PLATFORM_MESSAGE)
      .where(PLATFORM_MESSAGE.ID.eq(id))
      .execute();
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(PlatformMessage entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(PLATFORM_MESSAGE.BODY, entity.getBody());
    fieldValues.put(PLATFORM_MESSAGE.TYPE, entity.getType());
    return fieldValues;
  }


}
