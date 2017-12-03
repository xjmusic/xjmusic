// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.dao.VoiceDAO;
import io.xj.core.persistence.sql.impl.SQLConnection;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.model.voice.Voice;
import io.xj.core.tables.records.VoiceRecord;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.util.Map;

import static io.xj.core.Tables.VOICE_EVENT;
import static io.xj.core.tables.Pattern.PATTERN;
import static io.xj.core.tables.Library.LIBRARY;
import static io.xj.core.tables.Phase.PHASE;
import static io.xj.core.tables.Voice.VOICE;

public class VoiceDAOImpl extends DAOImpl implements VoiceDAO {

  @Inject
  public VoiceDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public VoiceRecord create(Access access, Voice entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public VoiceRecord readOne(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneRecord(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Result<VoiceRecord> readAll(Access access, ULong phaseId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, phaseId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public Result<VoiceRecord> readAllForPatternPhaseOffset(Access access, ULong patternId, ULong phaseOffset) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAllForPatternPhaseOffset(tx.getContext(), access, patternId, phaseOffset));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, ULong id, Voice entity) throws Exception {
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
   Create a new Voice

   @param db     context
   @param access control
   @param entity for new voice
   @return newly readMany record
   @throws BusinessException if failure
   */
  private VoiceRecord createRecord(DSLContext db, Access access, Voice entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    if (access.isTopLevel())
      requireExists("Phase", db.selectCount().from(PHASE)
        .where(PHASE.ID.eq(entity.getPhaseId()))
        .fetchOne(0, int.class));
    else
      requireExists("Phase", db.selectCount().from(PHASE)
        .join(PATTERN).on(PATTERN.ID.eq(PHASE.PATTERN_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .and(PHASE.ID.eq(entity.getPhaseId()))
        .fetchOne(0, int.class));

    return executeCreate(db, VOICE, fieldValues);
  }

  /**
   Read one Voice if able

   @param db     context
   @param access control
   @param id     of voice
   @return voice
   */
  private VoiceRecord readOneRecord(DSLContext db, Access access, ULong id) {
    if (access.isTopLevel())
      return db.selectFrom(VOICE)
        .where(VOICE.ID.eq(id))
        .fetchOne();
    else
      return recordInto(VOICE, db.select(VOICE.fields())
        .from(VOICE)
        .join(PHASE).on(PHASE.ID.eq(VOICE.PHASE_ID))
        .join(PATTERN).on(PATTERN.ID.eq(PHASE.PATTERN_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(VOICE.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
  }

  /**
   Read all Voice able for an Phase

   @param db      context
   @param access  control
   @param phaseId to readMany all voice of
   @return array of voices
   */
  private Result<VoiceRecord> readAll(DSLContext db, Access access, ULong phaseId) {
    if (access.isTopLevel())
      return resultInto(VOICE, db.select(VOICE.fields())
        .from(VOICE)
        .where(VOICE.PHASE_ID.eq(phaseId))
        .fetch());
    else
      return resultInto(VOICE, db.select(VOICE.fields())
        .from(VOICE)
        .join(PHASE).on(PHASE.ID.eq(VOICE.PHASE_ID))
        .join(PATTERN).on(PATTERN.ID.eq(PHASE.PATTERN_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(VOICE.PHASE_ID.eq(phaseId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetch());
  }

  /**
   Fetch all accessible Voice for an pattern phase by offset

   @param access      control
   @param patternId      to fetch phase voices for
   @param phaseOffset offset of phase in pattern
   @return voices in phase
   */
  private Result<VoiceRecord> readAllForPatternPhaseOffset(DSLContext db, Access access, ULong patternId, ULong phaseOffset) throws Exception {
    requireTopLevel(access);
    return resultInto(VOICE, db.select(VOICE.fields())
      .from(VOICE)
      .join(PHASE).on(PHASE.ID.eq(VOICE.PHASE_ID))
      .where(PHASE.PATTERN_ID.eq(patternId))
      .and(PHASE.OFFSET.eq(phaseOffset))
      .fetch());
  }

  /**
   Update a Voice record

   @param db     context
   @param access control
   @param id     to update
   @param entity to update with
   @throws BusinessException if failure
   */
  private void update(DSLContext db, Access access, ULong id, Voice entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();
    fieldValues.put(VOICE.ID, id);

    if (access.isTopLevel())
      requireExists("Phase", db.selectCount().from(PHASE)
        .where(PHASE.ID.eq(entity.getPhaseId()))
        .fetchOne(0, int.class));
    else
      requireExists("Phase", db.selectCount().from(PHASE)
        .join(PATTERN).on(PATTERN.ID.eq(PHASE.PATTERN_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .and(PHASE.ID.eq(entity.getPhaseId()))
        .fetchOne(0, int.class));

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
  private void delete(Access access, DSLContext db, ULong id) throws Exception {
    requireNotExists("Event in Voice", db.select(VOICE_EVENT.ID)
      .from(VOICE_EVENT)
      .where(VOICE_EVENT.VOICE_ID.eq(id))
      .fetch());

    if (!access.isTopLevel())
      requireExists("Voice", db.selectCount().from(VOICE)
        .join(PHASE).on(PHASE.ID.eq(VOICE.PHASE_ID))
        .join(PATTERN).on(PATTERN.ID.eq(PHASE.PATTERN_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PATTERN.LIBRARY_ID))
        .where(VOICE.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne(0, int.class));

    db.deleteFrom(VOICE)
      .where(VOICE.ID.eq(id))
      .andNotExists(
        db.select(VOICE_EVENT.ID)
          .from(VOICE_EVENT)
          .where(VOICE_EVENT.VOICE_ID.eq(id))
      )
      .execute();
  }

}
