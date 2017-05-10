// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.dao.impl;

import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.dao.PhaseDAO;
import io.outright.xj.core.db.sql.SQLConnection;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.model.phase.Phase;
import io.outright.xj.core.tables.records.PhaseRecord;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.Map;

import static io.outright.xj.core.Tables.PHASE_MEME;
import static io.outright.xj.core.tables.Idea.IDEA;
import static io.outright.xj.core.tables.Library.LIBRARY;
import static io.outright.xj.core.tables.Phase.PHASE;
import static io.outright.xj.core.tables.PhaseChord.PHASE_CHORD;
import static io.outright.xj.core.tables.Voice.VOICE;

public class PhaseDAOImpl extends DAOImpl implements PhaseDAO {

  @Inject
  public PhaseDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @Override
  public PhaseRecord create(Access access, Phase entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(createRecord(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public PhaseRecord readOne(Access access, ULong id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneRecord(tx.getContext(), access, id));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Nullable
  @Override
  public PhaseRecord readOneForIdea(Access access, ULong ideaId, ULong ideaPhaseOffset) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOneForIdea(tx.getContext(), access, ideaId, ideaPhaseOffset));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Result<PhaseRecord> readAll(Access access, ULong ideaId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, ideaId));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, ULong id, Phase entity) throws Exception {
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
   Create a new Phase

   @param db     context
   @param access control
   @param entity for new phase
   @return newly readMany record
   @throws BusinessException if failure
   */
  private PhaseRecord createRecord(DSLContext db, Access access, Phase entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();

    // [#237] shouldn't be able to create phase with same offset in idea
    requireNotExists("phase with same offset in idea",
      db.select(PHASE.ID).from(PHASE)
        .where(PHASE.IDEA_ID.eq(entity.getIdeaId()))
        .and(PHASE.OFFSET.eq(entity.getOffset()))
        .fetch());

    // Common for Create/Update
    deepValidate(db, access, entity);

    return executeCreate(db, PHASE, fieldValues);
  }

  /**
   Read one Phase if able

   @param db     context
   @param access control
   @param id     of phase
   @return phase
   */
  private PhaseRecord readOneRecord(DSLContext db, Access access, ULong id) {
    if (access.isTopLevel())
      return db.selectFrom(PHASE)
        .where(PHASE.ID.eq(id))
        .fetchOne();
    else
      return recordInto(PHASE, db.select(PHASE.fields())
        .from(PHASE)
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(PHASE.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
  }

  /**
   Read one Phase if able

   @param db              context
   @param access          control
   @param ideaId          of idea in which to read phase
   @param ideaPhaseOffset of phase in idea
   @return phase record
   */
  private PhaseRecord readOneForIdea(DSLContext db, Access access, ULong ideaId, ULong ideaPhaseOffset) {
    if (access.isTopLevel())
      return db.selectFrom(PHASE)
        .where(PHASE.IDEA_ID.eq(ideaId))
        .and(PHASE.OFFSET.eq(ideaPhaseOffset))
        .fetchOne();
    else
      return recordInto(PHASE, db.select(PHASE.fields())
        .from(PHASE)
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(PHASE.IDEA_ID.eq(ideaId))
        .and(PHASE.OFFSET.eq(ideaPhaseOffset))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());
  }

  /**
   Read all Phase able for an Idea

   @param db     context
   @param access control
   @param ideaId to readMany all phase of
   @return array of phases
   @throws SQLException on failure
   */
  private Result<PhaseRecord> readAll(DSLContext db, Access access, ULong ideaId) throws SQLException {
    if (access.isTopLevel())
      return resultInto(PHASE, db.select(PHASE.fields())
        .from(PHASE)
        .where(PHASE.IDEA_ID.eq(ideaId))
        .fetch());
    else
      return resultInto(PHASE, db.select(PHASE.fields())
        .from(PHASE)
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(PHASE.IDEA_ID.eq(ideaId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetch());
  }

  /**
   Update a Phase record

   @param db     context
   @param access control
   @param id     to update
   @param entity to update with
   @throws BusinessException if failure
   */
  private void update(DSLContext db, Access access, ULong id, Phase entity) throws Exception {
    entity.validate();

    Map<Field, Object> fieldValues = entity.updatableFieldValueMap();
    fieldValues.put(PHASE.ID, id);

    // Common for Create/Update
    deepValidate(db, access, entity);

    if (executeUpdate(db, PHASE, fieldValues) == 0)
      throw new BusinessException("No records updated.");
  }

  /**
   Delete an Phase

   @param db context
   @param id to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private void delete(Access access, DSLContext db, ULong id) throws Exception {
    requireNotExists("Voice in Phase", db.select(VOICE.ID)
      .from(VOICE)
      .where(VOICE.PHASE_ID.eq(id))
      .fetch());

    requireNotExists("Meme in Phase", db.select(PHASE_MEME.ID)
      .from(PHASE_MEME)
      .where(PHASE_MEME.PHASE_ID.eq(id))
      .fetch());

    requireNotExists("Chord in Phase", db.select(PHASE_CHORD.ID)
      .from(PHASE_CHORD)
      .where(PHASE_CHORD.PHASE_ID.eq(id))
      .fetch());

    if (!access.isTopLevel())
      requireExists("Phase", db.select(PHASE.ID).from(PHASE)
        .join(IDEA).on(IDEA.ID.eq(PHASE.IDEA_ID))
        .join(LIBRARY).on(IDEA.LIBRARY_ID.eq(LIBRARY.ID))
        .where(PHASE.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .fetchOne());

    db.deleteFrom(PHASE)
      .where(PHASE.ID.eq(id))
      .andNotExists(
        db.select(VOICE.ID)
          .from(VOICE)
          .where(VOICE.PHASE_ID.eq(id))
      )
      .andNotExists(
        db.select(PHASE_MEME.ID)
          .from(PHASE_MEME)
          .where(PHASE_MEME.PHASE_ID.eq(id))
      )
      .andNotExists(
        db.select(PHASE_CHORD.ID)
          .from(PHASE_CHORD)
          .where(PHASE_CHORD.PHASE_ID.eq(id))
      )
      .execute();
  }

  /**
   Provides consistent validation of a model for Creation/Update

   @param db     context
   @param access control
   @param entity to validate
   @throws BusinessException if invalid
   */
  private void deepValidate(DSLContext db, Access access, Phase entity) throws BusinessException {
    // actually select the parent idea for validation
    Record idea;
    if (access.isTopLevel())
      idea = db.select(IDEA.ID, IDEA.TYPE).from(IDEA)
        .where(IDEA.ID.eq(entity.getIdeaId()))
        .fetchOne();
    else
      idea = db.select(IDEA.ID, IDEA.TYPE).from(IDEA)
        .join(LIBRARY).on(LIBRARY.ID.eq(IDEA.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccounts()))
        .and(IDEA.ID.eq(entity.getIdeaId()))
        .fetchOne();

    requireExists("Idea", idea);

    // [#199] Macro-type Idea `total` not required; still is required for other types of Idea
    if (!idea.get(IDEA.TYPE).equals(Idea.MACRO)) {
      String d = "for a phase of a non-macro-type idea, total (# beats)";
      requireNonNull(d, entity.getTotal());
      requireGreaterThanZero(d, entity.getTotal());
    }
  }

}
