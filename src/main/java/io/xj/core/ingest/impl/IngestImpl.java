// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.ingest.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.core.access.Access;
import io.xj.core.cache.entity.EntityCache;
import io.xj.core.dao.InstrumentDAO;
import io.xj.core.dao.ProgramDAO;
import io.xj.core.entity.Entity;
import io.xj.core.entity.MemeEntity;
import io.xj.core.exception.CoreException;
import io.xj.core.ingest.Ingest;
import io.xj.core.model.ChainBinding;
import io.xj.core.model.Instrument;
import io.xj.core.model.InstrumentAudio;
import io.xj.core.model.InstrumentAudioChord;
import io.xj.core.model.InstrumentAudioEvent;
import io.xj.core.model.InstrumentMeme;
import io.xj.core.model.InstrumentType;
import io.xj.core.model.Program;
import io.xj.core.model.ProgramSequencePatternEvent;
import io.xj.core.model.ProgramMeme;
import io.xj.core.model.ProgramSequencePattern;
import io.xj.core.model.ProgramSequence;
import io.xj.core.model.ProgramSequenceBinding;
import io.xj.core.model.ProgramSequenceBindingMeme;
import io.xj.core.model.ProgramSequenceChord;
import io.xj.core.model.ProgramVoiceTrack;
import io.xj.core.model.ProgramType;
import io.xj.core.model.ProgramVoice;
import io.xj.core.model.SegmentChoiceArrangementPick;
import io.xj.core.util.Value;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 [#154350346] to ingest any combination of Programs, Instruments, or Libraries (with their Programs and Instruments)
 */
public class IngestImpl implements Ingest {
  private final Access access;
  private final EntityCache<Program> programs;
  private final EntityCache<InstrumentAudio> instrumentAudios;
  private final EntityCache<InstrumentAudioChord> instrumentAudioChords;
  private final EntityCache<InstrumentAudioEvent> instrumentAudioEvents;
  private final EntityCache<InstrumentMeme> instrumentMemes;
  private final EntityCache<ProgramSequencePatternEvent> programEvents;
  private final EntityCache<ProgramMeme> programMemes;
  private final EntityCache<ProgramSequencePattern> programPatterns;
  private final EntityCache<ProgramSequence> programSequences;
  private final EntityCache<ProgramSequenceBinding> programSequenceBindings;
  private final EntityCache<ProgramSequenceBindingMeme> programSequenceBindingMemes;
  private final EntityCache<ProgramSequenceChord> programSequenceChords;
  private final EntityCache<ProgramVoiceTrack> programTracks;
  private final EntityCache<ProgramVoice> programVoices;
  private final EntityCache<Instrument> instruments;

  @Inject
  public IngestImpl(
    @Assisted("access") Access access,
    @Assisted("bindings") Collection<ChainBinding> bindings,
    InstrumentDAO instrumentDAO,
    ProgramDAO programDAO
  ) throws CoreException {
    this.access = access;
    this.instrumentAudioChords = new EntityCache();
    this.instrumentAudios = new EntityCache();
    this.instrumentAudioEvents = new EntityCache();
    this.instruments = new EntityCache();
    this.instrumentMemes = new EntityCache();
    this.programs = new EntityCache();
    this.programEvents = new EntityCache();
    this.programMemes = new EntityCache();
    this.programPatterns = new EntityCache();
    this.programSequenceBindings = new EntityCache();
    this.programSequenceBindingMemes = new EntityCache();
    this.programSequenceChords = new EntityCache();
    this.programSequences = new EntityCache();
    this.programTracks = new EntityCache();
    this.programVoices = new EntityCache();

    Collection<UUID> programIds = Lists.newArrayList();
    Collection<UUID> instrumentIds = Lists.newArrayList();
    Collection<UUID> libraryIds = Lists.newArrayList();

    // get all the binding target ids into respective lists
    bindings.forEach(chainBinding -> {
      switch (chainBinding.getType()) {
        case Library:
          Value.put(libraryIds, chainBinding.getTargetId());
          break;
        case Program:
          Value.put(programIds, chainBinding.getTargetId());
          break;
        case Instrument:
          Value.put(instrumentIds, chainBinding.getTargetId());
          break;
      }
    });

    // library ids -> program and instrument ids; disregard library ids after this
    Value.put(programIds, programDAO.readIdsInLibraries(access, libraryIds));
    Value.put(instrumentIds, instrumentDAO.readIdsInLibraries(access, libraryIds));
    libraryIds.clear();

    // ingest programs
    programDAO.readManyWithChildEntities(access, programIds).forEach(entity -> {
      switch (entity.getClass().getSimpleName()) {
        case "Program":
          programs.add((Program) entity);
          break;
        case "ProgramSequencePatternEvent":
          programEvents.add((ProgramSequencePatternEvent) entity);
          break;
        case "ProgramMeme":
          programMemes.add((ProgramMeme) entity);
          break;
        case "ProgramSequencePattern":
          programPatterns.add((ProgramSequencePattern) entity);
          break;
        case "ProgramSequenceBinding":
          programSequenceBindings.add((ProgramSequenceBinding) entity);
          break;
        case "ProgramSequenceBindingMeme":
          programSequenceBindingMemes.add((ProgramSequenceBindingMeme) entity);
          break;
        case "ProgramSequenceChord":
          programSequenceChords.add((ProgramSequenceChord) entity);
          break;
        case "ProgramSequence":
          programSequences.add((ProgramSequence) entity);
          break;
        case "ProgramVoiceTrack":
          programTracks.add((ProgramVoiceTrack) entity);
          break;
        case "ProgramVoice":
          programVoices.add((ProgramVoice) entity);
          break;
      }
    });

    // ingest instruments
    instrumentDAO.readManyWithChildEntities(access, instrumentIds).forEach(entity -> {
      switch (entity.getClass().getSimpleName()) {
        case "Instrument":
          instruments.add((Instrument) entity);
          break;
        case "InstrumentMeme":
          instrumentMemes.add((InstrumentMeme) entity);
          break;
        case "InstrumentAudioChord":
          instrumentAudioChords.add((InstrumentAudioChord) entity);
          break;
        case "InstrumentAudioEvent":
          instrumentAudioEvents.add((InstrumentAudioEvent) entity);
          break;
        case "InstrumentAudio":
          instrumentAudios.add((InstrumentAudio) entity);
          break;
      }
    });
  }

  @Override
  public Collection<Long> getAvailableOffsets(ProgramSequenceBinding sequenceBinding) {
    return programSequenceBindings.getAll().stream()
      .filter(psb -> psb.getProgramId().equals(sequenceBinding.getProgramId()))
      .map(ProgramSequenceBinding::getOffset)
      .distinct()
      .collect(Collectors.toList());
  }

  @Override
  public InstrumentAudioChord getInstrumentAudioChord(UUID id) throws CoreException {
    return this.instrumentAudioChords.get(id);
  }

  @Override
  public InstrumentAudio getInstrumentAudio(UUID id) throws CoreException {
    return this.instrumentAudios.get(id);
  }

  @Override
  public InstrumentAudioEvent getInstrumentAudioEvent(UUID id) throws CoreException {
    return this.instrumentAudioEvents.get(id);
  }

  @Override
  public Instrument getInstrument(UUID id) throws CoreException {
    return this.instruments.get(id);
  }

  @Override
  public InstrumentMeme getInstrumentMeme(UUID id) throws CoreException {
    return this.instrumentMemes.get(id);
  }

  @Override
  public Program getProgram(UUID id) throws CoreException {
    return this.programs.get(id);
  }

  @Override
  public ProgramSequencePatternEvent getProgramEvent(UUID id) throws CoreException {
    return this.programEvents.get(id);
  }

  @Override
  public ProgramMeme getProgramMeme(UUID id) throws CoreException {
    return this.programMemes.get(id);
  }

  @Override
  public ProgramSequencePattern getProgramPattern(UUID id) throws CoreException {
    return this.programPatterns.get(id);
  }

  @Override
  public ProgramSequenceBinding getProgramSequenceBinding(UUID id) throws CoreException {
    return this.programSequenceBindings.get(id);
  }

  @Override
  public ProgramSequenceBindingMeme getProgramSequenceBindingMeme(UUID id) throws CoreException {
    return this.programSequenceBindingMemes.get(id);
  }

  @Override
  public ProgramSequenceChord getProgramSequenceChord(UUID id) throws CoreException {
    return this.programSequenceChords.get(id);
  }

  @Override
  public ProgramSequence getProgramSequence(UUID id) throws CoreException {
    return this.programSequences.get(id);
  }

  @Override
  public ProgramVoiceTrack getProgramTrack(UUID id) throws CoreException {
    return this.programTracks.get(id);
  }

  @Override
  public ProgramVoice getProgramVoice(UUID id) throws CoreException {
    return this.programVoices.get(id);
  }


  @Override
  public Collection<Program> getAllPrograms() {
    return programs.getAll();
  }

  @Override
  public Collection<InstrumentAudio> getAllInstrumentAudios() {
    return instrumentAudios.getAll();
  }

  @Override
  public Collection<InstrumentAudioChord> getAllInstrumentAudioChords() {
    return instrumentAudioChords.getAll();
  }

  @Override
  public Collection<InstrumentAudioEvent> getAllInstrumentAudioEvents() {
    return instrumentAudioEvents.getAll();
  }

  @Override
  public Collection<InstrumentMeme> getAllInstrumentMemes() {
    return instrumentMemes.getAll();
  }

  @Override
  public Collection<ProgramSequencePatternEvent> getAllProgramEvents() {
    return programEvents.getAll();
  }

  @Override
  public Collection<ProgramMeme> getAllProgramMemes() {
    return programMemes.getAll();
  }

  @Override
  public Collection<ProgramSequencePattern> getAllProgramPatterns() {
    return programPatterns.getAll();
  }

  @Override
  public Collection<ProgramSequence> getAllProgramSequences() {
    return programSequences.getAll();
  }

  @Override
  public Collection<ProgramSequenceBinding> getAllProgramSequenceBindings() {
    return programSequenceBindings.getAll();
  }

  @Override
  public Collection<ProgramSequenceBindingMeme> getAllProgramSequenceBindingMemes() {
    return programSequenceBindingMemes.getAll();
  }

  @Override
  public Collection<ProgramSequenceChord> getAllProgramSequenceChords() {
    return programSequenceChords.getAll();
  }

  @Override
  public Collection<ProgramVoiceTrack> getAllProgramTracks() {
    return programTracks.getAll();
  }

  @Override
  public Collection<ProgramVoice> getAllProgramVoices() {
    return programVoices.getAll();
  }

  @Override
  public Collection<Instrument> getAllInstruments() {
    return instruments.getAll();
  }

  @Override
  public Collection<Program> getProgramsOfType(ProgramType type) {
    return getAllPrograms().stream()
      .filter(program -> program.getType().equals(type))
      .collect(Collectors.toList());
  }

  @Override
  public Access getAccess() {
    return access;
  }

  @Override
  public Collection<Instrument> getInstrumentsOfType(InstrumentType type) {
    return getAllInstruments().stream()
      .filter(instrument -> instrument.getType().equals(type))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<Entity> getAllEntities() {
    return ImmutableList.<Entity>builder()
      .addAll(instrumentAudioChords.getAll())
      .addAll(instrumentAudios.getAll())
      .addAll(instrumentAudioEvents.getAll())
      .addAll(instruments.getAll())
      .addAll(instrumentMemes.getAll())
      .addAll(programs.getAll())
      .addAll(programEvents.getAll())
      .addAll(programMemes.getAll())
      .addAll(programPatterns.getAll())
      .addAll(programSequenceBindings.getAll())
      .addAll(programSequenceBindingMemes.getAll())
      .addAll(programSequenceChords.getAll())
      .addAll(programSequences.getAll())
      .addAll(programTracks.getAll())
      .addAll(programVoices.getAll())
      .build();
  }

  @Override
  public String toString() {
    return Entity.histogramString(getAllEntities());
  }

  @Override
  public Collection<ProgramMeme> getMemes(Program program) {
    return programMemes.getAll().stream()
      .filter(m -> m.getProgramId().equals(program.getId()))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<ProgramSequencePatternEvent> getEvents(Program program) {
    return programEvents.getAll().stream()
      .filter(m -> m.getProgramId().equals(program.getId()))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<ProgramSequencePatternEvent> getEvents(ProgramSequencePattern programPattern) {
    return programEvents.getAll().stream()
      .filter(m -> m.getProgramSequencePatternId().equals(programPattern.getId()))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<InstrumentMeme> getMemes(Instrument instrument) {
    return instrumentMemes.getAll().stream()
      .filter(m -> m.getInstrumentId().equals(instrument.getId()))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<InstrumentAudio> getAudiosForInstrumentId(UUID uuid) {
    return instrumentAudios.getAll().stream()
      .filter(a -> a.getInstrumentId().equals(uuid))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<InstrumentAudio> getAudios(Instrument instrument) {
    return getAudiosForInstrumentId(instrument.getId());
  }

  @Override
  public Collection<InstrumentAudioEvent> getEvents(InstrumentAudio audio) {
    return instrumentAudioEvents.getAll().stream()
      .filter(e -> e.getInstrumentAudioId().equals(audio.getId()))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<InstrumentAudioChord> getChords(InstrumentAudio audio) {
    return instrumentAudioChords.getAll().stream()
      .filter(e -> e.getInstrumentAudioId().equals(audio.getId()))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<ProgramSequenceChord> getChords(ProgramSequence sequence) {
    return programSequenceChords.getAll().stream()
      .filter(e -> e.getProgramSequenceId().equals(sequence.getId()))
      .collect(Collectors.toList());
  }

  public Collection<ProgramSequenceBindingMeme> getMemes(ProgramSequenceBinding programSequenceBinding) {
    return programSequenceBindingMemes.getAll().stream()
      .filter(m -> m.getProgramSequenceBindingId().equals(programSequenceBinding.getId()))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<ProgramSequenceBinding> getSequenceBindings(Program program) {
    return programSequenceBindings.getAll().stream()
      .filter(m -> m.getProgramId().equals(program.getId()))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<ProgramSequence> getSequences(Program program) {
    return programSequences.getAll().stream()
      .filter(m -> m.getProgramId().equals(program.getId()))
      .collect(Collectors.toList());
  }

  @Override
  public ProgramSequence getSequence(ProgramSequenceBinding sequenceBinding) throws CoreException {
    return programSequences.get(sequenceBinding.getProgramSequenceId());
  }

  @Override
  public InstrumentAudio getAudio(SegmentChoiceArrangementPick pick) throws CoreException {
    return instrumentAudios.get(pick.getInstrumentAudioId());
  }

  @Override
  public Collection<InstrumentAudioEvent> getFirstEventsOfAudiosOfInstrument(Instrument instrument) {
    Map<String, InstrumentAudioEvent> result = Maps.newHashMap();
    getAudios(instrument).forEach(audio ->
      getEvents(audio).stream().filter(search -> search.getInstrumentAudioId().equals(audio.getId())).forEach(audioEvent -> {
        String key = audioEvent.getInstrumentAudioId().toString();
        if (result.containsKey(key)) {
          if (audioEvent.getPosition() < result.get(key).getPosition()) {
            result.put(key, audioEvent);
          }
        } else {
          result.put(key, audioEvent);
        }
      }));
    return result.values();
  }

  @Override
  public Collection<MemeEntity> getMemesAtBeginning(Program program) {
    Map<String, MemeEntity> memes = Maps.newHashMap();

    // add sequence memes
    getMemes(program).forEach((meme ->
      memes.put(meme.getName(), meme)));

    // add sequence binding memes
    getProgramSequenceBindingsAtOffset(program, 0L).forEach(sequenceBinding ->
      getMemes(sequenceBinding).forEach(meme ->
        memes.put(meme.getName(), meme)));

    return memes.values();
  }

  @Override
  public Collection<ProgramSequenceBinding> getProgramSequenceBindingsAtOffset(Program program, Long offset) {
    return getAllProgramSequenceBindings().stream()
      .filter(psb -> psb.getProgramId().equals(program.getId()) && psb.getOffset().equals(offset))
      .collect(Collectors.toList());
  }

  @Override
  public ProgramVoice getVoice(ProgramSequencePatternEvent event) throws CoreException {
    return programVoices.get(getTrack(event).getProgramVoiceId());
  }

  @Override
  public ProgramVoiceTrack getTrack(ProgramSequencePatternEvent event) throws CoreException {
    return programTracks.get(event.getProgramVoiceTrackId());
  }

  @Override
  public Collection<ProgramVoice> getVoices(Program program) {
    return programVoices.getAll().stream()
      .filter(m -> m.getProgramId().equals(program.getId()))
      .collect(Collectors.toList());
  }

}
