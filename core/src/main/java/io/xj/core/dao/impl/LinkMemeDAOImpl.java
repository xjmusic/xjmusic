// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.LinkMemeDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.link_meme.LinkMeme;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;
import com.google.inject.Inject;

import java.math.BigInteger;
import java.util.Collection;
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
  public LinkMeme create(Access access, LinkMeme entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public LinkMeme readOne(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<LinkMeme> readAll(Access access, BigInteger linkId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, ULong.valueOf(linkId)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<LinkMeme> readAllInLinks(Access access, Collection<BigInteger> linkIds) throws Exception {
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
   Create a new Link Meme record

   @param db     context
   @param access control
   @param entity for new LinkMeme
   @return new record
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private static LinkMeme createRecord(DSLContext db, Access access, LinkMeme entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    if (access.isTopLevel())
      requireExists("Link", db.selectCount().from(LINK)
        .where(LINK.ID.eq(ULong.valueOf(entity.getLinkId())))
        .fetchOne());
    else
      requireExists("Link", db.selectCount().from(LINK)
        .join(CHAIN).on(LINK.CHAIN_ID.eq(CHAIN.ID))
        .where(LINK.ID.eq(ULong.valueOf(entity.getLinkId())))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne());

    requireNotExists("Link Meme", db.selectCount().from(LINK_MEME)
      .where(LINK_MEME.LINK_ID.eq(ULong.valueOf(entity.getLinkId())))
      .and(LINK_MEME.NAME.eq(entity.getName()))
      .fetchOne(0, int.class));

    return modelFrom(executeCreate(db, LINK_MEME, fieldValues), LinkMeme.class);
  }

  /**
   Read one Link Meme where able

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private static LinkMeme readOne(DSLContext db, Access access, ULong id) throws BusinessException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(LINK_MEME)
        .where(LINK_MEME.ID.eq(id))
        .fetchOne(), LinkMeme.class);
    else
      return modelFrom(db.select(LINK_MEME.fields()).from(LINK_MEME)
        .join(LINK).on(LINK.ID.eq(LINK_MEME.LINK_ID))
        .join(CHAIN).on(LINK.CHAIN_ID.eq(CHAIN.ID))
        .where(LINK_MEME.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), LinkMeme.class);
  }

  /**
   Read all Memes of an Link where able

   @param db     context
   @param access control
   @param linkId to readMany memes for
   @return array of link memes
   */
  private static Collection<LinkMeme> readAll(DSLContext db, Access access, ULong linkId) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.selectFrom(LINK_MEME)
        .where(LINK_MEME.LINK_ID.eq(linkId))
        .fetch(), LinkMeme.class);
    else
      return modelsFrom(db.select(LINK_MEME.fields()).from(LINK_MEME)
        .join(LINK).on(LINK.ID.eq(LINK_MEME.LINK_ID))
        .join(CHAIN).on(LINK.CHAIN_ID.eq(CHAIN.ID))
        .where(LINK.ID.eq(linkId))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .fetch(), LinkMeme.class);
  }

  /**
   Read all records in parent records by ids

   @param db      context
   @param access  control
   @param linkIds id of parent's parent (the chain)
   @return array of records
   */
  private static Collection<LinkMeme> readAllInLinks(DSLContext db, Access access, Collection<ULong> linkIds) throws Exception {
    requireAccessToLinks(db, access, linkIds);

    return modelsFrom(db.select(LINK_MEME.fields()).from(LINK_MEME)
      .join(LINK).on(LINK.ID.eq(LINK_MEME.LINK_ID))
      .where(LINK.ID.in(linkIds))
      .fetch(), LinkMeme.class);
  }

  /**
   Delete an LinkMeme record

   @param db     context
   @param access control
   @param id     to delete
   @throws BusinessException if failure
   */
  private static void delete(DSLContext db, Access access, ULong id) throws BusinessException {
    if (!access.isTopLevel())
      requireExists("Link Meme", db.selectCount().from(LINK_MEME)
        .join(LINK).on(LINK.ID.eq(LINK_MEME.LINK_ID))
        .join(CHAIN).on(LINK.CHAIN_ID.eq(CHAIN.ID))
        .where(LINK_MEME.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    db.deleteFrom(LINK_MEME)
      .where(LINK_MEME.ID.eq(id))
      .execute();
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(LinkMeme entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(LINK_MEME.LINK_ID, entity.getLinkId());
    fieldValues.put(LINK_MEME.NAME, entity.getName());
    return fieldValues;
  }


}
