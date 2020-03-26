// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.ingest;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.cache.EntityCache;
import io.xj.service.hub.dao.InstrumentDAO;
import io.xj.service.hub.dao.ProgramDAO;
import io.xj.service.hub.entity.Entity;
import io.xj.service.hub.entity.MemeEntity;
import io.xj.service.hub.model.ChainBinding;
import io.xj.service.hub.model.Instrument;
import io.xj.service.hub.model.InstrumentAudio;
import io.xj.service.hub.model.InstrumentAudioChord;
import io.xj.service.hub.model.InstrumentAudioEvent;
import io.xj.service.hub.model.InstrumentMeme;
import io.xj.service.hub.model.InstrumentType;
import io.xj.service.hub.model.Program;
import io.xj.service.hub.model.ProgramMeme;
import io.xj.service.hub.model.ProgramSequence;
import io.xj.service.hub.model.ProgramSequenceBinding;
import io.xj.service.hub.model.ProgramSequenceBindingMeme;
import io.xj.service.hub.model.ProgramSequenceChord;
import io.xj.service.hub.model.ProgramSequencePattern;
import io.xj.service.hub.model.ProgramSequencePatternEvent;
import io.xj.service.hub.model.ProgramType;
import io.xj.service.hub.model.ProgramVoice;
import io.xj.service.hub.model.ProgramVoiceTrack;
import io.xj.service.hub.model.SegmentChoiceArrangementPick;
import io.xj.lib.util.Text;
import io.xj.lib.util.Value;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 [#154350346] to ingest any combination of Programs, Instruments, or Libraries (with their Programs and Instruments)
 */
class IngestImpl implements Ingest {
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
  ) throws HubException {
    this.access = access;
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
  public InstrumentAudioChord getInstrumentAudioChord(UUID id) throws HubException {
    return this.instrumentAudioChords.get(id);
  }

  @Override
  public InstrumentAudio getInstrumentAudio(UUID id) throws HubException {
    return this.instrumentAudios.get(id);
  }

  @Override
  public InstrumentAudioEvent getInstrumentAudioEvent(UUID id) throws HubException {
    return this.instrumentAudioEvents.get(id);
  }

  @Override
  public Instrument getInstrument(UUID id) throws HubException {
    return this.instruments.get(id);
  }

  @Override
  public InstrumentMeme getInstrumentMeme(UUID id) throws HubException {
    return this.instrumentMemes.get(id);
  }

  @Override
  public Program getProgram(UUID id) throws HubException {
    return this.programs.get(id);
  }

  @Override
  public ProgramSequencePatternEvent getProgramEvent(UUID id) throws HubException {
    return this.programEvents.get(id);
  }

  @Override
  public ProgramMeme getProgramMeme(UUID id) throws HubException {
    return this.programMemes.get(id);
  }

  @Override
  public ProgramSequencePattern getProgramPattern(UUID id) throws HubException {
    return this.programPatterns.get(id);
  }

  @Override
  public ProgramSequenceBinding getProgramSequenceBinding(UUID id) throws HubException {
    return this.programSequenceBindings.get(id);
  }

  @Override
  public ProgramSequenceBindingMeme getProgramSequenceBindingMeme(UUID id) throws HubException {
    return this.programSequenceBindingMemes.get(id);
  }

  @Override
  public ProgramSequenceChord getProgramSequenceChord(UUID id) throws HubException {
    return this.programSequenceChords.get(id);
  }

  @Override
  public ProgramSequence getProgramSequence(UUID id) throws HubException {
    return this.programSequences.get(id);
  }

  @Override
  public ProgramVoiceTrack getProgramTrack(UUID id) throws HubException {
    return this.programTracks.get(id);
  }

  @Override
  public ProgramVoice getProgramVoice(UUID id) throws HubException {
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
    Multiset<String> entityHistogram = ConcurrentHashMultiset.create();
    getAllEntities().forEach((Object obj) -> entityHistogram.add(Text.getSimpleName(obj)));
    List<String> descriptors = com.google.api.client.util.Lists.newArrayList();
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
  public ProgramSequence getSequence(ProgramSequenceBinding sequenceBinding) throws HubException {
    return programSequences.get(sequenceBinding.getProgramSequenceId());
  }

  @Override
  public InstrumentAudio getAudio(SegmentChoiceArrangementPick pick) throws HubException {
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
  public ProgramVoice getVoice(ProgramSequencePatternEvent event) throws HubException {
    return programVoices.get(getTrack(event).getProgramVoiceId());
  }

  @Override
  public ProgramVoiceTrack getTrack(ProgramSequencePatternEvent event) throws HubException {
    return programTracks.get(event.getProgramVoiceTrackId());
  }

  @Override
  public Collection<ProgramVoice> getVoices(Program program) {
    return programVoices.getAll().stream()
      .filter(m -> m.getProgramId().equals(program.getId()))
      .collect(Collectors.toList());
  }

}
