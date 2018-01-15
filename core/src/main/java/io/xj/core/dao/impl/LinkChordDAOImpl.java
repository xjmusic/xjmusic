// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.LinkChordDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.link_chord.LinkChord;
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

import static io.xj.core.Tables.LINK;
import static io.xj.core.Tables.LINK_CHORD;
import static io.xj.core.tables.Chain.CHAIN;

public class LinkChordDAOImpl extends DAOImpl implements LinkChordDAO {

  @Inject
  public LinkChordDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  /**
   Create a new Link Chord

   @param db     context
   @param access control
   @param entity for new link
   @return newly readMany record
   @throws BusinessException if failure
   */
  private static LinkChord createRecord(DSLContext db, Access access, LinkChord entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    requireTopLevel(access);

    requireExists("Link", db.selectCount().from(LINK)
      .where(LINK.ID.eq(ULong.valueOf(entity.getLinkId())))
      .fetchOne(0, int.class));

    return modelFrom(executeCreate(db, LINK_CHORD, fieldValues), LinkChord.class);
  }

  /**
   Read one Chord if able

   @param db     context
   @param access control
   @param id     of link
   @return link
   */
  private static LinkChord readOne(DSLContext db, Access access, ULong id) throws BusinessException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(LINK_CHORD)
        .where(LINK_CHORD.ID.eq(id))
        .fetchOne(), LinkChord.class);
    else
      return modelFrom(db.select(LINK_CHORD.fields())
        .from(LINK_CHORD)
        .join(LINK).on(LINK.ID.eq(LINK_CHORD.LINK_ID))
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(LINK_CHORD.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), LinkChord.class);
  }

  /**
   Read all Chord able for an Chain

   @param db     context
   @param access control
   @param linkId to readMany all link of
   @return array of links
   */
  private static Collection<LinkChord> readAll(DSLContext db, Access access, Collection<ULong> linkId) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.select(LINK_CHORD.fields()).from(LINK_CHORD)
        .where(LINK_CHORD.LINK_ID.in(linkId))
        .orderBy(LINK_CHORD.POSITION)
        .fetch(), LinkChord.class);
    else
      return modelsFrom(db.select(LINK_CHORD.fields()).from(LINK_CHORD)
        .join(LINK).on(LINK.ID.eq(LINK_CHORD.LINK_ID))
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(LINK_CHORD.LINK_ID.in(linkId))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(LINK_CHORD.POSITION)
        .fetch(), LinkChord.class);
  }

  /**
   Read all records in parent records by ids
   order by position ascending

   @param db      context
   @param access  control
   @param linkIds id of parent's parent (the chain)
   @return array of records
   */
  private static Collection<LinkChord> readAllInLinks(DSLContext db, Access access, Collection<ULong> linkIds) throws Exception {
    requireAccessToLinks(db, access, linkIds);

    return modelsFrom(db.select(LINK_CHORD.fields()).from(LINK_CHORD)
      .join(LINK).on(LINK.ID.eq(LINK_CHORD.LINK_ID))
      .where(LINK.ID.in(linkIds))
      .orderBy(LINK_CHORD.POSITION.desc())
      .fetch(), LinkChord.class);
  }

  /**
   Update a Chord record

   @param db     context
   @param access control
   @param id     to update
   @param entity to update with
   @throws BusinessException if failure
   */
  private static void update(DSLContext db, Access access, ULong id, LinkChord entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);
    fieldValues.put(LINK_CHORD.ID, id);

    requireTopLevel(access);

    requireExists("existing LinkChord with immutable Link membership",
      db.selectCount().from(LINK_CHORD)
        .where(LINK_CHORD.ID.eq(id))
        .and(LINK_CHORD.LINK_ID.eq(ULong.valueOf(entity.getLinkId())))
        .fetchOne(0, int.class));

    if (0 == executeUpdate(db, LINK_CHORD, fieldValues))
      throw new BusinessException("No records updated.");
  }

  /**
   Delete an Chord

   @param db context
   @param id to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private static void delete(Access access, DSLContext db, ULong id) throws Exception {
    requireTopLevel(access);

    db.deleteFrom(LINK_CHORD)
      .where(LINK_CHORD.ID.eq(id))
      .execute();
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(LinkChord entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(LINK_CHORD.NAME, entity.getName());
    fieldValues.put(LINK_CHORD.LINK_ID, ULong.valueOf(entity.getLinkId()));
    fieldValues.put(LINK_CHORD.POSITION, entity.getPosition());
    return fieldValues;
  }

  @Override
  public LinkChord create(Access access, LinkChord entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public LinkChord readOne(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Collection<LinkChord> readAll(Access access, Collection<BigInteger> parentIds) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, uLongValuesOf(parentIds)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<LinkChord> readAllInLinks(Access access, Collection<BigInteger> linkIds) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInLinks(tx.getContext(), access, idCollection(linkIds)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, LinkChord entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, ULong.valueOf(id), entity);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void destroy(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(access, tx.getContext(), ULong.valueOf(id));
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

}
