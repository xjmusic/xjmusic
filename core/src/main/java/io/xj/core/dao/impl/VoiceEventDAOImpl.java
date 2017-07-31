// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.app.access.impl.Access;
import io.xj.core.app.exception.BusinessException;
import io.xj.core.app.exception.ConfigException;
import io.xj.core.dao.VoiceEventDAO;
import io.xj.core.db.sql.impl.SQLConnection;
import io.xj.core.db.sql.SQLDatabaseProvider;
import io.xj.core.model.voice_event.VoiceEvent;
import io.xj.core.tables.Voice;
import io.xj.core.tables.records.VoiceEventRecord;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.Map;

import static io.xj.core.Tables.VOICE;
import static io.xj.core.Tables.VOICE_EVENT;
import static io.xj.core.tables.Idea.IDEA;
import static io.xj.core.tables.Library.LIBRARY;
import static io.xj.core.tables.Phase.PHASE;

public class VoiceEventDAOImpl extends DAOImpl implements VoiceEventDAO {

  @Inject
  public VoiceEventDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public VoiceEventRecord create(Access access, VoiceEvent entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public VoiceEventRecord readOne(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneRecord(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Result<VoiceEventRecord> readAll(Access access, ULong voiceId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, voiceId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, ULong id, VoiceEvent entity) throws Exception {
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
   Create a new Voice Event

   @param db     context
   @param access control
   @param entity for new voice
   @return newly readMany record
   @throws BusinessException if failure
   */
  private VoiceEventRecord create(DSLContext db, Access access, VoiceEvent entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    if (access.isTopLevel())
      requireExists("Voice", db.select(VOICE.ID).from(VOICE)
        .where(VOICE.ID.eq(entity.getVoiceId()))
        .fetchOne());
    else
      requireExists("Voice", db.select(VOICE.ID).from(VOICE)
        .join(PHASE).on(PHASE.ID.eq(VOICE.PHASE_ID))
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .and(VOICE.ID.eq(entity.getVoiceId()))
        .fetchOne());

    return executeCreate(db, VOICE_EVENT, fieldValues);
  }

  /**
   Read one Event if able

   @param db     context
   @param access control
   @param id     of voice
   @return voice
   */
  private VoiceEventRecord readOneRecord(DSLContext db, Access access, ULong id) {
    if (access.isTopLevel())
      return db.selectFrom(VOICE_EVENT)
        .where(VOICE_EVENT.ID.eq(id))
        .fetchOne();
    else
      return recordInto(VOICE_EVENT, db.select(VOICE_EVENT.fields())
        .from(VOICE_EVENT)
        .join(VOICE).on(VOICE.ID.eq(VOICE_EVENT.VOICE_ID))
        .join(PHASE).on(PHASE.ID.eq(VOICE.PHASE_ID))
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(VOICE_EVENT.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
  }

  /**
   Read all Event able for an Idea

   @param db      context
   @param access  control
   @param voiceId to readMany all voice of
   @return array of voices
   @throws SQLException on failure
   */
  private Result<VoiceEventRecord> readAll(DSLContext db, Access access, ULong voiceId) throws SQLException {
    if (access.isTopLevel())
      return resultInto(VOICE_EVENT, db.select(VOICE_EVENT.fields())
        .from(VOICE_EVENT)
        .where(VOICE_EVENT.VOICE_ID.eq(voiceId))
        .orderBy(VOICE_EVENT.POSITION)
        .fetch());
    else
      return resultInto(VOICE_EVENT, db.select(VOICE_EVENT.fields())
        .from(VOICE_EVENT)
        .join(VOICE).on(VOICE.ID.eq(VOICE_EVENT.VOICE_ID))
        .join(PHASE).on(PHASE.ID.eq(VOICE.PHASE_ID))
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(VOICE_EVENT.VOICE_ID.eq(voiceId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .orderBy(VOICE_EVENT.POSITION)
        .fetch());
  }

  /**
   Update a Event record

   @param db     context
   @param access control
   @param id     to update
   @param entity to update with
   @throws BusinessException if failure
   */
  private void update(DSLContext db, Access access, ULong id, VoiceEvent entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();
    fieldValues.put(VOICE_EVENT.ID, id);

    if (access.isTopLevel())
      requireExists("Voice", db.select(VOICE.ID).from(VOICE)
        .where(VOICE.ID.eq(entity.getVoiceId()))
        .fetchOne());
    else
      requireExists("Voice", db.select(VOICE.ID).from(VOICE)
        .join(PHASE).on(PHASE.ID.eq(VOICE.PHASE_ID))
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .and(VOICE.ID.eq(entity.getVoiceId()))
        .fetchOne());

    if (executeUpdate(db, VOICE_EVENT, fieldValues) == 0)
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
  private void delete(Access access, DSLContext db, ULong id) throws Exception {
    if (!access.isTopLevel())
      requireExists("Voice Meme",
        db.select(VOICE_EVENT.ID).from(VOICE_EVENT)
          .join(Voice.VOICE).on(Voice.VOICE.ID.eq(VOICE_EVENT.VOICE_ID))
          .join(PHASE).on(PHASE.ID.eq(VOICE.PHASE_ID))
          .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
          .join(LIBRARY).on(IDEA.LIBRARY_ID.eq(LIBRARY.ID))
          .where(VOICE_EVENT.ID.eq(id))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
          .fetchOne());

    db.deleteFrom(VOICE_EVENT)
      .where(VOICE_EVENT.ID.eq(id))
      .execute();
  }

}
