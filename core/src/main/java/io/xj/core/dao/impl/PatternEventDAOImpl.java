// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.PatternEventDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.pattern_event.PatternEvent;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;
import io.xj.core.tables.Voice;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;
import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

import static io.xj.core.Tables.VOICE;
import static io.xj.core.Tables.PATTERN_EVENT;
import static io.xj.core.tables.Library.LIBRARY;
import static io.xj.core.tables.Sequence.SEQUENCE;
import static io.xj.core.tables.Pattern.PATTERN;

public class PatternEventDAOImpl extends DAOImpl implements PatternEventDAO {

  @Inject
  public PatternEventDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  /**
   Create a new Voice Event

   @param db     context
   @param access control
   @param entity for new voice
   @return newly readMany record
   @throws BusinessException if failure
   */
  private static PatternEvent create(DSLContext db, Access access, PatternEvent entity) throws BusinessException {
    entity.validate();
    requireRelationships(db, access, entity);

    Map<Field, Object> fieldValues = fieldValueMap(entity);
    return modelFrom(executeCreate(db, PATTERN_EVENT, fieldValues), PatternEvent.class);
  }

  /**
   Read one Event if able

   @param db     context
   @param access control
   @param id     of voice
   @return voice
   */
  private static PatternEvent readOne(DSLContext db, Access access, ULong id) throws BusinessException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(PATTERN_EVENT)
        .where(PATTERN_EVENT.ID.eq(id))
        .fetchOne(), PatternEvent.class);
    else
      return modelFrom(db.select(PATTERN_EVENT.fields())
        .from(PATTERN_EVENT)
        .join(VOICE).on(VOICE.ID.eq(PATTERN_EVENT.VOICE_ID))
        .join(SEQUENCE).on(SEQUENCE.ID.eq(VOICE.SEQUENCE_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(SEQUENCE.LIBRARY_ID))
        .where(PATTERN_EVENT.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), PatternEvent.class);
  }

  /**
   Read all PatternEvent for a Pattern

   @return array of voices
    @param db      context
   @param access  control
   @param patternIds to readMany all voice of
   */
  private static Collection<PatternEvent> readAll(DSLContext db, Access access, Collection<ULong> patternIds) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.select(PATTERN_EVENT.fields())
        .from(PATTERN_EVENT)
        .where(PATTERN_EVENT.PATTERN_ID.in(patternIds))
        .orderBy(PATTERN_EVENT.POSITION)
        .fetch(), PatternEvent.class);
    else
      return modelsFrom(db.select(PATTERN_EVENT.fields())
        .from(PATTERN_EVENT)
        .join(VOICE).on(VOICE.ID.eq(PATTERN_EVENT.VOICE_ID))
        .join(SEQUENCE).on(SEQUENCE.ID.eq(VOICE.SEQUENCE_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(SEQUENCE.LIBRARY_ID))
        .where(PATTERN_EVENT.PATTERN_ID.in(patternIds))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(PATTERN_EVENT.POSITION)
        .fetch(), PatternEvent.class);
  }

  /**
   Read all PatternEvent for a Voice

   @param db      context
   @param access  control
   @param voiceId to readMany all voice of
   @return array of voices
   */
  private static Collection<PatternEvent> readAllOfVoice(DSLContext db, Access access, ULong voiceId) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.select(PATTERN_EVENT.fields())
        .from(PATTERN_EVENT)
        .where(PATTERN_EVENT.VOICE_ID.eq(voiceId))
        .orderBy(PATTERN_EVENT.POSITION)
        .fetch(), PatternEvent.class);
    else
      return modelsFrom(db.select(PATTERN_EVENT.fields())
        .from(PATTERN_EVENT)
        .join(VOICE).on(VOICE.ID.eq(PATTERN_EVENT.VOICE_ID))
        .join(SEQUENCE).on(SEQUENCE.ID.eq(VOICE.SEQUENCE_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(SEQUENCE.LIBRARY_ID))
        .where(PATTERN_EVENT.VOICE_ID.eq(voiceId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(PATTERN_EVENT.POSITION)
        .fetch(), PatternEvent.class);
  }

  /**
   Update a Event record

   @param db     context
   @param access control
   @param id     to update
   @param entity to update with
   @throws BusinessException if failure
   */
  private static void update(DSLContext db, Access access, ULong id, PatternEvent entity) throws Exception {
    entity.validate();
    requireRelationships(db, access, entity);

    Map<Field, Object> fieldValues = fieldValueMap(entity);
    fieldValues.put(PATTERN_EVENT.ID, id);
    if (0 == executeUpdate(db, PATTERN_EVENT, fieldValues))
      throw new BusinessException("No records updated.");
  }

  /**
   Delete an Event

   @param db context
   @param id to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private static void delete(Access access, DSLContext db, ULong id) throws Exception {
    if (!access.isTopLevel())
      requireExists("Voice Meme",
        db.selectCount().from(PATTERN_EVENT)
          .join(Voice.VOICE).on(Voice.VOICE.ID.eq(PATTERN_EVENT.VOICE_ID))
          .join(SEQUENCE).on(SEQUENCE.ID.eq(VOICE.SEQUENCE_ID))
          .join(LIBRARY).on(SEQUENCE.LIBRARY_ID.eq(LIBRARY.ID))
          .where(PATTERN_EVENT.ID.eq(id))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne(0, int.class));

    db.deleteFrom(PATTERN_EVENT)
      .where(PATTERN_EVENT.ID.eq(id))
      .execute();
  }

  /**
   Require relationships exist

   @param db     context
   @param access control
   @param entity to validate
   @throws BusinessException if not exist
   */
  private static void requireRelationships(DSLContext db, Access access, PatternEvent entity) throws BusinessException {
    if (access.isTopLevel())
      requireExists("Voice", db.selectCount().from(VOICE)
        .where(VOICE.ID.eq(ULong.valueOf(entity.getVoiceId())))
        .fetchOne(0, int.class));
    else
      requireExists("Voice", db.selectCount().from(VOICE)
        .join(SEQUENCE).on(SEQUENCE.ID.eq(VOICE.SEQUENCE_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(SEQUENCE.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .and(VOICE.ID.eq(ULong.valueOf(entity.getVoiceId())))
        .fetchOne(0, int.class));

    if (access.isTopLevel())
      requireExists("Pattern", db.selectCount().from(PATTERN)
        .where(PATTERN.ID.eq(ULong.valueOf(entity.getPatternId())))
        .fetchOne(0, int.class));
    else
      requireExists("Pattern", db.selectCount().from(PATTERN)
        .join(SEQUENCE).on(SEQUENCE.ID.eq(PATTERN.SEQUENCE_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(SEQUENCE.LIBRARY_ID))
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
  private static Map<Field, Object> fieldValueMap(PatternEvent entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(PATTERN_EVENT.DURATION, entity.getDuration());
    fieldValues.put(PATTERN_EVENT.INFLECTION, entity.getInflection());
    fieldValues.put(PATTERN_EVENT.NOTE, entity.getNote());
    fieldValues.put(PATTERN_EVENT.POSITION, entity.getPosition());
    fieldValues.put(PATTERN_EVENT.TONALITY, entity.getTonality());
    fieldValues.put(PATTERN_EVENT.VELOCITY, entity.getVelocity());
    fieldValues.put(PATTERN_EVENT.VOICE_ID, entity.getVoiceId());
    fieldValues.put(PATTERN_EVENT.PATTERN_ID, entity.getPatternId());
    return fieldValues;
  }

  @Override
  public PatternEvent create(Access access, PatternEvent entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public PatternEvent readOne(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<PatternEvent> readAll(Access access, Collection<BigInteger> parentIds) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, uLongValuesOf(parentIds)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<PatternEvent> readAllOfVoice(Access access, BigInteger voiceId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllOfVoice(tx.getContext(), access, ULong.valueOf(voiceId)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, PatternEvent entity) throws Exception {
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
