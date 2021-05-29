// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.ingest;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.Instrument;
import io.xj.InstrumentAudio;
import io.xj.InstrumentAudioChord;
import io.xj.InstrumentMeme;
import io.xj.Program;
import io.xj.ProgramMeme;
import io.xj.ProgramSequence;
import io.xj.ProgramSequenceBinding;
import io.xj.ProgramSequenceBindingMeme;
import io.xj.ProgramSequenceChord;
import io.xj.lib.entity.EntityStore;
import io.xj.lib.entity.EntityStoreException;
import io.xj.lib.util.Value;
import io.xj.hub.access.HubAccess;
import io.xj.hub.dao.DAOException;
import io.xj.hub.dao.InstrumentDAO;
import io.xj.hub.dao.ProgramDAO;

import java.util.Collection;
import java.util.List;
import java.util.Set;
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
    @Assisted("libraryIds") Set<String> sourceLibraryIds,
    @Assisted("programIds") Set<String> sourceProgramIds,
    @Assisted("instrumentIds") Set<String> sourceInstrumentIds,
    InstrumentDAO instrumentDAO,
    ProgramDAO programDAO,
    EntityStore entityStore
  ) throws HubIngestException {
    store = entityStore;
    try {
      List<String> libraryIds = Lists.newArrayList(sourceLibraryIds);
      List<String> programIds = Lists.newArrayList(sourceProgramIds);
      List<String> instrumentIds = Lists.newArrayList(sourceInstrumentIds);
      this.hubAccess = hubAccess;

      // library ids -> program and instrument ids; disregard library ids after this
      Value.put(programIds, programDAO.readIdsInLibraries(hubAccess, libraryIds));
      Value.put(instrumentIds, instrumentDAO.readIdsInLibraries(hubAccess, libraryIds));
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
  public Instrument getInstrument(String id) throws HubIngestException {
    return getOrThrow(Instrument.class, id);
  }

  @Override
  public Program getProgram(String id) throws HubIngestException {
    return getOrThrow(Program.class, id);
  }

  @Override
  public Collection<Program> getAllPrograms() {
    return getAll(Program.class);
  }

  @Override
  public Collection<InstrumentAudioChord> getAllInstrumentAudioChords() {
    return getAll(InstrumentAudioChord.class);
  }

  @Override
  public Collection<InstrumentMeme> getAllInstrumentMemes() {
    return getAll(InstrumentMeme.class);
  }

  private <N> Collection<N> getAll(Class<N> type) {
    return store.getAll(type);
  }

  @Override
  public Collection<ProgramMeme> getAllProgramMemes() {
    return getAll(ProgramMeme.class);
  }

  @Override
  public Collection<ProgramSequence> getAllProgramSequences() {
    return getAll(ProgramSequence.class);
  }

  @Override
  public Collection<ProgramSequenceBinding> getAllProgramSequenceBindings() {
    return getAll(ProgramSequenceBinding.class);
  }

  @Override
  public Collection<ProgramSequenceBindingMeme> getAllProgramSequenceBindingMemes() {
    return getAll(ProgramSequenceBindingMeme.class);
  }

  @Override
  public Collection<ProgramSequenceChord> getAllProgramSequenceChords() {
    return getAll(ProgramSequenceChord.class);
  }

  @Override
  public Collection<Instrument> getAllInstruments() {
    return getAll(Instrument.class);
  }

  @Override
  public Collection<Program> getProgramsOfType(Program.Type type) {
    return getAllPrograms().stream()
      .filter(program -> program.getType().equals(type))
      .collect(Collectors.toList());
  }

  @Override
  public HubAccess getHubAccess() {
    return hubAccess;
  }

  @Override
  public Collection<Instrument> getInstrumentsOfType(Instrument.Type type) {
    return getAllInstruments().stream()
      .filter(instrument -> instrument.getType().equals(type))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<Object> getAllEntities() {
    return ImmutableList.builder().addAll(store.getAll()).build();
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
  public Collection<InstrumentAudioChord> getChords(InstrumentAudio audio) {
    return getAllInstrumentAudioChords().stream()
      .filter(e -> e.getInstrumentAudioId().equals(audio.getId()))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<ProgramSequenceChord> getChords(ProgramSequence sequence) {
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
  public Collection<ProgramSequenceBinding> getSequenceBindings(Program program) {
    return getAllProgramSequenceBindings().stream()
      .filter(m -> m.getProgramId().equals(program.getId()))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<ProgramSequence> getSequences(Program program) {
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
  private <N> N getOrThrow(Class<N> type, String id) throws HubIngestException {
    try {
      return store.get(type, id)
        .orElseThrow(() -> new HubIngestException(String.format("No such %s[%s]",
          type.getSimpleName(), id)));
    } catch (EntityStoreException e) {
      throw new HubIngestException(e);
    }
  }
}
