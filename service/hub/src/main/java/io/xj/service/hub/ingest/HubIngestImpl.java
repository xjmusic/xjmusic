// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.ingest;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.lib.entity.Entity;
import io.xj.lib.entity.EntityCache;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.MemeEntity;
import io.xj.lib.util.Text;
import io.xj.lib.util.Value;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.dao.DAOException;
import io.xj.service.hub.dao.InstrumentDAO;
import io.xj.service.hub.dao.ProgramDAO;
import io.xj.service.hub.entity.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 [#154350346] to ingest any combination of Programs, Instruments, or Libraries (with their Programs and Instruments)
 */
class HubIngestImpl implements HubIngest {
  private final HubAccess hubAccess;
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
  public HubIngestImpl(
    @Assisted("hubAccess") HubAccess hubAccess,
    @Assisted("libraryIds") Set<UUID> sourceLibraryIds,
    @Assisted("programIds") Set<UUID> sourceProgramIds,
    @Assisted("instrumentIds") Set<UUID> sourceInstrumentIds,
    InstrumentDAO instrumentDAO,
    ProgramDAO programDAO
  ) throws HubIngestException {
    try {
      List<UUID> libraryIds = Lists.newArrayList(sourceLibraryIds);
      List<UUID> programIds = Lists.newArrayList(sourceProgramIds);
      List<UUID> instrumentIds = Lists.newArrayList(sourceInstrumentIds);
      this.hubAccess = hubAccess;
      this.instrumentAudioChords = new EntityCache<>();
      this.instrumentAudios = new EntityCache<>();
      this.instrumentAudioEvents = new EntityCache<>();
      this.instruments = new EntityCache<>();
      this.instrumentMemes = new EntityCache<>();
      this.programs = new EntityCache<>();
      this.programEvents = new EntityCache<>();
      this.programMemes = new EntityCache<>();
      this.programPatterns = new EntityCache<>();
      this.programSequenceBindings = new EntityCache<>();
      this.programSequenceBindingMemes = new EntityCache<>();
      this.programSequenceChords = new EntityCache<>();
      this.programSequences = new EntityCache<>();
      this.programTracks = new EntityCache<>();
      this.programVoices = new EntityCache<>();

      // library ids -> program and instrument ids; disregard library ids after this
      Value.put(programIds, programDAO.readIdsInLibraries(hubAccess, libraryIds));
      Value.put(instrumentIds, instrumentDAO.readIdsInLibraries(hubAccess, libraryIds));
      libraryIds.clear();

      // ingest programs
      programDAO.readManyWithChildEntities(hubAccess, programIds).forEach(entity -> {
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
      instrumentDAO.readManyWithChildEntities(hubAccess, instrumentIds).forEach(entity -> {
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
    } catch (DAOException e) {
      throw new HubIngestException(e);
    }
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
  public InstrumentAudioChord getInstrumentAudioChord(UUID id) throws HubIngestException {
    return getOrThrow(instrumentAudioChords, id);
  }

  @Override
  public InstrumentAudio getInstrumentAudio(UUID id) throws HubIngestException {
    return getOrThrow(instrumentAudios, id);
  }

  @Override
  public InstrumentAudioEvent getInstrumentAudioEvent(UUID id) throws HubIngestException {
    return getOrThrow(instrumentAudioEvents, id);
  }

  @Override
  public Instrument getInstrument(UUID id) throws HubIngestException {
    return getOrThrow(instruments, id);
  }

  @Override
  public InstrumentMeme getInstrumentMeme(UUID id) throws HubIngestException {
    return getOrThrow(instrumentMemes, id);
  }

  @Override
  public Program getProgram(UUID id) throws HubIngestException {
    return getOrThrow(programs, id);
  }

  @Override
  public ProgramSequencePatternEvent getProgramEvent(UUID id) throws HubIngestException {
    return getOrThrow(programEvents, id);
  }

  @Override
  public ProgramMeme getProgramMeme(UUID id) throws HubIngestException {
    return getOrThrow(programMemes, id);
  }

  @Override
  public ProgramSequencePattern getProgramPattern(UUID id) throws HubIngestException {
    return getOrThrow(programPatterns, id);
  }

  @Override
  public ProgramSequenceBinding getProgramSequenceBinding(UUID id) throws HubIngestException {
    return getOrThrow(programSequenceBindings, id);
  }

  @Override
  public ProgramSequenceBindingMeme getProgramSequenceBindingMeme(UUID id) throws HubIngestException {
    return getOrThrow(programSequenceBindingMemes, id);
  }

  @Override
  public ProgramSequenceChord getProgramSequenceChord(UUID id) throws HubIngestException {
    return getOrThrow(programSequenceChords, id);
  }

  @Override
  public ProgramSequence getProgramSequence(UUID id) throws HubIngestException {
    return getOrThrow(programSequences, id);
  }

  @Override
  public ProgramVoiceTrack getProgramTrack(UUID id) throws HubIngestException {
    return getOrThrow(programTracks, id);
  }

  @Override
  public ProgramVoice getProgramVoice(UUID id) throws HubIngestException {
    return getOrThrow(programVoices, id);
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
    Multiset<String> entityHistogram = ConcurrentHashMultiset.create();
    getAllEntities().forEach((Object obj) -> entityHistogram.add(Text.getSimpleName(obj)));
    List<String> descriptors = Lists.newArrayList();
    Collection<String> names = Ordering.from(String.CASE_INSENSITIVE_ORDER).sortedCopy(entityHistogram.elementSet());
    names.forEach((String name) -> descriptors.add(String.format("%d %s", entityHistogram.count(name), name)));
    return String.join(", ", descriptors);
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
  public ProgramSequence getSequence(ProgramSequenceBinding sequenceBinding) throws HubIngestException {
    return getOrThrow(programSequences, sequenceBinding.getProgramSequenceId());
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
  public ProgramVoice getVoice(ProgramSequencePatternEvent event) throws HubIngestException {
    return getOrThrow(programVoices, getTrack(event).getProgramVoiceId());
  }

  @Override
  public ProgramVoiceTrack getTrack(ProgramSequencePatternEvent event) throws HubIngestException {
    return getOrThrow(programTracks, event.getProgramVoiceTrackId());
  }

  @Override
  public Collection<ProgramVoice> getVoices(Program program) {
    return programVoices.getAll().stream()
      .filter(m -> m.getProgramId().equals(program.getId()))
      .collect(Collectors.toList());
  }

  /**
   Get a member from a map of entities, or else throw an exception

   @param entities to get member from
   @param id       of member to get
   @param <N>      type of entity
   @return entity from map
   @throws HubIngestException if no such entity exists
   */
  private <N extends Entity> N getOrThrow(EntityCache<N> entities, UUID id) throws HubIngestException {
    try {
      return entities.get(id);
    } catch (EntityException e) {
      throw new HubIngestException(e);
    }
  }
}
