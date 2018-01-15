// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.LibraryDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.audio_chord.AudioChord;
import io.xj.core.model.audio_event.AudioEvent;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument_meme.InstrumentMeme;
import io.xj.core.model.library.Library;
import io.xj.core.model.library.LibraryHash;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern_meme.PatternMeme;
import io.xj.core.model.phase.Phase;
import io.xj.core.model.phase_chord.PhaseChord;
import io.xj.core.model.phase_meme.PhaseMeme;
import io.xj.core.model.voice.Voice;
import io.xj.core.model.voice_event.VoiceEvent;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLConnection;

import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.api.client.util.Maps;
import com.google.inject.Inject;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

import static io.xj.core.Tables.AUDIO;
import static io.xj.core.Tables.AUDIO_CHORD;
import static io.xj.core.Tables.INSTRUMENT_MEME;
import static io.xj.core.Tables.LIBRARY;
import static io.xj.core.Tables.PATTERN;
import static io.xj.core.Tables.PHASE_CHORD;
import static io.xj.core.tables.Account.ACCOUNT;
import static io.xj.core.tables.AudioEvent.AUDIO_EVENT;
import static io.xj.core.tables.Instrument.INSTRUMENT;
import static io.xj.core.tables.PatternMeme.PATTERN_MEME;
import static io.xj.core.tables.Phase.PHASE;
import static io.xj.core.tables.PhaseMeme.PHASE_MEME;
import static io.xj.core.tables.Voice.VOICE;
import static io.xj.core.tables.VoiceEvent.VOICE_EVENT;

public class LibraryDAOImpl extends DAOImpl implements LibraryDAO {

  @Inject
  public LibraryDAOImpl(
    SQLDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  /**
   Create a new record

   @param db     context
   @param access control
   @param entity for new record
   @return newly readMany record
   @throws BusinessException if a Business Rule is violated
   */
  private static Library create(DSLContext db, Access access, Library entity) throws BusinessException {
    entity.validate();

    Map<Field, Object> fieldValues = fieldValueMap(entity);

    requireTopLevel(access);

    return modelFrom(executeCreate(db, LIBRARY, fieldValues), Library.class);
  }

  /**
   Read one record

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private static Library readOne(DSLContext db, Access access, ULong id) throws BusinessException {
    if (access.isTopLevel())
      return modelFrom(db.selectFrom(LIBRARY)
        .where(LIBRARY.ID.eq(id))
        .fetchOne(), Library.class);
    else
      return modelFrom(db.select(LIBRARY.fields())
        .from(LIBRARY)
        .where(LIBRARY.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(), Library.class);
  }

  /**
   Read all records in parent by id

   @param db        context
   @param access    control
   @param accountId of parent
   @return array of records
   */
  private static Collection<Library> readAll(DSLContext db, Access access, ULong accountId) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.select(LIBRARY.fields())
        .from(LIBRARY)
        .where(LIBRARY.ACCOUNT_ID.eq(accountId))
        .fetch(), Library.class);
    else
      return modelsFrom(db.select(LIBRARY.fields())
        .from(LIBRARY)
        .where(LIBRARY.ACCOUNT_ID.eq(accountId))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetch(), Library.class);
  }

  /**
   Read all records visible to user

   @param db     context
   @param access control
   @return array of records
   */
  private static Collection<Library> readAll(DSLContext db, Access access) throws BusinessException {
    if (access.isTopLevel())
      return modelsFrom(db.select(LIBRARY.fields())
        .from(LIBRARY)
        .fetch(), Library.class);
    else
      return modelsFrom(db.select(LIBRARY.fields())
        .from(LIBRARY)
        .where(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetch(), Library.class);
  }

  /**
   Update a record

   @param db     context
   @param access control
   @param id     of record
   @param entity to update with
   @throws BusinessException if a Business Rule is violated
   */
  private static void update(DSLContext db, Access access, ULong id, Library entity) throws BusinessException {
    entity.validate();
    Map<Field, Object> fieldValues = fieldValueMap(entity);
    fieldValues.put(LIBRARY.ID, id);

    requireTopLevel(access);

    requireExists("Account",
      db.selectCount().from(ACCOUNT)
        .where(ACCOUNT.ID.eq(ULong.valueOf(entity.getAccountId())))
        .fetchOne(0, int.class));

    if (0 == executeUpdate(db, LIBRARY, fieldValues))
      throw new BusinessException("No records updated.");
  }

  /**
   Delete a Library

   @param db        context
   @param access    control
   @param libraryId to delete
   @throws Exception         if database failure
   @throws ConfigException   if not configured properly
   @throws BusinessException if fails business rule
   */
  private static void delete(DSLContext db, Access access, ULong libraryId) throws Exception {
    requireTopLevel(access);

    requireNotExists("Pattern in Library", db.select(PATTERN.ID)
      .from(PATTERN)
      .where(PATTERN.LIBRARY_ID.eq(libraryId))
      .fetch().into(PATTERN));

    requireNotExists("Instrument in Library", db.select(INSTRUMENT.ID)
      .from(INSTRUMENT)
      .where(INSTRUMENT.LIBRARY_ID.eq(libraryId))
      .fetch().into(INSTRUMENT));

    db.deleteFrom(LIBRARY)
      .where(LIBRARY.ID.eq(libraryId))
      .andNotExists(
        db.select(PATTERN.ID)
          .from(PATTERN)
          .where(PATTERN.LIBRARY_ID.eq(libraryId))
      )
      .andNotExists(
        db.select(INSTRUMENT.ID)
          .from(INSTRUMENT)
          .where(INSTRUMENT.LIBRARY_ID.eq(libraryId))
      )
      .execute();
  }

  /**
   Only certain (writable) fields are mapped back to jOOQ records--
   Read-only fields are excluded from here.

   @param entity to source values from
   @return values mapped to record fields
   */
  private static Map<Field, Object> fieldValueMap(Library entity) {
    Map<Field, Object> fieldValues = Maps.newHashMap();
    fieldValues.put(LIBRARY.NAME, entity.getName());
    fieldValues.put(LIBRARY.ACCOUNT_ID, entity.getAccountId());
    return fieldValues;
  }

  /**
   [#154343470] Ops wants LibraryHash to compute the hash of an entire library, which can be used as a unique stamp of the state of the library's entire contents at any instant

   @param db     context
   @param access control
   @param id     of record
   @return record
   */
  private static LibraryHash readHash(DSLContext db, Access access, ULong id) throws BusinessException {
    requireTopLevel(access);

    LibraryHash result = new LibraryHash(id.toBigInteger());

    // SELECT `id,updated_at` FROM  `library` --> add entities to result objects
    putEntity(result, Library.class, db.select(LIBRARY.ID, LIBRARY.UPDATED_AT)
      .from(LIBRARY)
      .where(LIBRARY.ID.eq(id))
      .fetch());

    // SELECT `id,updated_at` FROM  `audio` --> add entities to result objects
    putEntity(result, Audio.class, db.select(AUDIO.ID, AUDIO.UPDATED_AT)
      .from(AUDIO)
      .join(INSTRUMENT).on(AUDIO.INSTRUMENT_ID.eq(INSTRUMENT.ID))
      .where(INSTRUMENT.LIBRARY_ID.eq(id))
      .fetch());

    // SELECT `id,updated_at` FROM  `audio_chord` --> add entities to result objects
    putEntity(result, AudioChord.class, db.select(AUDIO_CHORD.ID, AUDIO_CHORD.UPDATED_AT)
      .from(AUDIO_CHORD)
      .join(AUDIO).on(AUDIO_CHORD.AUDIO_ID.eq(AUDIO.ID))
      .join(INSTRUMENT).on(AUDIO.INSTRUMENT_ID.eq(INSTRUMENT.ID))
      .where(INSTRUMENT.LIBRARY_ID.eq(id))
      .fetch());

    // SELECT `id,updated_at` FROM  `audio_event` --> add entities to result objects
    putEntity(result, AudioEvent.class, db.select(AUDIO_EVENT.ID, AUDIO_EVENT.UPDATED_AT)
      .from(AUDIO_EVENT)
      .join(AUDIO).on(AUDIO_EVENT.AUDIO_ID.eq(AUDIO.ID))
      .join(INSTRUMENT).on(AUDIO.INSTRUMENT_ID.eq(INSTRUMENT.ID))
      .where(INSTRUMENT.LIBRARY_ID.eq(id))
      .fetch());

    // SELECT `id,updated_at` FROM  `instrument` --> add entities to result objects
    putEntity(result, Instrument.class, db.select(INSTRUMENT.ID, INSTRUMENT.UPDATED_AT)
      .from(INSTRUMENT)
      .where(INSTRUMENT.LIBRARY_ID.eq(id))
      .fetch());

    // SELECT `id,updated_at` FROM  `instrument_meme` --> add entities to result objects
    putEntity(result, InstrumentMeme.class, db.select(INSTRUMENT_MEME.ID, INSTRUMENT_MEME.UPDATED_AT)
      .from(INSTRUMENT_MEME)
      .join(INSTRUMENT).on(INSTRUMENT_MEME.INSTRUMENT_ID.eq(INSTRUMENT.ID))
      .where(INSTRUMENT.LIBRARY_ID.eq(id))
      .fetch());

    // SELECT `id,updated_at` FROM  `pattern` --> add entities to result objects
    putEntity(result, Pattern.class, db.select(PATTERN.ID, PATTERN.UPDATED_AT)
      .from(PATTERN)
      .where(PATTERN.LIBRARY_ID.eq(id))
      .fetch());

    // SELECT `id,updated_at` FROM  `pattern_meme` --> add entities to result objects
    putEntity(result, PatternMeme.class, db.select(PATTERN_MEME.ID, PATTERN_MEME.UPDATED_AT)
      .from(PATTERN_MEME)
      .join(PATTERN).on(PATTERN_MEME.PATTERN_ID.eq(PATTERN.ID))
      .where(PATTERN.LIBRARY_ID.eq(id))
      .fetch());

    // SELECT `id,updated_at` FROM  `phase` --> add entities to result objects
    putEntity(result, Phase.class, db.select(PHASE.ID, PHASE.UPDATED_AT)
      .from(PHASE)
      .join(PATTERN).on(PHASE.PATTERN_ID.eq(PATTERN.ID))
      .where(PATTERN.LIBRARY_ID.eq(id))
      .fetch());

    // SELECT `id,updated_at` FROM  `phase_chord` --> add entities to result objects
    putEntity(result, PhaseChord.class, db.select(PHASE_CHORD.ID, PHASE_CHORD.UPDATED_AT)
      .from(PHASE_CHORD)
      .join(PHASE).on(PHASE_CHORD.PHASE_ID.eq(PHASE.ID))
      .join(PATTERN).on(PHASE.PATTERN_ID.eq(PATTERN.ID))
      .where(PATTERN.LIBRARY_ID.eq(id))
      .fetch());

    // SELECT `id,updated_at` FROM  `phase_meme` --> add entities to result objects
    putEntity(result, PhaseMeme.class, db.select(PHASE_MEME.ID, PHASE_MEME.UPDATED_AT)
      .from(PHASE_MEME)
      .join(PHASE).on(PHASE_MEME.PHASE_ID.eq(PHASE.ID))
      .join(PATTERN).on(PHASE.PATTERN_ID.eq(PATTERN.ID))
      .where(PATTERN.LIBRARY_ID.eq(id))
      .fetch());

    // SELECT `id,updated_at` FROM  `voice` --> add entities to result objects
    putEntity(result, Voice.class, db.select(VOICE.ID, VOICE.UPDATED_AT)
      .from(VOICE)
      .join(PATTERN).on(VOICE.PATTERN_ID.eq(PATTERN.ID))
      .where(PATTERN.LIBRARY_ID.eq(id))
      .fetch());

    // SELECT `id,updated_at` FROM  `voice_event` --> add entities to result objects
    putEntity(result, VoiceEvent.class, db.select(VOICE_EVENT.ID, VOICE_EVENT.UPDATED_AT)
      .from(VOICE_EVENT)
      .join(VOICE).on(VOICE_EVENT.VOICE_ID.eq(VOICE.ID))
      .join(PATTERN).on(VOICE.PATTERN_ID.eq(PATTERN.ID))
      .where(PATTERN.LIBRARY_ID.eq(id))
      .fetch());

    return result;
  }

  /**
   Put the id, updated_at tuple into a library result hash, for a retrieved jooq record result set

   @param target to put data into
   @param clazz  of data
   @param data   to put
   */
  private static void putEntity(LibraryHash target, Class clazz, Result<Record2<ULong, Timestamp>> data) {
    data.forEach(tupleRecord -> target.putEntity(String.format(
      "%s-%s", clazz.getSimpleName(), tupleRecord.value1()
    ), tupleRecord.value2()));
  }

  @Override
  public Library create(Access access, Library entity) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(create(tx.getContext(), access, entity));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Library readOne(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readOne(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public LibraryHash readHash(Access access, BigInteger id) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      return tx.success(readHash(tx.getContext(), access, ULong.valueOf(id)));
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  @Nullable
  public Collection<Library> readAll(Access access, @Nullable BigInteger accountId) throws Exception {
    SQLConnection tx = dbProvider.getConnection();
    try {
      if (Objects.nonNull(accountId)) {
        return tx.success(readAll(tx.getContext(), access, ULong.valueOf(accountId)));
      } else {
        return tx.success(readAll(tx.getContext(), access));
      }

    } catch (Exception e) {
      throw tx.failure(e);
    }
  }

  @Override
  public void update(Access access, BigInteger id, Library entity) throws Exception {
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
      delete(tx.getContext(), access, ULong.valueOf(id));
      tx.success();
    } catch (Exception e) {
      throw tx.failure(e);
    }
  }


}
