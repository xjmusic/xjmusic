// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.ProgramSequenceChord;
import io.xj.hub.tables.pojos.ProgramSequenceChordVoicing;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.tables.records.ProgramSequenceChordVoicingRecord;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.ChordEntity;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.StringUtils;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.ValueUtils;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static io.xj.hub.Tables.LIBRARY;
import static io.xj.hub.Tables.PROGRAM;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_CHORD;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_CHORD_VOICING;
import static io.xj.hub.Tables.PROGRAM_VOICE;

@Service
public class ProgramSequenceChordManagerImpl extends HubPersistenceServiceImpl implements ProgramSequenceChordManager {

  final ProgramVoiceManager programVoiceManager;

  public ProgramSequenceChordManagerImpl(
    EntityFactory entityFactory,
    ProgramVoiceManager programVoiceManager,
    HubSqlStoreProvider sqlStoreProvider
  ) {
    super(entityFactory, sqlStoreProvider);
    this.programVoiceManager = programVoiceManager;
  }

  @Override
  public ProgramSequenceChord create(
    HubAccess access,
    ProgramSequenceChord entity
  ) throws ManagerException, JsonapiException, ValueException {
    DSLContext db = sqlStoreProvider.getDSL();
    validate(entity);
    requireArtist(access);
    requireProgramModification(db, access, entity.getProgramId());

    requireNotExists(String.format("Chord in sequence at position %f", entity.getPosition()),
      db.selectCount().from(PROGRAM_SEQUENCE_CHORD)
        .where(PROGRAM_SEQUENCE_CHORD.PROGRAM_SEQUENCE_ID.eq(entity.getProgramSequenceId()))
        .and(PROGRAM_SEQUENCE_CHORD.POSITION.eq(entity.getPosition()))
        .fetchOne(0, int.class));

    return modelFrom(ProgramSequenceChord.class, executeCreate(db, PROGRAM_SEQUENCE_CHORD, entity));
  }

  @Override
  @Nullable
  public ProgramSequenceChord readOne(
    HubAccess access,
    UUID id
  ) throws ManagerException {
    requireArtist(access);
    DSLContext db = sqlStoreProvider.getDSL();
    return readOne(db, access, id);
  }

  @Override
  @Nullable
  public Collection<ProgramSequenceChord> readMany(
    HubAccess access,
    Collection<UUID> programIds
  ) throws ManagerException {
    requireArtist(access);
    if (access.isTopLevel())
      return modelsFrom(ProgramSequenceChord.class,
        sqlStoreProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_CHORD)
          .where(PROGRAM_SEQUENCE_CHORD.PROGRAM_ID.in(programIds))
          .fetch());
    else
      return modelsFrom(ProgramSequenceChord.class,
        sqlStoreProvider.getDSL().select(PROGRAM_SEQUENCE_CHORD.fields()).from(PROGRAM_SEQUENCE_CHORD)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_CHORD.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_SEQUENCE_CHORD.PROGRAM_ID.in(programIds))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetch());
  }

  @Override
  public Collection<ProgramSequenceChord> search(
    HubAccess access,
    UUID libraryId,
    String chordName
  ) throws ManagerException {
    DSLContext db = sqlStoreProvider.getDSL();
    requireArtist(access);
    requireLibraryRead(db, access, libraryId);
    if (StringUtils.isNullOrEmpty(chordName))
      throw new ManagerException("Search requires at least one character of text!");

    return modelsFrom(ProgramSequenceChord.class,
      sqlStoreProvider.getDSL().select(PROGRAM_SEQUENCE_CHORD.fields()).from(PROGRAM_SEQUENCE_CHORD)
        .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_CHORD.PROGRAM_ID))
        .where(DSL.lower(PROGRAM_SEQUENCE_CHORD.NAME).like(String.format("%s%%", chordName).toLowerCase(Locale.ROOT)))
        .and(PROGRAM.LIBRARY_ID.eq(libraryId))
        .fetch())
      .stream()
      .toList();
  }

  @Override
  public ManagerCloner<ProgramSequenceChord> clone(HubAccess access, UUID cloneId, ProgramSequenceChord entity, List<InstrumentType> voicingTypes) throws ManagerException {
    requireArtist(access);
    AtomicReference<ManagerCloner<ProgramSequenceChord>> result = new AtomicReference<>();
    DSLContext db = sqlStoreProvider.getDSL();

    requireNotExists(String.format("Chord in sequence at position %f", entity.getPosition()),
      db.selectCount().from(PROGRAM_SEQUENCE_CHORD)
        .where(PROGRAM_SEQUENCE_CHORD.PROGRAM_SEQUENCE_ID.eq(entity.getProgramSequenceId()))
        .and(PROGRAM_SEQUENCE_CHORD.POSITION.eq(entity.getPosition()))
        .fetchOne(0, int.class));

    db.transaction(ctx -> result.set(clone(DSL.using(ctx), access, cloneId, entity, voicingTypes)));
    return result.get();
  }

  @Override
  public ProgramSequenceChord update(
    HubAccess access,
    UUID id,
    ProgramSequenceChord entity
  ) throws ManagerException, JsonapiException, ValueException {
    DSLContext db = sqlStoreProvider.getDSL();
    validate(entity);
    requireArtist(access);
    requireProgramModification(db, access, entity.getProgramId());
    executeUpdate(db, PROGRAM_SEQUENCE_CHORD, id, entity);
    requireNotExists(String.format("Chord in sequence at position %f", entity.getPosition()),
      db.selectCount().from(PROGRAM_SEQUENCE_CHORD)
        .where(PROGRAM_SEQUENCE_CHORD.PROGRAM_SEQUENCE_ID.eq(entity.getProgramSequenceId()))
        .and(PROGRAM_SEQUENCE_CHORD.POSITION.eq(entity.getPosition()))
        .and(PROGRAM_SEQUENCE_CHORD.ID.notEqual(id))
        .fetchOne(0, int.class));
    return entity;
  }

  @Override
  public void destroy(
    HubAccess access,
    UUID id
  ) throws ManagerException {
    DSLContext db = sqlStoreProvider.getDSL();
    requireArtist(access);
    ProgramSequenceChord chord = readOne(access, id);
    if (Objects.isNull(chord))
      throw new ManagerException("Chord does not exist!");
    requireProgramModification(db, access, chord.getProgramId());
    db.deleteFrom(PROGRAM_SEQUENCE_CHORD_VOICING)
      .where(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_SEQUENCE_CHORD_ID.eq(id))
      .execute();
    db.deleteFrom(PROGRAM_SEQUENCE_CHORD)
      .where(PROGRAM_SEQUENCE_CHORD.ID.eq(id))
      .execute();
  }

  @Override
  public ProgramSequenceChord newInstance() {
    return new ProgramSequenceChord();
  }

  /**
   * Validate data
   *
   * @param record to validate
   * @throws ManagerException if invalid
   */
  public ProgramSequenceChord validate(ProgramSequenceChord record) throws ManagerException {
    try {
      ValueUtils.require(record.getProgramId(), "Program ID");
      ValueUtils.require(record.getProgramSequenceId(), "Sequence ID");
      ChordEntity.validate(record);
      return record;

    } catch (ValueException e) {
      throw new ManagerException(e);
    }
  }

  /**
   * Require user has access to read a library
   *
   * @param db        database
   * @param access    control
   * @param libraryId for which to require access
   * @throws ManagerException if there is no access
   */
  void requireLibraryRead(DSLContext db, HubAccess access, UUID libraryId) throws ManagerException {
    if (access.isTopLevel()) return;
    requireExists("Library", db.selectCount().from(LIBRARY)
      .where(LIBRARY.ID.eq(libraryId))
      .and(LIBRARY.IS_DELETED.eq(false))
      .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
      .fetchOne(0, int.class));
  }

  /**
   * Read a sequence chord
   *
   * @param db     database
   * @param access control
   * @param id     of chord to read
   * @return chord
   * @throws ManagerException if there is no access
   */
  ProgramSequenceChord readOne(DSLContext db, HubAccess access, UUID id) throws ManagerException {
    if (access.isTopLevel())
      return modelFrom(ProgramSequenceChord.class,
        db.selectFrom(PROGRAM_SEQUENCE_CHORD)
          .where(PROGRAM_SEQUENCE_CHORD.ID.eq(id))
          .fetchOne());
    else
      return modelFrom(ProgramSequenceChord.class,
        db.select(PROGRAM_SEQUENCE_CHORD.fields()).from(PROGRAM_SEQUENCE_CHORD)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_CHORD.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_SEQUENCE_CHORD.ID.eq(id))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne());
  }

  /**
   * Clone a chord's voicings
   *
   * @param db      database
   * @param access  control
   * @param cloneId of chord to clone
   * @param entity  new chord attributes
   * @return cloner
   * @throws ManagerException on failure
   */
  ManagerCloner<ProgramSequenceChord> clone(DSLContext db, HubAccess access, UUID cloneId, ProgramSequenceChord entity, List<InstrumentType> voicingTypes) throws ManagerException, ValueException, JsonapiException {
    requireArtist(access);

    ProgramSequenceChord from = readOne(db, access, cloneId);
    if (Objects.isNull(from))
      throw new ManagerException("Can't clone nonexistent ProgramSequenceChord");

    ProgramSequenceChord programSequenceChord = validate(entity);
    requireProgramModification(db, access, entity.getProgramId());

    var fromVoicesById = modelsFrom(ProgramVoice.class,
      db.selectFrom(PROGRAM_VOICE).where(PROGRAM_VOICE.PROGRAM_ID.eq(from.getProgramId()))).stream()
      .collect(Collectors.toMap(ProgramVoice::getId, (v) -> v));

    var targetVoicesByType = modelsFrom(ProgramVoice.class,
      db.selectFrom(PROGRAM_VOICE).where(PROGRAM_VOICE.PROGRAM_ID.eq(entity.getProgramId()))).stream()
      .collect(Collectors.toMap(ProgramVoice::getType, (v) -> v));

    var target = modelFrom(ProgramSequenceChord.class, executeCreate(db, PROGRAM_SEQUENCE_CHORD, programSequenceChord));
    ManagerCloner<ProgramSequenceChord> cloner = new ManagerCloner<>(target, this);

    ProgramSequenceChordVoicingRecord voicing;
    InstrumentType voiceType;
    for (var vc : db.selectFrom(PROGRAM_SEQUENCE_CHORD_VOICING)
      .where(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_SEQUENCE_CHORD_ID.eq(cloneId))
      .fetch())
      if (fromVoicesById.containsKey(vc.getProgramVoiceId())) {
        voiceType = fromVoicesById.get(vc.getProgramVoiceId()).getType();
        if (voicingTypes.contains(voiceType)) {

          if (!targetVoicesByType.containsKey(voiceType))
            targetVoicesByType.put(voiceType, programVoiceManager.add(db, target.getProgramId(), voiceType));

          voicing = db.newRecord(PROGRAM_SEQUENCE_CHORD_VOICING);
          voicing.setId(UUID.randomUUID());
          voicing.setProgramId(target.getProgramId());
          voicing.setProgramSequenceChordId(target.getId());
          voicing.setNotes(vc.getNotes());
          voicing.setProgramVoiceId(targetVoicesByType.get(voiceType).getId());
          voicing.store();
          cloner.addChildClone(modelFrom(ProgramSequenceChordVoicing.class, voicing));
        }
      }


    // Creating a new chord via cloning, include created voices in response payload
    // https://www.pivotaltracker.com/story/show/182254064
    targetVoicesByType.values().forEach(cloner::addChildClone);

    return cloner;
  }
}
