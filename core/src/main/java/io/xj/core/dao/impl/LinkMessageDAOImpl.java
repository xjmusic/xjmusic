// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.LinkMessageDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.link_message.LinkMessage;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

import static io.xj.core.Tables.LINK;
import static io.xj.core.Tables.LINK_MESSAGE;
import static io.xj.core.tables.Chain.CHAIN;

public class LinkMessageDAOImpl extends DAOImpl implements LinkMessageDAO {

  @Inject
  public LinkMessageDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public LinkMessage create(Access access, LinkMessage entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public LinkMessage readOne(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Collection<LinkMessage> readAllInLink(Access access, BigInteger linkId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInLink(tx.getContext(), access, ULong.valueOf(linkId)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<LinkMessage> readAllInLinks(Access access, Collection<BigInteger> linkIds) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInLinks(tx.getContext(), access, idCollection(linkIds)));
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
  private static LinkMessage create(DSLContext db, Access access, LinkMessage entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    requireTopLevel(access);
    requireExists("Link",
      db.selectCount().from(LINK)
        .where(LINK.ID.eq(ULong.valueOf(entity.getLinkId())))
        .fetchOne(0, int.class));

    return modelFrom(executeCreate(db, LINK_MESSAGE, fieldValues), LinkMessage.class);
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  @Nullable
  private static LinkMessage readOne(DSLContext db, Access access, ULong id) throws Exception {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(LINK_MESSAGE)
        .where(LINK_MESSAGE.ID.eq(id))
        .fetchOne(), LinkMessage.class);
    else
      return modelFrom(db.select(LINK_MESSAGE.fields())
        .from(LINK_MESSAGE)
        .join(LINK).on(LINK.ID.eq(LINK_MESSAGE.LINK_ID))
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(LINK_MESSAGE.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), LinkMessage.class);
  }

  /**
   Read all records in parent record by id

   @param db     context
   @param access control
   @param linkId of parent
   @return array of records
   */
  private static Collection<LinkMessage> readAllInLink(DSLContext db, Access access, ULong linkId) throws Exception {
    requireAccessToLinks(db, access, ImmutableList.of(linkId));

    return modelsFrom(db.select(LINK_MESSAGE.fields())
      .from(LINK_MESSAGE)
      .where(LINK_MESSAGE.LINK_ID.eq(linkId))
      .orderBy(LINK_MESSAGE.TYPE)
      .fetch(), LinkMessage.class);
  }

  /**
   Read all records in parent record's parent record by id

   @param db      context
   @param access  control
   @param linkIds id of parent's parent (the chain)
   @return array of records
   */
  private static Collection<LinkMessage> readAllInLinks(DSLContext db, Access access, Collection<ULong> linkIds) throws Exception {
    requireAccessToLinks(db, access, linkIds);

    return modelsFrom(db.select(LINK_MESSAGE.fields())
      .from(LINK_MESSAGE)
      .join(LINK).on(LINK.ID.eq(LINK_MESSAGE.LINK_ID))
      .where(LINK.ID.in(linkIds))
      .orderBy(LINK_MESSAGE.TYPE)
      .fetch(), LinkMessage.class);
  }

  /**
   Delete an LinkMessage

   @param db context
   @param id to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private static void delete(DSLContext db, Access access, ULong id) throws Exception {
    requireTopLevel(access);

    requireExists("LinkMessage", db.selectCount().from(LINK_MESSAGE)
      .where(LINK_MESSAGE.ID.eq(id))
      .fetchOne(0, int.class));

    db.deleteFrom(LINK_MESSAGE)
      .where(LINK_MESSAGE.ID.eq(id))
      .execute();
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(LinkMessage entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(LINK_MESSAGE.LINK_ID, ULong.valueOf(entity.getLinkId()));
    fieldValues.put(LINK_MESSAGE.BODY, entity.getBody());
    fieldValues.put(LINK_MESSAGE.TYPE, entity.getType());
    return fieldValues;
  }


}
