// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.app.access.impl.Access;
import io.xj.core.app.exception.BusinessException;
import io.xj.core.app.exception.ConfigException;
import io.xj.core.dao.LinkChordDAO;
import io.xj.core.db.sql.impl.SQLConnection;
import io.xj.core.db.sql.SQLDatabaseProvider;
import io.xj.core.model.link_chord.LinkChord;
import io.xj.core.tables.records.LinkChordRecord;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.List;
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

  @Override
  public LinkChordRecord create(Access access, LinkChord entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public LinkChordRecord readOne(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneRecord(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Result<LinkChordRecord> readAll(Access access, ULong linkId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, linkId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Result<LinkChordRecord> readAllInLinks(Access access, List<ULong> linkIds) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllInLinks(tx.getContext(), access, linkIds));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, ULong id, LinkChord entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, id, entity);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(access, tx.getContext(), id);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  /**
   Create a new Link Chord

   @param db     context
   @param access control
   @param entity for new link
   @return newly readMany record
   @throws BusinessException if failure
   */
  private LinkChordRecord createRecord(DSLContext db, Access access, LinkChord entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    requireTopLevel(access);

    requireExists("Link", db.select(LINK.ID).from(LINK)
      .where(LINK.ID.eq(entity.getLinkId()))
      .fetchOne());

    return executeCreate(db, LINK_CHORD, fieldValues);
  }

  /**
   Read one Chord if able

   @param db     context
   @param access control
   @param id     of link
   @return link
   */
  private LinkChordRecord readOneRecord(DSLContext db, Access access, ULong id) {
    if (access.isTopLevel())
      return db.selectFrom(LINK_CHORD)
        .where(LINK_CHORD.ID.eq(id))
        .fetchOne();
    else
      return recordInto(LINK_CHORD, db.select(LINK_CHORD.fields())
        .from(LINK_CHORD)
        .join(LINK).on(LINK.ID.eq(LINK_CHORD.LINK_ID))
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(LINK_CHORD.ID.eq(id))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
  }

  /**
   Read all Chord able for an Chain

   @param db     context
   @param access control
   @param linkId to readMany all link of
   @return array of links
   @throws SQLException on failure
   */
  private Result<LinkChordRecord> readAll(DSLContext db, Access access, ULong linkId) throws SQLException {
    if (access.isTopLevel())
      return resultInto(LINK_CHORD, db.select(LINK_CHORD.fields())
        .from(LINK_CHORD)
        .where(LINK_CHORD.LINK_ID.eq(linkId))
        .orderBy(LINK_CHORD.POSITION)
        .fetch());
    else
      return resultInto(LINK_CHORD, db.select(LINK_CHORD.fields())
        .from(LINK_CHORD)
        .join(LINK).on(LINK.ID.eq(LINK_CHORD.LINK_ID))
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(LINK_CHORD.LINK_ID.eq(linkId))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .orderBy(LINK_CHORD.POSITION)
        .fetch());
  }

  /**
   Read all records in parent records by ids
   order by position ascending

   @return array of records
    @param db     context
   @param access control
   @param linkIds id of parent's parent (the chain)
   */
  private Result<LinkChordRecord> readAllInLinks(DSLContext db, Access access, List<ULong> linkIds) throws Exception {
    if (access.isTopLevel())
      return resultInto(LINK_CHORD, db.select(LINK_CHORD.fields())
        .from(LINK_CHORD)
        .join(LINK).on(LINK.ID.eq(LINK_CHORD.LINK_ID))
        .where(LINK.ID.in(linkIds))
        .orderBy(LINK_CHORD.POSITION.desc())
        .fetch());
    else
      return resultInto(LINK_CHORD, db.select(LINK_CHORD.fields())
        .from(LINK_CHORD)
        .join(LINK).on(LINK.ID.eq(LINK_CHORD.LINK_ID))
        .join(CHAIN).on(CHAIN.ID.eq(LINK.CHAIN_ID))
        .where(LINK.ID.in(linkIds))
        .and(CHAIN.ACCOUNT_ID.in(access.getAccounts()))
        .orderBy(LINK_CHORD.POSITION.desc())
        .fetch());
  }

  /**
   Update a Chord record

   @param db     context
   @param access control
   @param id     to update
   @param entity to update with
   @throws BusinessException if failure
   */
  private void update(DSLContext db, Access access, ULong id, LinkChord entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();
    fieldValues.put(LINK_CHORD.ID, id);

    requireTopLevel(access);

    requireExists("existing LinkChord with immutable Link membership",
      db.select(LINK_CHORD.ID).from(LINK_CHORD)
        .where(LINK_CHORD.ID.eq(id))
        .and(LINK_CHORD.LINK_ID.eq(entity.getLinkId()))
        .fetchOne());

    if (executeUpdate(db, LINK_CHORD, fieldValues) == 0)
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
  private void delete(Access access, DSLContext db, ULong id) throws Exception {
    requireTopLevel(access);

    db.deleteFrom(LINK_CHORD)
      .where(LINK_CHORD.ID.eq(id))
      .execute();
  }

}
