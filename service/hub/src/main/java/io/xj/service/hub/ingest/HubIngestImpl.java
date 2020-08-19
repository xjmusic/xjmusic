// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.ingest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.lib.entity.Entity;
import io.xj.lib.entity.EntityStore;
import io.xj.lib.entity.EntityStoreException;
import io.xj.lib.util.Value;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.dao.DAOException;
import io.xj.service.hub.dao.InstrumentDAO;
import io.xj.service.hub.dao.ProgramDAO;
import io.xj.service.hub.entity.Instrument;
import io.xj.service.hub.entity.InstrumentAudio;
import io.xj.service.hub.entity.InstrumentAudioChord;
import io.xj.service.hub.entity.InstrumentMeme;
import io.xj.service.hub.entity.InstrumentType;
import io.xj.service.hub.entity.Program;
import io.xj.service.hub.entity.ProgramMeme;
import io.xj.service.hub.entity.ProgramSequence;
import io.xj.service.hub.entity.ProgramSequenceBinding;
import io.xj.service.hub.entity.ProgramSequenceBindingMeme;
import io.xj.service.hub.entity.ProgramSequenceChord;
import io.xj.service.hub.entity.ProgramType;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 [#154350346] to ingest any combination of Programs, Instruments, or Libraries (with their Programs and Instruments)
 */
class HubIngestImpl implements HubIngest {
  private final HubAccess hubAccess;
  private final EntityStore store;

  @Inject
  public HubIngestImpl(
    @Assisted("hubAccess") HubAccess hubAccess,
    @Assisted("libraryIds") Set<UUID> sourceLibraryIds,
    @Assisted("programIds") Set<UUID> sourceProgramIds,
    @Assisted("instrumentIds") Set<UUID> sourceInstrumentIds,
    InstrumentDAO instrumentDAO,
    ProgramDAO programDAO,
    EntityStore entityStore
  ) throws HubIngestException {
    store = entityStore;
    try {
      List<UUID> libraryIds = Lists.newArrayList(sourceLibraryIds);
      List<UUID> programIds = Lists.newArrayList(sourceProgramIds);
      List<UUID> instrumentIds = Lists.newArrayList(sourceInstrumentIds);
      this.hubAccess = hubAccess;

      // library ids -> program and instrument ids; disregard library ids after this
      Value.put(programIds, programDAO.readIdsInLibraries(hubAccess, libraryIds));
      Value.put(instrumentIds, instrumentDAO.readIdsInLibraries(hubAccess, libraryIds));
      libraryIds.clear();

      // ingest programs
      store.putAll(programDAO.readManyWithChildEntities(hubAccess, programIds));

      // ingest instruments
      store.putAll(instrumentDAO.readManyWithChildEntities(hubAccess, instrumentIds));

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
  public Collection<Program> getAllPrograms() throws HubIngestException {
    return getAll(Program.class);
  }

  @Override
  public Collection<InstrumentAudioChord> getAllInstrumentAudioChords() throws HubIngestException {
    return getAll(InstrumentAudioChord.class);
  }

  @Override
  public Collection<InstrumentMeme> getAllInstrumentMemes() throws HubIngestException {
    return getAll(InstrumentMeme.class);
  }

  private <N extends Entity> Collection<N> getAll(Class<N> type) throws HubIngestException {
    try {
      return store.getAll(type);
    } catch (EntityStoreException e) {
      throw new HubIngestException(e);
    }
  }

  @Override
  public Collection<ProgramMeme> getAllProgramMemes() throws HubIngestException {
    return getAll(ProgramMeme.class);
  }

  @Override
  public Collection<ProgramSequence> getAllProgramSequences() throws HubIngestException {
    return getAll(ProgramSequence.class);
  }

  @Override
  public Collection<ProgramSequenceBinding> getAllProgramSequenceBindings() throws HubIngestException {
    return getAll(ProgramSequenceBinding.class);
  }

  @Override
  public Collection<ProgramSequenceBindingMeme> getAllProgramSequenceBindingMemes() throws HubIngestException {
    return getAll(ProgramSequenceBindingMeme.class);
  }

  @Override
  public Collection<ProgramSequenceChord> getAllProgramSequenceChords() throws HubIngestException {
    return getAll(ProgramSequenceChord.class);
  }

  @Override
  public Collection<Instrument> getAllInstruments() throws HubIngestException {
    return getAll(Instrument.class);
  }

  @Override
  public Collection<Program> getProgramsOfType(ProgramType type) throws HubIngestException {
    return getAllPrograms().stream()
      .filter(program -> program.getType().equals(type))
      .collect(Collectors.toList());
  }

  @Override
  public HubAccess getHubAccess() {
    return hubAccess;
  }

  @Override
  public Collection<Instrument> getInstrumentsOfType(InstrumentType type) throws HubIngestException {
    return getAllInstruments().stream()
      .filter(instrument -> instrument.getType().equals(type))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<Entity> getAllEntities() {
    return ImmutableList.<Entity>builder().addAll(store.getAll()).build();
  }

  @Override
  public Collection<ProgramMeme> getMemes(Program program) throws HubIngestException {
    return getAllProgramMemes().stream()
      .filter(m -> m.getProgramId().equals(program.getId()))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<InstrumentMeme> getMemes(Instrument instrument) throws HubIngestException {
    return getAllInstrumentMemes().stream()
      .filter(m -> m.getInstrumentId().equals(instrument.getId()))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<InstrumentAudioChord> getChords(InstrumentAudio audio) throws HubIngestException {
    return getAllInstrumentAudioChords().stream()
      .filter(e -> e.getInstrumentAudioId().equals(audio.getId()))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<ProgramSequenceChord> getChords(ProgramSequence sequence) throws HubIngestException {
    return getAllProgramSequenceChords().stream()
      .filter(e -> e.getProgramSequenceId().equals(sequence.getId()))
      .collect(Collectors.toList());
  }

  public Collection<ProgramSequenceBindingMeme> getMemes(ProgramSequenceBinding programSequenceBinding) throws HubIngestException {
    return getAllProgramSequenceBindingMemes().stream()
      .filter(m -> m.getProgramSequenceBindingId().equals(programSequenceBinding.getId()))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<ProgramSequenceBinding> getSequenceBindings(Program program) throws HubIngestException {
    return getAllProgramSequenceBindings().stream()
      .filter(m -> m.getProgramId().equals(program.getId()))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<ProgramSequence> getSequences(Program program) throws HubIngestException {
    return getAllProgramSequences().stream()
      .filter(m -> m.getProgramId().equals(program.getId()))
      .collect(Collectors.toList());
  }

  /**
   Get a member from a map of entities, or else throw an exception

   @param type of member to get
   @param id   of member to get
   @param <N>  type of entity
   @return entity from map
   @throws HubIngestException if no such entity exists
   */
  private <N extends Entity> N getOrThrow(Class<N> type, UUID id) throws HubIngestException {
    try {
      return store.get(type, id)
        .orElseThrow(() -> new HubIngestException(String.format("No such %s[%s]",
          type.getSimpleName(), id)));
    } catch (EntityStoreException e) {
      throw new HubIngestException(e);
    }
  }
}
