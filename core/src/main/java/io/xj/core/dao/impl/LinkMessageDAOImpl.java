// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.app.access.impl.Access;
import io.xj.core.app.exception.BusinessException;
import io.xj.core.app.exception.ConfigException;
import io.xj.core.dao.LinkMessageDAO;
import io.xj.core.database.sql.impl.SQLConnection;
import io.xj.core.database.sql.SQLDatabaseProvider;
import io.xj.core.model.link_message.LinkMessage;
import io.xj.core.tables.records.LinkMessageRecord;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.util.List;
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
  public LinkMessageRecord create(Access access, LinkMessage entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public LinkMessageRecord readOne(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneRecord(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Result<LinkMessageRecord> readAllInLink(Access access, ULong linkId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInLink(tx.getContext(), access, linkId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Result<LinkMessageRecord> readAllInLinks(Access access, List<ULong> linkIds) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInLinks(tx.getContext(), access, linkIds));
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
  private LinkMessageRecord createRecord(DSLContext db, Access access, LinkMessage entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    requireTopLevel(access);
    requireExists("Link",
      db.select(LINK.ID).from(LINK)
        .where(LINK.ID.eq(entity.getLinkId()))
        .fetchOne());

    return executeCreate(db, LINK_MESSAGE, fieldValues);
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  @Nullable
  private LinkMessageRecord readOneRecord(DSLContext db, Access access, ULong id) throws Exception {
    if (access.isTopLevel())
      return db.selectFrom(LINK_MESSAGE)
        .where(LINK_MESSAGE.ID.eq(id))
        .fetchOne();
    else
      return recordInto(LINK_MESSAGE, db.select(LINK_MESSAGE.fields())
        .from(LINK_MESSAGE)
        .join(LINK).on(LINK.ID.eq(LINK_MESSAGE.LINK_ID))
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(LINK_MESSAGE.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
  }

  /**
   Read all records in parent record by id

   @param db     context
   @param access control
   @param linkId of parent
   @return array of records
   */
  private Result<LinkMessageRecord> readAllInLink(DSLContext db, Access access, ULong linkId) throws Exception {
    if (access.isTopLevel())
      return resultInto(LINK_MESSAGE, db.select(LINK_MESSAGE.fields())
        .from(LINK_MESSAGE)
        .where(LINK_MESSAGE.LINK_ID.eq(linkId))
        .orderBy(LINK_MESSAGE.TYPE)
        .fetch());
    else
      return resultInto(LINK_MESSAGE, db.select(LINK_MESSAGE.fields())
        .from(LINK_MESSAGE)
        .join(LINK).on(LINK.ID.eq(LINK_MESSAGE.LINK_ID))
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(LINK_MESSAGE.LINK_ID.eq(linkId))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .orderBy(LINK_MESSAGE.TYPE)
        .fetch());
  }

  /**
   Read all records in parent record's parent record by id

   @return array of records
    @param db     context
   @param access control
   @param linkIds id of parent's parent (the chain)
   */
  private Result<LinkMessageRecord> readAllInLinks(DSLContext db, Access access, List<ULong> linkIds) throws Exception {
    if (access.isTopLevel())
      return resultInto(LINK_MESSAGE, db.select(LINK_MESSAGE.fields())
        .from(LINK_MESSAGE)
        .join(LINK).on(LINK.ID.eq(LINK_MESSAGE.LINK_ID))
        .where(LINK.ID.in(linkIds))
        .orderBy(LINK_MESSAGE.TYPE)
        .fetch());
    else
      return resultInto(LINK_MESSAGE, db.select(LINK_MESSAGE.fields())
        .from(LINK_MESSAGE)
        .join(LINK).on(LINK.ID.eq(LINK_MESSAGE.LINK_ID))
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(LINK.ID.in(linkIds))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .orderBy(LINK_MESSAGE.TYPE)
        .fetch());
  }

  /**
   Delete an LinkMessage

   @param db context
   @param id to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private void delete(DSLContext db, Access access, ULong id) throws Exception {
    requireTopLevel(access);

    requireExists("LinkMessage", db.select(LINK_MESSAGE.ID).from(LINK_MESSAGE)
      .where(LINK_MESSAGE.ID.eq(id))
      .fetchOne());

    db.deleteFrom(LINK_MESSAGE)
      .where(LINK_MESSAGE.ID.eq(id))
      .execute();
  }

}
