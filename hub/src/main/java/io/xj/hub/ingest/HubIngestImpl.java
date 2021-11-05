// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.ingest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.hub.access.HubAccess;
import io.xj.hub.dao.*;
import io.xj.hub.enums.ContentBindingType;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.*;
import io.xj.lib.entity.EntityStore;
import io.xj.lib.entity.EntityStoreException;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.util.Values;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 [#154350346] to ingest any combination of Programs, Instruments, or Libraries (with their Programs and Instruments)
 */
class HubIngestImpl implements HubIngest {
  private final HubAccess hubAccess;
  private final EntityStore store;
  private final JsonProvider jsonProvider;

  @Inject
  public HubIngestImpl(
    @Assisted("hubAccess") HubAccess hubAccess,
    @Assisted("templateId") UUID templateId,
    EntityStore entityStore,
    InstrumentDAO instrumentDAO,
    JsonProvider jsonProvider,
    ProgramDAO programDAO,
    TemplateDAO templateDAO,
    TemplateBindingDAO templateBindingDAO
  ) throws HubIngestException {
    store = entityStore;
    this.jsonProvider = jsonProvider;
    this.hubAccess = hubAccess;
    try {
      store.put(templateDAO.readOne(hubAccess, templateId));
      var bindings = store.putAll(templateBindingDAO.readMany(hubAccess, ImmutableList.of(templateId)));
      List<UUID> libraryIds = bindings.stream()
        .filter(b -> ContentBindingType.Library.equals(b.getType()))
        .map(TemplateBinding::getTargetId)
        .collect(Collectors.toList());
      List<UUID> programIds = bindings.stream()
        .filter(b -> ContentBindingType.Program.equals(b.getType()))
        .map(TemplateBinding::getTargetId)
        .collect(Collectors.toList());
      List<UUID> instrumentIds = bindings.stream()
        .filter(b -> ContentBindingType.Instrument.equals(b.getType()))
        .map(TemplateBinding::getTargetId)
        .collect(Collectors.toList());

      // library ids -> program and instrument ids; disregard library ids after this
      Values.put(programIds, programDAO.readIdsInLibraries(hubAccess, libraryIds));
      Values.put(instrumentIds, instrumentDAO.readIdsInLibraries(hubAccess, libraryIds));
      libraryIds.clear();

      // ingest programs
      for (Object o : programDAO.readManyWithChildEntities(hubAccess, programIds))
        store.put(o);

      // ingest instruments
      for (Object n : instrumentDAO.readManyWithChildEntities(hubAccess, instrumentIds))
        store.put(n);

    } catch (DAOException | EntityStoreException e) {
      throw new HubIngestException(e);
    }
  }

  @Override
  public Instrument getInstrument(UUID id) throws HubIngestException {
    return getOrThrow(Instrument.class, id);
  }

  @Override
  public Program getProgram(UUID id) throws HubIngestException {
    return getOrThrow(Program.class, id);
  }

  @Override
  public Collection<Program> getAllPrograms() {
    return getAll(Program.class);
  }

  private <N> Collection<N> getAll(Class<N> type) {
    return store.getAll(type);
  }

  @Override
  public Collection<Instrument> getAllInstruments() {
    return getAll(Instrument.class);
  }

  @Override
  public Collection<Program> getProgramsOfType(ProgramType type) {
    return getAllPrograms().stream()
      .filter(program -> program.getType().equals(type))
      .collect(Collectors.toList());
  }

  @Override
  public HubAccess getHubAccess() {
    return hubAccess;
  }

  @Override
  public Collection<Instrument> getInstrumentsOfType(InstrumentType type) {
    return getAllInstruments().stream()
      .filter(instrument -> instrument.getType().equals(type))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<Object> getAllEntities() {
    return ImmutableList.builder().addAll(store.getAll()).build();
  }

  @Override
  public String toJSON() throws JsonProcessingException {
    var entities = getAllEntities();
    return jsonProvider.getMapper().writeValueAsString(new HubContentPayload()
      .setTemplates(entities.stream()
        .filter(ent -> Template.class.equals(ent.getClass()))
        .map(ent -> (Template) ent)
        .collect(Collectors.toList()))
      .setTemplateBindings(entities.stream()
        .filter(ent -> TemplateBinding.class.equals(ent.getClass()))
        .map(ent -> (TemplateBinding) ent)
        .collect(Collectors.toList()))
      .setInstruments(entities.stream()
        .filter(ent -> Instrument.class.equals(ent.getClass()))
        .map(ent -> (Instrument) ent)
        .collect(Collectors.toList()))
      .setInstrumentAudios(entities.stream()
        .filter(ent -> InstrumentAudio.class.equals(ent.getClass()))
        .map(ent -> (InstrumentAudio) ent)
        .collect(Collectors.toList()))
      .setInstrumentMemes(entities.stream()
        .filter(ent -> InstrumentMeme.class.equals(ent.getClass()))
        .map(ent -> (InstrumentMeme) ent)
        .collect(Collectors.toList()))
      .setPrograms(entities.stream()
        .filter(ent -> Program.class.equals(ent.getClass()))
        .map(ent -> (Program) ent)
        .collect(Collectors.toList()))
      .setProgramMemes(entities.stream()
        .filter(ent -> ProgramMeme.class.equals(ent.getClass()))
        .map(ent -> (ProgramMeme) ent)
        .collect(Collectors.toList()))
      .setProgramSequences(entities.stream()
        .filter(ent -> ProgramSequence.class.equals(ent.getClass()))
        .map(ent -> (ProgramSequence) ent)
        .collect(Collectors.toList()))
      .setProgramSequenceBindings(entities.stream()
        .filter(ent -> ProgramSequenceBinding.class.equals(ent.getClass()))
        .map(ent -> (ProgramSequenceBinding) ent)
        .collect(Collectors.toList()))
      .setProgramSequenceBindingMemes(entities.stream()
        .filter(ent -> ProgramSequenceBindingMeme.class.equals(ent.getClass()))
        .map(ent -> (ProgramSequenceBindingMeme) ent)
        .collect(Collectors.toList()))
      .setProgramSequenceChords(entities.stream()
        .filter(ent -> ProgramSequenceChord.class.equals(ent.getClass()))
        .map(ent -> (ProgramSequenceChord) ent)
        .collect(Collectors.toList()))
      .setProgramSequenceChordVoicings(entities.stream()
        .filter(ent -> ProgramSequenceChordVoicing.class.equals(ent.getClass()))
        .map(ent -> (ProgramSequenceChordVoicing) ent)
        .collect(Collectors.toList()))
      .setProgramSequencePatterns(entities.stream()
        .filter(ent -> ProgramSequencePattern.class.equals(ent.getClass()))
        .map(ent -> (ProgramSequencePattern) ent)
        .collect(Collectors.toList()))
      .setProgramSequencePatternEvents(entities.stream()
        .filter(ent -> ProgramSequencePatternEvent.class.equals(ent.getClass()))
        .map(ent -> (ProgramSequencePatternEvent) ent)
        .collect(Collectors.toList()))
      .setProgramVoices(entities.stream()
        .filter(ent -> ProgramVoice.class.equals(ent.getClass()))
        .map(ent -> (ProgramVoice) ent)
        .collect(Collectors.toList()))
      .setProgramVoiceTracks(entities.stream()
        .filter(ent -> ProgramVoiceTrack.class.equals(ent.getClass()))
        .map(ent -> (ProgramVoiceTrack) ent)
        .collect(Collectors.toList())));
  }

  /**
   Get a member from a map of entities, or else throw an exception

   @param type of member to get
   @param id   of member to get
   @param <N>  type of entity
   @return entity from map
   @throws HubIngestException if no such entity exists
   */
  private <N> N getOrThrow(Class<N> type, UUID id) throws HubIngestException {
    try {
      return store.get(type, id)
        .orElseThrow(() -> new HubIngestException(String.format("No such %s[%s]",
          type.getSimpleName(), id)));
    } catch (EntityStoreException e) {
      throw new HubIngestException(e);
    }
  }
}
