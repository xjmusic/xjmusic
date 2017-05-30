// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.dao.LinkMemeDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.link_meme.LinkMeme;
import io.outright.xj.core.tables.records.LinkMemeRecord;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static io.outright.xj.core.tables.Chain.CHAIN;
import static io.outright.xj.core.tables.Link.LINK;
import static io.outright.xj.core.tables.LinkMeme.LINK_MEME;

/**
 LinkMeme DAO
 <p>
 TODO [core] more specific permissions of user (artist) access by per-entity ownership
 */
public class LinkMemeDAOImpl extends DAOImpl implements LinkMemeDAO {

  @Inject
  public LinkMemeDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public LinkMemeRecord create(Access access, LinkMeme entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public LinkMemeRecord readOne(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneRecord(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Result<LinkMemeRecord> readAll(Access access, ULong linkId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, linkId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Result<LinkMemeRecord> readAllInLinks(Access access, List<ULong> linkIds) throws Exception {
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
   Create a new Link Meme record

   @param db     context
   @param access control
   @param entity for new LinkMeme
   @return new record
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private LinkMemeRecord createRecord(DSLContext db, Access access, LinkMeme entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    if (access.isTopLevel())
      requireExists("Link", db.select(LINK.ID).from(LINK)
        .where(LINK.ID.eq(entity.getLinkId()))
        .fetchOne());
    else
      requireExists("Link", db.select(LINK.ID).from(LINK)
        .join(CHAIN).on(LINK.CHAIN_ID.eq(CHAIN.ID))
        .where(LINK.ID.eq(entity.getLinkId()))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());

    if (db.selectFrom(LINK_MEME)
      .where(LINK_MEME.LINK_ID.eq(entity.getLinkId()))
      .and(LINK_MEME.NAME.eq(entity.getName()))
      .fetchOne() != null)
      throw new BusinessException("Link Meme already exists!");

    return executeCreate(db, LINK_MEME, fieldValues);
  }

  /**
   Read one Link Meme where able

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private LinkMemeRecord readOneRecord(DSLContext db, Access access, ULong id) throws SQLException {
    if (access.isTopLevel())
      return db.selectFrom(LINK_MEME)
        .where(LINK_MEME.ID.eq(id))
        .fetchOne();
    else
      return recordInto(LINK_MEME, db.select(LINK_MEME.fields()).from(LINK_MEME)
        .join(LINK).on(LINK.ID.eq(LINK_MEME.LINK_ID))
        .join(CHAIN).on(LINK.CHAIN_ID.eq(CHAIN.ID))
        .where(LINK_MEME.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
  }

  /**
   Read all Memes of an Link where able

   @param db     context
   @param access control
   @param linkId to readMany memes for
   @return array of link memes
   @throws SQLException if failure
   */
  private Result<LinkMemeRecord> readAll(DSLContext db, Access access, ULong linkId) throws SQLException {
    if (access.isTopLevel())
      return db.selectFrom(LINK_MEME)
        .where(LINK_MEME.LINK_ID.eq(linkId))
        .fetch();
    else
      return resultInto(LINK_MEME, db.select(LINK_MEME.fields()).from(LINK_MEME)
        .join(LINK).on(LINK.ID.eq(LINK_MEME.LINK_ID))
        .join(CHAIN).on(LINK.CHAIN_ID.eq(CHAIN.ID))
        .where(LINK.ID.eq(linkId))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetch());
  }

  /**
   Read all records in parent records by ids

   @return array of records
    @param db     context
   @param access control
   @param linkIds id of parent's parent (the chain)
   */
  private Result<LinkMemeRecord> readAllInLinks(DSLContext db, Access access, List<ULong> linkIds) throws Exception {
    if (access.isTopLevel())
      return resultInto(LINK_MEME, db.select(LINK_MEME.fields())
        .from(LINK_MEME)
        .join(LINK).on(LINK.ID.eq(LINK_MEME.LINK_ID))
        .where(LINK.ID.in(linkIds))
        .fetch());
    else
      return resultInto(LINK_MEME, db.select(LINK_MEME.fields())
        .from(LINK_MEME)
        .join(LINK).on(LINK.ID.eq(LINK_MEME.LINK_ID))
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(LINK.ID.in(linkIds))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetch());
  }

  /**
   Delete an LinkMeme record

   @param db     context
   @param access control
   @param id     to delete
   @throws BusinessException if failure
   */
  // TODO: fail if no linkMeme is deleted
  private void delete(DSLContext db, Access access, ULong id) throws BusinessException {
    if (!access.isTopLevel())
      requireExists("Link Meme", db.select(LINK_MEME.ID).from(LINK_MEME)
        .join(LINK).on(LINK.ID.eq(LINK_MEME.LINK_ID))
        .join(CHAIN).on(LINK.CHAIN_ID.eq(CHAIN.ID))
        .where(LINK_MEME.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());

    db.deleteFrom(LINK_MEME)
      .where(LINK_MEME.ID.eq(id))
      .execute();
  }

}
