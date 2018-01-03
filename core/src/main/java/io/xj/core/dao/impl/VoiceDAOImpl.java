// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.VoiceDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.voice.Voice;
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

import static io.xj.core.Tables.PICK;
import static io.xj.core.tables.Arrangement.ARRANGEMENT;
import static io.xj.core.tables.Library.LIBRARY;
import static io.xj.core.tables.Pattern.PATTERN;
import static io.xj.core.tables.Voice.VOICE;
import static io.xj.core.tables.VoiceEvent.VOICE_EVENT;

public class VoiceDAOImpl extends DAOImpl implements VoiceDAO {

  @Inject
  public VoiceDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public Voice create(Access access, Voice entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Voice readOne(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Collection<Voice> readAll(Access access, BigInteger patternId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, ULong.valueOf(patternId)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, Voice entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, ULong.valueOf(id), entity);
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void delete(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(access, tx.getContext(), ULong.valueOf(id));
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  /**
   Create a new Voice

   @param db     context
   @param access control
   @param entity for new voice
   @return newly readMany record
   @throws BusinessException if failure
   */
  private static Voice create(DSLContext db, Access access, Voice entity) throws BusinessException {
    entity.validate();
    requireRelationships(db, access, entity);

    return modelFrom(executeCreate(db, VOICE, fieldValueMap(entity)), Voice.class);
  }

  /**
   Read one Voice if able

   @param db     context
   @param access control
   @param id     of voice
   @return voice
   */
  private static Voice readOne(DSLContext db, Access access, ULong id) throws BusinessException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(VOICE)
        .where(VOICE.ID.eq(id))
        .fetchOne(), Voice.class);
    else
      return modelFrom(db.select(VOICE.fields())
        .from(VOICE)
        .join(PATTERN).on(PATTERN.ID.eq(VOICE.PATTERN_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(VOICE.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), Voice.class);
  }

  /**
   Read all Voice able for an Phase

   @param db        context
   @param access    control
   @param patternId to readMany all voice of
   @return array of voices
   */
  private static Collection<Voice> readAll(DSLContext db, Access access, ULong patternId) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.select(VOICE.fields())
        .from(VOICE)
        .where(VOICE.PATTERN_ID.eq(patternId))
        .fetch(), Voice.class);
    else
      return modelsFrom(db.select(VOICE.fields())
        .from(VOICE)
        .join(PATTERN).on(PATTERN.ID.eq(VOICE.PATTERN_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(VOICE.PATTERN_ID.eq(patternId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetch(), Voice.class);
  }

  /**
   Update a Voice record

   @param db     context
   @param access control
   @param id     to update
   @param entity to update with
   @throws BusinessException if failure
   */
  private static void update(DSLContext db, Access access, ULong id, Voice entity) throws Exception {
    entity.validate();
    requireRelationships(db, access, entity);

    Map<Field, Object> fieldValues = fieldValueMap(entity);
    fieldValues.put(VOICE.ID, id);
    if (0 == executeUpdate(db, VOICE, fieldValues))
      throw new BusinessException("No records updated.");
  }

  /**
   Delete an Voice

   @param db context
   @param id to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private static void delete(Access access, DSLContext db, ULong id) throws Exception {
    if (!access.isTopLevel())
      requireExists("Voice", db.selectCount().from(VOICE)
        .join(PATTERN).on(PATTERN.ID.eq(VOICE.PATTERN_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(VOICE.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    db.deleteFrom(VOICE_EVENT)
      .where(VOICE_EVENT.VOICE_ID.eq(id))
      .execute();

    db.deleteFrom(PICK)
      .where(PICK.ARRANGEMENT_ID.in(
        db.select(ARRANGEMENT.ID).from(ARRANGEMENT)
          .where(ARRANGEMENT.VOICE_ID.eq(id))))
      .execute();

    db.deleteFrom(ARRANGEMENT)
      .where(ARRANGEMENT.VOICE_ID.eq(id))
      .execute();

    db.deleteFrom(VOICE)
      .where(VOICE.ID.eq(id))
      .execute();
  }

  /**
   Require that related records exist

   @param db     context
   @param access control
   @param entity to validate
   @throws BusinessException if relationships do not exist
   */
  private static void requireRelationships(DSLContext db, Access access, Voice entity) throws BusinessException {
    if (access.isTopLevel())
      requireExists("Pattern", db.selectCount().from(PATTERN)
        .where(PATTERN.ID.eq(ULong.valueOf(entity.getPatternId())))
        .fetchOne(0, int.class));
    else
      requireExists("Pattern", db.selectCount().from(PATTERN)
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .and(PATTERN.ID.eq(ULong.valueOf(entity.getPatternId())))
        .fetchOne(0, int.class));
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(Voice entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(VOICE.PATTERN_ID, entity.getPatternId());
    fieldValues.put(VOICE.TYPE, entity.getType());
    fieldValues.put(VOICE.DESCRIPTION, entity.getDescription());
    return fieldValues;
  }

}
