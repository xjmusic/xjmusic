// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import com.google.api.client.util.Sets;
import com.google.inject.Inject;
import io.xj.hub.access.HubAccess;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.tables.pojos.ProgramSequenceChord;
import io.xj.hub.tables.pojos.ProgramSequenceChordVoicing;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;
import org.jooq.DSLContext;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.xj.hub.Tables.LIBRARY;
import static io.xj.hub.Tables.PROGRAM;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_CHORD;
import static io.xj.hub.Tables.PROGRAM_SEQUENCE_CHORD_VOICING;
import static io.xj.hub.Tables.PROGRAM_VOICE;

public class ProgramSequenceChordVoicingManagerImpl extends HubPersistenceServiceImpl<ProgramSequenceChordVoicing> implements ProgramSequenceChordVoicingManager {
  private static final String VOICING_NOTES_EMPTY = "(None)";

  @Inject
  public ProgramSequenceChordVoicingManagerImpl(
    EntityFactory entityFactory,
    HubDatabaseProvider dbProvider
  ) {
    super(entityFactory, dbProvider);
  }

  @Override
  public ProgramSequenceChordVoicing create(HubAccess access, ProgramSequenceChordVoicing entity) throws ManagerException, JsonapiException, ValueException {
    DSLContext db = dbProvider.getDSL();
    validate(entity);
    requireArtist(access);
    requireProgramModification(db, access, entity.getProgramId());
    requireNotExistsForChordAndVoice(db, entity);
    return modelFrom(ProgramSequenceChordVoicing.class, executeCreate(db, PROGRAM_SEQUENCE_CHORD_VOICING, entity));
  }

  @Override
  @Nullable
  public ProgramSequenceChordVoicing readOne(HubAccess access, UUID id) throws ManagerException {
    requireArtist(access);
    if (access.isTopLevel())
      return modelFrom(ProgramSequenceChordVoicing.class,
        dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_CHORD_VOICING)
          .where(PROGRAM_SEQUENCE_CHORD_VOICING.ID.eq(id))
          .fetchOne());
    else
      return modelFrom(ProgramSequenceChordVoicing.class,
        dbProvider.getDSL().select(PROGRAM_SEQUENCE_CHORD_VOICING.fields()).from(PROGRAM_SEQUENCE_CHORD_VOICING)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_SEQUENCE_CHORD_VOICING.ID.eq(id))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetchOne());
  }

  @Override
  @Nullable
  public Collection<ProgramSequenceChordVoicing> readMany(HubAccess access, Collection<UUID> programIds) throws ManagerException {
    requireArtist(access);
    if (access.isTopLevel())
      return modelsFrom(ProgramSequenceChordVoicing.class,
        dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_CHORD_VOICING)
          .where(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_ID.in(programIds))
          .fetch());
    else
      return modelsFrom(ProgramSequenceChordVoicing.class,
        dbProvider.getDSL().select(PROGRAM_SEQUENCE_CHORD_VOICING.fields()).from(PROGRAM_SEQUENCE_CHORD_VOICING)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_ID.in(programIds))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetch());
  }

  @Override
  public Collection<ProgramSequenceChordVoicing> readManyForChords(HubAccess access, List<UUID> chordIds) throws ManagerException {
    requireArtist(access);
    if (access.isTopLevel())
      return modelsFrom(ProgramSequenceChordVoicing.class,
        dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_CHORD_VOICING)
          .where(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_SEQUENCE_CHORD_ID.in(chordIds))
          .fetch());
    else
      return modelsFrom(ProgramSequenceChordVoicing.class,
        dbProvider.getDSL().select(PROGRAM_SEQUENCE_CHORD_VOICING.fields()).from(PROGRAM_SEQUENCE_CHORD_VOICING)
          .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_ID))
          .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
          .where(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_SEQUENCE_CHORD_ID.in(chordIds))
          .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
          .fetch());
  }

  @Override
  public Collection<ProgramSequenceChordVoicing> createEmptyVoicings(HubAccess access, ProgramSequenceChord chord) throws ManagerException {
    DSLContext db = dbProvider.getDSL();
    requireArtist(access);
    requireProgramModification(db, access, chord.getProgramId());
    var existingVoicingVoiceIds = modelsFrom(ProgramSequenceChordVoicing.class,
      dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_CHORD_VOICING)
        .where(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_SEQUENCE_CHORD_ID.eq(chord.getId()))
        .fetch()).stream().map(ProgramSequenceChordVoicing::getProgramVoiceId).collect(Collectors.toSet());
    var missingVoicingVoiceIds = modelsFrom(ProgramVoice.class,
      dbProvider.getDSL().selectFrom(PROGRAM_VOICE)
        .where(PROGRAM_VOICE.PROGRAM_ID.eq(chord.getProgramId()))
        .fetch()).stream().map(ProgramVoice::getId)
      .filter((id) -> !existingVoicingVoiceIds.contains(id))
      .collect(Collectors.toSet());
    Collection<ProgramSequenceChordVoicing> voicings = Sets.newHashSet();
    for (var voiceId : missingVoicingVoiceIds)
      voicings.add(createEmptyVoicing(db, chord.getProgramId(), voiceId, chord.getId()));
    return voicings;
  }

  @Override
  public Collection<ProgramSequenceChordVoicing> createEmptyVoicings(HubAccess access, ProgramVoice voice) throws ManagerException {
    DSLContext db = dbProvider.getDSL();
    requireArtist(access);
    requireProgramModification(db, access, voice.getProgramId());
    var existingVoiceChordIds = modelsFrom(ProgramSequenceChordVoicing.class,
      dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_CHORD_VOICING)
        .where(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_VOICE_ID.eq(voice.getId()))
        .fetch()).stream().map(ProgramSequenceChordVoicing::getProgramSequenceChordId).collect(Collectors.toSet());
    var missingVoiceChordIds = modelsFrom(ProgramSequenceChord.class,
      dbProvider.getDSL().selectFrom(PROGRAM_SEQUENCE_CHORD)
        .where(PROGRAM_SEQUENCE_CHORD.PROGRAM_ID.eq(voice.getProgramId()))
        .fetch()).stream().map(ProgramSequenceChord::getId)
      .filter((id) -> !existingVoiceChordIds.contains(id))
      .collect(Collectors.toSet());
    Collection<ProgramSequenceChordVoicing> voicings = Sets.newHashSet();
    for (var chordId : missingVoiceChordIds)
      voicings.add(createEmptyVoicing(db, voice.getProgramId(), voice.getId(), chordId));
    return voicings;
  }

  @Override
  public ProgramSequenceChordVoicing update(HubAccess access, UUID id, ProgramSequenceChordVoicing entity) throws ManagerException, JsonapiException, ValueException {
    DSLContext db = dbProvider.getDSL();
    requireAny("Same id", Objects.equals(id, entity.getId()));
    validate(entity);
    requireArtist(access);
    requireProgramModification(db, access, entity.getProgramId());
    requireNotExistsForChordAndVoice(db, entity);
    executeUpdate(db, PROGRAM_SEQUENCE_CHORD_VOICING, id, entity);
    return entity;
  }

  @Override
  public void destroy(HubAccess access, UUID id) throws ManagerException {
    DSLContext db = dbProvider.getDSL();
    requireArtist(access);

    if (!access.isTopLevel())
      requireExists("Voicing belongs to Program in Account you have access to", db.selectCount()
        .from(PROGRAM_SEQUENCE_CHORD_VOICING)
        .join(PROGRAM).on(PROGRAM.ID.eq(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_ID))
        .join(LIBRARY).on(LIBRARY.ID.eq(PROGRAM.LIBRARY_ID))
        .where(PROGRAM_SEQUENCE_CHORD_VOICING.ID.eq(id))
        .and(LIBRARY.ACCOUNT_ID.in(access.getAccountIds()))
        .fetchOne(0, int.class));

    db.deleteFrom(PROGRAM_SEQUENCE_CHORD_VOICING)
      .where(PROGRAM_SEQUENCE_CHORD_VOICING.ID.eq(id))
      .execute();
  }

  @Override
  public ProgramSequenceChordVoicing newInstance() {
    return new ProgramSequenceChordVoicing();
  }

  /**
   Validate data

   @param record to validate
   @throws ManagerException if invalid
   */
  public void validate(ProgramSequenceChordVoicing record) throws ManagerException {
    try {
      Values.require(record.getProgramId(), "Program ID");
      Values.require(record.getProgramVoiceId(), "Program Voice ID");
      Values.require(record.getProgramSequenceChordId(), "Sequence Chord ID");
      Values.require(record.getNotes(), "Notes are required");

    } catch (ValueException e) {
      throw new ManagerException(e);
    }
  }

  /**
   Creating a
   - new program voice
   - new chord via cloning
   - new chord
   creates (None) chord voicings for all sequence chords, and includes all created entities in API response
   https://www.pivotaltracker.com/story/show/182220689

   @param db context
   @param programId of voicing
   @param voiceId of voicing
   @param chordId of voicing
   @return empty voicing created
   @throws ManagerException on failure
   */
  private ProgramSequenceChordVoicing createEmptyVoicing(DSLContext db, UUID programId, UUID voiceId, UUID chordId) throws ManagerException {
    var voicing = new ProgramSequenceChordVoicing();
    voicing.setProgramId(programId);
    voicing.setProgramVoiceId(voiceId);
    voicing.setProgramSequenceChordId(chordId);
    voicing.setNotes(VOICING_NOTES_EMPTY);
    return modelFrom(ProgramSequenceChordVoicing.class, executeCreate(db, PROGRAM_SEQUENCE_CHORD_VOICING, voicing));
  }

  /**
   Cannot create/update a voicing to existing chord+voice
   https://www.pivotaltracker.com/story/show/182220689

   @param db      context
   @param voicing to test
   @throws ManagerException if already exists
   */
  private void requireNotExistsForChordAndVoice(DSLContext db, ProgramSequenceChordVoicing voicing) throws ManagerException {
    requireNotExists("existing voicing for this chord and voice", db.selectCount().from(PROGRAM_SEQUENCE_CHORD_VOICING)
      .where(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_SEQUENCE_CHORD_ID.eq(voicing.getProgramSequenceChordId()))
      .and(PROGRAM_SEQUENCE_CHORD_VOICING.PROGRAM_VOICE_ID.eq(voicing.getProgramVoiceId()))
      .and(PROGRAM_SEQUENCE_CHORD_VOICING.ID.ne(voicing.getId()))
      .fetchOne(0, int.class));
  }
}
