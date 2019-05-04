// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import com.google.api.client.util.Maps;
import com.google.inject.Inject;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.VoiceDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.voice.Voice;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.types.ULong;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

import static io.xj.core.tables.Library.LIBRARY;
import static io.xj.core.tables.PatternEvent.PATTERN_EVENT;
import static io.xj.core.tables.Sequence.SEQUENCE;
import static io.xj.core.tables.Voice.VOICE;

public class VoiceDAOImpl extends DAOImpl implements VoiceDAO {

  @Inject
  public VoiceDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  /**
   Create a new Voice

   @param db     context
   @param access control
   @param entity for new voice
   @return newly readMany record
   @throws CoreException if failure
   */
  private static Voice create(DSLContext db, Access access, Voice entity) throws CoreException {
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
  private static Voice readOne(DSLContext db, Access access, ULong id) throws CoreException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(VOICE)
        .where(VOICE.ID.eq(id))
        .fetchOne(), Voice.class);
    else
      return modelFrom(db.select(VOICE.fields())
        .from(VOICE)
        .join(SEQUENCE).on(SEQUENCE.ID.eq(VOICE.SEQUENCE_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(SEQUENCE.LIBRARY_ID))
        .where(VOICE.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), Voice.class);
  }

  /**
   Read all Voice able for an Pattern

   @param db         context
   @param access     control
   @param sequenceId to readMany all voice of
   @return array of voices
   */
  private static Collection<Voice> readAll(DSLContext db, Access access, Collection<ULong> sequenceId) throws CoreException {
    if (access.isTopLevel())
      return modelsFrom(db.select(VOICE.fields())
        .from(VOICE)
        .where(VOICE.SEQUENCE_ID.in(sequenceId))
        .fetch(), Voice.class);
    else
      return modelsFrom(db.select(VOICE.fields())
        .from(VOICE)
        .join(SEQUENCE).on(SEQUENCE.ID.eq(VOICE.SEQUENCE_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(SEQUENCE.LIBRARY_ID))
        .where(VOICE.SEQUENCE_ID.in(sequenceId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetch(), Voice.class);
  }

  /**
   Update a Voice record

   @param db     context
   @param access control
   @param id     to update
   @param entity to update with
   @throws CoreException if failure
   */
  private static void update(DSLContext db, Access access, ULong id, Voice entity) throws CoreException {
    entity.validate();
    requireRelationships(db, access, entity);

    Map<Field, Object> fieldValues = fieldValueMap(entity);
    fieldValues.put(VOICE.ID, id);
    if (0 == executeUpdate(db, VOICE, fieldValues))
      throw new CoreException("No records updated.");
  }

  /**
   Delete an Voice

   @param db context
   @param id to delete
   @throws CoreException if database failure
   @throws CoreException if not configured properly
   @throws CoreException if fails business rule
   */
  private static void delete(Access access, DSLContext db, ULong id) throws CoreException {
    if (!access.isTopLevel())
      requireExists("Voice", db.selectCount().from(VOICE)
        .join(SEQUENCE).on(SEQUENCE.ID.eq(VOICE.SEQUENCE_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(SEQUENCE.LIBRARY_ID))
        .where(VOICE.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    db.deleteFrom(PATTERN_EVENT)
      .where(PATTERN_EVENT.VOICE_ID.eq(id))
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
   @throws CoreException if relationships do not exist
   */
  private static void requireRelationships(DSLContext db, Access access, Voice entity) throws CoreException {
    if (access.isTopLevel())
      requireExists("Sequence", db.selectCount().from(SEQUENCE)
        .where(SEQUENCE.ID.eq(ULong.valueOf(entity.getSequenceId())))
        .fetchOne(0, int.class));
    else
      requireExists("Sequence", db.selectCount().from(SEQUENCE)
        .join(LIBRARY).on(LIBRARY.ID.eq(SEQUENCE.LIBRARY_ID))
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .and(SEQUENCE.ID.eq(ULong.valueOf(entity.getSequenceId())))
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
    fieldValues.put(VOICE.SEQUENCE_ID, entity.getSequenceId());
    fieldValues.put(VOICE.TYPE, entity.getType());
    fieldValues.put(VOICE.DESCRIPTION, entity.getDescription());
    return fieldValues;
  }

  @Override
  public Voice create(Access access, Voice entity) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Voice readOne(Access access, BigInteger id) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Collection<Voice> readAll(Access access, Collection<BigInteger> parentIds) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readAll(tx.getContext(), access, uLongValuesOf(parentIds)));
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, Voice entity) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      update(tx.getContext(), access, ULong.valueOf(id), entity);
      tx.success();
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void destroy(Access access, BigInteger id) throws CoreException {
    SQLConnection tx = dbProvider.getConnection();
    try {
      delete(access, tx.getContext(), ULong.valueOf(id));
      tx.success();
    } catch (CoreException e) {
      throw tx.failure(e);
    }
  }

}
