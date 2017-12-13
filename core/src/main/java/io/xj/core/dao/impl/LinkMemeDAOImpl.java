// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.LinkMemeDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.link_meme.LinkMeme;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;
import io.xj.core.tables.records.LinkMemeRecord;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static io.xj.core.tables.Chain.CHAIN;
import static io.xj.core.tables.Link.LINK;
import static io.xj.core.tables.LinkMeme.LINK_MEME;

/**
 LinkMeme DAO
 <p>
 future: more specific permissions of user (artist) access by per-entity ownership
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
  public Collection<LinkMeme> readAll(Access access, ULong linkId) throws Exception {
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
      requireExists("Link", db.selectCount().from(LINK)
        .where(LINK.ID.eq(entity.getLinkId()))
        .fetchOne());
    else
      requireExists("Link", db.selectCount().from(LINK)
        .join(CHAIN).on(LINK.CHAIN_ID.eq(CHAIN.ID))
        .where(LINK.ID.eq(entity.getLinkId()))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());

    requireNotExists("Link Meme", db.selectCount().from(LINK_MEME)
      .where(LINK_MEME.LINK_ID.eq(entity.getLinkId()))
      .and(LINK_MEME.NAME.eq(entity.getName()))
      .fetchOne(0, int.class));

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
  private Collection<LinkMeme> readAll(DSLContext db, Access access, ULong linkId) throws SQLException {
    Collection<LinkMeme> result = Lists.newArrayList();

    if (access.isTopLevel())
      db.selectFrom(LINK_MEME)
        .where(LINK_MEME.LINK_ID.eq(linkId))
        .fetch().forEach((record) -> {
        result.add(new LinkMeme().setFromRecord(record));
      });
    else
      resultInto(LINK_MEME, db.select(LINK_MEME.fields()).from(LINK_MEME)
        .join(LINK).on(LINK.ID.eq(LINK_MEME.LINK_ID))
        .join(CHAIN).on(LINK.CHAIN_ID.eq(CHAIN.ID))
        .where(LINK.ID.eq(linkId))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetch()).forEach((record) -> {
        result.add(new LinkMeme().setFromRecord(record));
      });

    return result;
  }

  /**
   Read all records in parent records by ids

   @param db      context
   @param access  control
   @param linkIds id of parent's parent (the chain)
   @return array of records
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
  private void delete(DSLContext db, Access access, ULong id) throws BusinessException {
    if (!access.isTopLevel())
      requireExists("Link Meme", db.selectCount().from(LINK_MEME)
        .join(LINK).on(LINK.ID.eq(LINK_MEME.LINK_ID))
        .join(CHAIN).on(LINK.CHAIN_ID.eq(CHAIN.ID))
        .where(LINK_MEME.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne(0, int.class));

    db.deleteFrom(LINK_MEME)
      .where(LINK_MEME.ID.eq(id))
      .execute();
  }

}
