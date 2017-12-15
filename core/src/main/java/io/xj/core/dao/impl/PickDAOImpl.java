// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.PickDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.pick.Pick;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;
import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;
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
  public Pick create(Access access, Pick entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Pick readOne(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Collection<Pick> readAll(Access access, BigInteger arrangementId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, ULong.valueOf(arrangementId)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<Pick> readAllInLink(Access access, BigInteger linkId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInLink(tx.getContext(), access, ULong.valueOf(linkId)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger pickId, Pick entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, ULong.valueOf(pickId), entity);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(Access access, BigInteger pickId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(tx.getContext(), access, ULong.valueOf(pickId));
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
  private static Pick create(DSLContext db, Access access, Pick entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    requireTopLevel(access);

    requireExists("Arrangement", db.selectCount().from(ARRANGEMENT)
      .where(ARRANGEMENT.ID.eq(ULong.valueOf(entity.getArrangementId())))
      .fetchOne(0, int.class));

    return modelFrom(executeCreate(db, PICK, fieldValues), Pick.class);
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private static Pick readOne(DSLContext db, Access access, ULong id) throws BusinessException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(PICK)
        .where(PICK.ID.eq(id))
        .fetchOne(), Pick.class);
    else
      return modelFrom(db.select(PICK.fields())
        .from(PICK)
        .join(ARRANGEMENT).on(ARRANGEMENT.ID.eq(PICK.ARRANGEMENT_ID))
        .join(CHOICE).on(CHOICE.ID.eq(ARRANGEMENT.CHOICE_ID))
        .join(LINK).on(LINK.ID.eq(CHOICE.LINK_ID))
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(PICK.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), Pick.class);
  }

  /**
   Read all records in parent by id

   @param db            context
   @param access        control
   @param arrangementId of parent
   @return array of records
   */
  private static Collection<Pick> readAll(DSLContext db, Access access, ULong arrangementId) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.select(PICK.fields())
        .from(PICK)
        .where(PICK.ARRANGEMENT_ID.eq(arrangementId))
        .fetch(), Pick.class);
    else
      return modelsFrom(db.select(PICK.fields())
        .from(PICK)
        .join(ARRANGEMENT).on(ARRANGEMENT.ID.eq(PICK.ARRANGEMENT_ID))
        .join(CHOICE).on(CHOICE.ID.eq(ARRANGEMENT.CHOICE_ID))
        .join(LINK).on(LINK.ID.eq(CHOICE.LINK_ID))
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(PICK.ARRANGEMENT_ID.eq(arrangementId))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .fetch(), Pick.class);
  }

  /**
   Read all records in parent's parent's parent (link) by id

   @param db     context
   @param access control
   @param linkId of parent
   @return array of records
   */
  private static Collection<Pick> readAllInLink(DSLContext db, Access access, ULong linkId) throws BusinessException {
    requireTopLevel(access);
    return modelsFrom(db.select(PICK.fields())
      .from(PICK)
      .join(ARRANGEMENT).on(ARRANGEMENT.ID.eq(PICK.ARRANGEMENT_ID))
      .join(CHOICE).on(CHOICE.ID.eq(ARRANGEMENT.CHOICE_ID))
      .where(CHOICE.LINK_ID.eq(linkId))
      .fetch(), Pick.class);
  }

  /**
   Update a record

   @param db     context
   @param access control
   @param id     of record
   @param entity to update with
   @throws BusinessException if a Business Rule is violated
   */
  private static void update(DSLContext db, Access access, ULong id, Pick entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);
    fieldValues.put(PICK.ID, id);

    requireTopLevel(access);

    requireExists("existing Pick with immutable Arrangement membership",
      db.selectCount().from(PICK)
        .where(PICK.ID.eq(id))
        .and(PICK.ARRANGEMENT_ID.eq(ULong.valueOf(entity.getArrangementId())))
        .fetchOne(0, int.class));

    if (0 == executeUpdate(db, PICK, fieldValues))
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
  private static void delete(DSLContext db, Access access, ULong id) throws Exception {
    requireTopLevel(access);

    requireExists("Pick", db.selectCount().from(PICK)
      .where(PICK.ID.eq(id))
      .fetchOne(0, int.class));

    db.deleteFrom(PICK)
      .where(PICK.ID.eq(id))
      .execute();
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(Pick entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(PICK.ARRANGEMENT_ID, ULong.valueOf(entity.getArrangementId()));
    fieldValues.put(PICK.AUDIO_ID, ULong.valueOf(entity.getAudioId()));
    fieldValues.put(PICK.START, entity.getStart());
    fieldValues.put(PICK.LENGTH, entity.getLength());
    fieldValues.put(PICK.AMPLITUDE, entity.getAmplitude());
    fieldValues.put(PICK.PITCH, entity.getPitch());
    return fieldValues;
  }


}
