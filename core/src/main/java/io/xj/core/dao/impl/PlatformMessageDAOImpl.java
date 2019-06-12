// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import com.google.api.client.util.Maps;
import com.google.inject.Inject;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.PlatformMessageDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.message.platform.PlatformMessage;
import io.xj.core.model.user.role.UserRoleType;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
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

  /**
   Create a record

   @param db     context
   @param access control
   @param entity for new record
   @return newly readMany record
   @throws CoreException on failure
   */
  private static PlatformMessage create(DSLContext db, Access access, PlatformMessage entity) throws CoreException {
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
  private static PlatformMessage readOne(DSLContext db, Access access, ULong id) throws CoreException {
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
  private static Collection<PlatformMessage> readAllPreviousDays(DSLContext db, Access access, Integer previousDays) throws CoreException {
    requireRole("platform access", access, UserRoleType.Admin, UserRoleType.Engineer);

    return modelsFrom(db.select(PLATFORM_MESSAGE.fields())
      .from(PLATFORM_MESSAGE)
      .where(PLATFORM_MESSAGE.CREATED_AT.ge(Timestamp.from(Instant.now().minusSeconds(previousDays * secondsPerDay))))
      .orderBy(PLATFORM_MESSAGE.CREATED_AT.desc())
      .fetch(), PlatformMessage.class);
  }

  /**
   Delete an PlatformMessage

   @param db context
   @param id to delete
   @throws CoreException if database failure
   @throws CoreException if not configured properly
   @throws CoreException if fails business rule
   */
  private static void delete(DSLContext db, Access access, ULong id) throws CoreException {
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

  @Override
  public PlatformMessage create(Access access, PlatformMessage entity) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public PlatformMessage readOne(Access access, BigInteger id) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<PlatformMessage> readMany(Access access, Collection<BigInteger> parentIds) throws CoreException {
    throw new CoreException("Not allowed to read all Platform Messages (must specify # previous days).");

  }

  @Override
  public void update(Access access, BigInteger id, PlatformMessage entity) throws CoreException {
    throw new CoreException("Not allowed to update PlatformMessage record.");
  }

  @Override
  @Nullable
  public Collection<PlatformMessage> readAllPreviousDays(Access access, Integer previousDays) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllPreviousDays(tx.getContext(), access, previousDays));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void destroy(Access access, BigInteger id) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(tx.getContext(), access, ULong.valueOf(id));
      tx.success();
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public PlatformMessage newInstance() {
    return new PlatformMessage();
  }


}
