// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.PhaseEventDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.phase_event.PhaseEvent;
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
import static io.xj.core.Tables.PHASE_EVENT;
import static io.xj.core.tables.Library.LIBRARY;
import static io.xj.core.tables.Pattern.PATTERN;
import static io.xj.core.tables.Phase.PHASE;

public class PhaseEventDAOImpl extends DAOImpl implements PhaseEventDAO {

  @Inject
  public PhaseEventDAOImpl(
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
  private static PhaseEvent create(DSLContext db, Access access, PhaseEvent entity) throws BusinessException {
    entity.validate();
    requireRelationships(db, access, entity);

    Map<Field, Object> fieldValues = fieldValueMap(entity);
    return modelFrom(executeCreate(db, PHASE_EVENT, fieldValues), PhaseEvent.class);
  }

  /**
   Read one Event if able

   @param db     context
   @param access control
   @param id     of voice
   @return voice
   */
  private static PhaseEvent readOne(DSLContext db, Access access, ULong id) throws BusinessException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(PHASE_EVENT)
        .where(PHASE_EVENT.ID.eq(id))
        .fetchOne(), PhaseEvent.class);
    else
      return modelFrom(db.select(PHASE_EVENT.fields())
        .from(PHASE_EVENT)
        .join(VOICE).on(VOICE.ID.eq(PHASE_EVENT.VOICE_ID))
        .join(PATTERN).on(PATTERN.ID.eq(VOICE.PATTERN_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(PHASE_EVENT.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), PhaseEvent.class);
  }

  /**
   Read all PhaseEvent for a Phase

   @return array of voices
    @param db      context
   @param access  control
   @param phaseIds to readMany all voice of
   */
  private static Collection<PhaseEvent> readAll(DSLContext db, Access access, Collection<ULong> phaseIds) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.select(PHASE_EVENT.fields())
        .from(PHASE_EVENT)
        .where(PHASE_EVENT.PHASE_ID.in(phaseIds))
        .orderBy(PHASE_EVENT.POSITION)
        .fetch(), PhaseEvent.class);
    else
      return modelsFrom(db.select(PHASE_EVENT.fields())
        .from(PHASE_EVENT)
        .join(VOICE).on(VOICE.ID.eq(PHASE_EVENT.VOICE_ID))
        .join(PATTERN).on(PATTERN.ID.eq(VOICE.PATTERN_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(PHASE_EVENT.PHASE_ID.in(phaseIds))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(PHASE_EVENT.POSITION)
        .fetch(), PhaseEvent.class);
  }

  /**
   Read all PhaseEvent for a Voice

   @param db      context
   @param access  control
   @param voiceId to readMany all voice of
   @return array of voices
   */
  private static Collection<PhaseEvent> readAllOfVoice(DSLContext db, Access access, ULong voiceId) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.select(PHASE_EVENT.fields())
        .from(PHASE_EVENT)
        .where(PHASE_EVENT.VOICE_ID.eq(voiceId))
        .orderBy(PHASE_EVENT.POSITION)
        .fetch(), PhaseEvent.class);
    else
      return modelsFrom(db.select(PHASE_EVENT.fields())
        .from(PHASE_EVENT)
        .join(VOICE).on(VOICE.ID.eq(PHASE_EVENT.VOICE_ID))
        .join(PATTERN).on(PATTERN.ID.eq(VOICE.PATTERN_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(PHASE_EVENT.VOICE_ID.eq(voiceId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .orderBy(PHASE_EVENT.POSITION)
        .fetch(), PhaseEvent.class);
  }

  /**
   Update a Event record

   @param db     context
   @param access control
   @param id     to update
   @param entity to update with
   @throws BusinessException if failure
   */
  private static void update(DSLContext db, Access access, ULong id, PhaseEvent entity) throws Exception {
    entity.validate();
    requireRelationships(db, access, entity);

    Map<Field, Object> fieldValues = fieldValueMap(entity);
    fieldValues.put(PHASE_EVENT.ID, id);
    if (0 == executeUpdate(db, PHASE_EVENT, fieldValues))
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
        db.selectCount().from(PHASE_EVENT)
          .join(Voice.VOICE).on(Voice.VOICE.ID.eq(PHASE_EVENT.VOICE_ID))
          .join(PATTERN).on(PATTERN.ID.eq(VOICE.PATTERN_ID))
          .join(LIBRARY).on(PATTERN.LIBRARY_ID.eq(LIBRARY.ID))
          .where(PHASE_EVENT.ID.eq(id))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne(0, int.class));

    db.deleteFrom(PHASE_EVENT)
      .where(PHASE_EVENT.ID.eq(id))
      .execute();
  }

  /**
   Require relationships exist

   @param db     context
   @param access control
   @param entity to validate
   @throws BusinessException if not exist
   */
  private static void requireRelationships(DSLContext db, Access access, PhaseEvent entity) throws BusinessException {
    if (access.isTopLevel())
      requireExists("Voice", db.selectCount().from(VOICE)
        .where(VOICE.ID.eq(ULong.valueOf(entity.getVoiceId())))
        .fetchOne(0, int.class));
    else
      requireExists("Voice", db.selectCount().from(VOICE)
        .join(PATTERN).on(PATTERN.ID.eq(VOICE.PATTERN_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .and(VOICE.ID.eq(ULong.valueOf(entity.getVoiceId())))
        .fetchOne(0, int.class));

    if (access.isTopLevel())
      requireExists("Phase", db.selectCount().from(PHASE)
        .where(PHASE.ID.eq(ULong.valueOf(entity.getPhaseId())))
        .fetchOne(0, int.class));
    else
      requireExists("Phase", db.selectCount().from(PHASE)
        .join(PATTERN).on(PATTERN.ID.eq(PHASE.PATTERN_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .and(PHASE.ID.eq(ULong.valueOf(entity.getPhaseId())))
        .fetchOne(0, int.class));
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(PhaseEvent entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(PHASE_EVENT.DURATION, entity.getDuration());
    fieldValues.put(PHASE_EVENT.INFLECTION, entity.getInflection());
    fieldValues.put(PHASE_EVENT.NOTE, entity.getNote());
    fieldValues.put(PHASE_EVENT.POSITION, entity.getPosition());
    fieldValues.put(PHASE_EVENT.TONALITY, entity.getTonality());
    fieldValues.put(PHASE_EVENT.VELOCITY, entity.getVelocity());
    fieldValues.put(PHASE_EVENT.VOICE_ID, entity.getVoiceId());
    fieldValues.put(PHASE_EVENT.PHASE_ID, entity.getPhaseId());
    return fieldValues;
  }

  @Override
  public PhaseEvent create(Access access, PhaseEvent entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public PhaseEvent readOne(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<PhaseEvent> readAll(Access access, Collection<BigInteger> parentIds) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, uLongValuesOf(parentIds)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Collection<PhaseEvent> readAllOfVoice(Access access, BigInteger voiceId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllOfVoice(tx.getContext(), access, ULong.valueOf(voiceId)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, PhaseEvent entity) throws Exception {
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
