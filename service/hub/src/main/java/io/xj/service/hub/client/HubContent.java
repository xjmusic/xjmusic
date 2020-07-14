// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.client;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.lib.entity.Entity;
import io.xj.lib.entity.MemeEntity;
import io.xj.lib.util.Text;
import io.xj.service.hub.entity.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 [#154350346] to ingest any combination of Programs, Instruments, or Libraries (with their Programs and Instruments)
 */
public class HubContent {
  private final Map<UUID, Program> programs;
  private final Map<UUID, InstrumentAudio> instrumentAudios;
  private final Map<UUID, InstrumentAudioChord> instrumentAudioChords;
  private final Map<UUID, InstrumentAudioEvent> instrumentAudioEvents;
  private final Map<UUID, InstrumentMeme> instrumentMemes;
  private final Map<UUID, ProgramSequencePatternEvent> programEvents;
  private final Map<UUID, ProgramMeme> programMemes;
  private final Map<UUID, ProgramSequencePattern> programPatterns;
  private final Map<UUID, ProgramSequence> programSequences;
  private final Map<UUID, ProgramSequenceBinding> programSequenceBindings;
  private final Map<UUID, ProgramSequenceBindingMeme> programSequenceBindingMemes;
  private final Map<UUID, ProgramSequenceChord> programSequenceChords;
  private final Map<UUID, ProgramVoiceTrack> programTracks;
  private final Map<UUID, ProgramVoice> programVoices;
  private final Map<UUID, Instrument> instruments;

  @Inject
  public HubContent(
    @Assisted("entities") Collection<Entity> entities
  ) {
    this.instrumentAudioChords = Maps.newConcurrentMap();
    this.instrumentAudios = Maps.newConcurrentMap();
    this.instrumentAudioEvents = Maps.newConcurrentMap();
    this.instruments = Maps.newConcurrentMap();
    this.instrumentMemes = Maps.newConcurrentMap();
    this.programs = Maps.newConcurrentMap();
    this.programEvents = Maps.newConcurrentMap();
    this.programMemes = Maps.newConcurrentMap();
    this.programPatterns = Maps.newConcurrentMap();
    this.programSequenceBindings = Maps.newConcurrentMap();
    this.programSequenceBindingMemes = Maps.newConcurrentMap();
    this.programSequenceChords = Maps.newConcurrentMap();
    this.programSequences = Maps.newConcurrentMap();
    this.programTracks = Maps.newConcurrentMap();
    this.programVoices = Maps.newConcurrentMap();

    // ingest programs
    entities.forEach(entity -> {
      switch (entity.getClass().getSimpleName()) {
        case "InstrumentAudioChord":
          instrumentAudioChords.put(entity.getId(), (InstrumentAudioChord) entity);
          break;
        case "InstrumentAudio":
          instrumentAudios.put(entity.getId(), (InstrumentAudio) entity);
          break;
        case "InstrumentAudioEvent":
          instrumentAudioEvents.put(entity.getId(), (InstrumentAudioEvent) entity);
          break;
        case "Instrument":
          instruments.put(entity.getId(), (Instrument) entity);
          break;
        case "InstrumentMeme":
          instrumentMemes.put(entity.getId(), (InstrumentMeme) entity);
          break;
        case "Program":
          programs.put(entity.getId(), (Program) entity);
          break;
        case "ProgramSequencePatternEvent":
          programEvents.put(entity.getId(), (ProgramSequencePatternEvent) entity);
          break;
        case "ProgramMeme":
          programMemes.put(entity.getId(), (ProgramMeme) entity);
          break;
        case "ProgramSequencePattern":
          programPatterns.put(entity.getId(), (ProgramSequencePattern) entity);
          break;
        case "ProgramSequenceBinding":
          programSequenceBindings.put(entity.getId(), (ProgramSequenceBinding) entity);
          break;
        case "ProgramSequenceBindingMeme":
          programSequenceBindingMemes.put(entity.getId(), (ProgramSequenceBindingMeme) entity);
          break;
        case "ProgramSequenceChord":
          programSequenceChords.put(entity.getId(), (ProgramSequenceChord) entity);
          break;
        case "ProgramSequence":
          programSequences.put(entity.getId(), (ProgramSequence) entity);
          break;
        case "ProgramVoiceTrack":
          programTracks.put(entity.getId(), (ProgramVoiceTrack) entity);
          break;
        case "ProgramVoice":
          programVoices.put(entity.getId(), (ProgramVoice) entity);
          break;
      }
    });
  }

  /**
   Get all available sequence pattern offsets of a given sequence

   @param sequenceBinding to get available sequence pattern offsets for
   @return collection of available sequence pattern offsets
   */
  public Collection<Long> getAvailableOffsets(ProgramSequenceBinding sequenceBinding) {
    return getAllProgramSequenceBindings().stream()
      .filter(psb -> psb.getProgramId().equals(sequenceBinding.getProgramId()))
      .map(ProgramSequenceBinding::getOffset)
      .distinct()
      .collect(Collectors.toList());
  }

  /**
   get cached InstrumentAudioChord by id

   @param id of InstrumentAudioChord to get
   @return InstrumentAudioChord
   */
  public InstrumentAudioChord getInstrumentAudioChord(UUID id) {
    return this.instrumentAudioChords.get(id);
  }

  /**
   get cached InstrumentAudio by id

   @param id of InstrumentAudio to get
   @return InstrumentAudio
   */
  public InstrumentAudio getInstrumentAudio(UUID id) {
    return this.instrumentAudios.get(id);
  }

  /**
   get cached InstrumentAudioEvent by id

   @param id of InstrumentAudioEvent to get
   @return InstrumentAudioEvent
   */
  public InstrumentAudioEvent getInstrumentAudioEvent(UUID id) {
    return this.instrumentAudioEvents.get(id);
  }

  /**
   get cached Instrument by id

   @param id of Instrument to get
   @return Instrument
   */
  public Instrument getInstrument(UUID id) {
    return this.instruments.get(id);
  }

  /**
   get cached InstrumentMeme by id

   @param id of InstrumentMeme to get
   @return InstrumentMeme
   */
  public InstrumentMeme getInstrumentMeme(UUID id) {
    return this.instrumentMemes.get(id);
  }

  /**
   get cached Program by id

   @param id of Program to get
   @return Program
   */
  public Program getProgram(UUID id) {
    return this.programs.get(id);
  }

  /**
   get cached ProgramSequencePatternEvent by id

   @param id of ProgramSequencePatternEvent to get
   @return ProgramSequencePatternEvent
   */
  public ProgramSequencePatternEvent getProgramEvent(UUID id) {
    return this.programEvents.get(id);
  }

  /**
   get cached ProgramMeme by id

   @param id of ProgramMeme to get
   @return ProgramMeme
   */
  public ProgramMeme getProgramMeme(UUID id) {
    return this.programMemes.get(id);
  }

  /**
   get cached ProgramSequencePattern by id

   @param id of ProgramSequencePattern to get
   @return ProgramSequencePattern
   */
  public ProgramSequencePattern getProgramPattern(UUID id) {
    return this.programPatterns.get(id);
  }

  /**
   get cached ProgramSequenceBinding by id

   @param id of ProgramSequenceBinding to get
   @return ProgramSequenceBinding
   */
  public ProgramSequenceBinding getProgramSequenceBinding(UUID id) {
    return this.programSequenceBindings.get(id);
  }

  /**
   get cached ProgramSequenceBindingMeme by id

   @param id of ProgramSequenceBindingMeme to get
   @return ProgramSequenceBindingMeme
   */
  public ProgramSequenceBindingMeme getProgramSequenceBindingMeme(UUID id) {
    return this.programSequenceBindingMemes.get(id);
  }

  /**
   get cached ProgramSequenceChord by id

   @param id of ProgramSequenceChord to get
   @return ProgramSequenceChord
   */
  public ProgramSequenceChord getProgramSequenceChord(UUID id) {
    return this.programSequenceChords.get(id);
  }

  /**
   get cached ProgramSequence by id

   @param id of ProgramSequence to get
   @return ProgramSequence
   */
  public ProgramSequence getProgramSequence(UUID id) {
    return this.programSequences.get(id);
  }

  /**
   get cached ProgramVoiceTrack by id

   @param id of ProgramVoiceTrack to get
   @return ProgramVoiceTrack
   */
  public ProgramVoiceTrack getProgramTrack(UUID id) {
    return this.programTracks.get(id);
  }

  /**
   get cached ProgramVoice by id

   @param id of ProgramVoice to get
   @return ProgramVoice
   */
  public ProgramVoice getProgramVoice(UUID id) {
    return this.programVoices.get(id);
  }


  /**
   get all cached Programs

   @return cached Programs
   */
  public Collection<Program> getAllPrograms() {
    return programs.values();
  }

  /**
   get all cached InstrumentAudios

   @return cached InstrumentAudios
   */
  public Collection<InstrumentAudio> getAllInstrumentAudios() {
    return instrumentAudios.values();
  }

  /**
   get all cached InstrumentAudioChords

   @return cached InstrumentAudioChords
   */
  public Collection<InstrumentAudioChord> getAllInstrumentAudioChords() {
    return instrumentAudioChords.values();
  }

  /**
   get all cached InstrumentAudioEvents

   @return cached InstrumentAudioEvents
   */
  public Collection<InstrumentAudioEvent> getAllInstrumentAudioEvents() {
    return instrumentAudioEvents.values();
  }

  /**
   get all cached InstrumentMemes

   @return cached InstrumentMemes
   */
  public Collection<InstrumentMeme> getAllInstrumentMemes() {
    return instrumentMemes.values();
  }

  /**
   get all cached ProgramEvents

   @return cached ProgramEvents
   */
  public Collection<ProgramSequencePatternEvent> getAllProgramSequencePatternEvents() {
    return programEvents.values();
  }

  /**
   get all cached ProgramMemes

   @return cached ProgramMemes
   */
  public Collection<ProgramMeme> getAllProgramMemes() {
    return programMemes.values();
  }

  /**
   get all cached ProgramPatterns

   @return cached ProgramPatterns
   */
  public Collection<ProgramSequencePattern> getAllProgramSequencePatterns() {
    return programPatterns.values();
  }

  /**
   get all cached ProgramSequences

   @return cached ProgramSequences
   */
  public Collection<ProgramSequence> getAllProgramSequences() {
    return programSequences.values();
  }

  /**
   get all cached ProgramSequenceBindings

   @return cached ProgramSequenceBindings
   */
  public Collection<ProgramSequenceBinding> getAllProgramSequenceBindings() {
    return programSequenceBindings.values();
  }

  /**
   get all cached ProgramSequenceBindingMemes

   @return cached ProgramSequenceBindingMemes
   */
  public Collection<ProgramSequenceBindingMeme> getAllProgramSequenceBindingMemes() {
    return programSequenceBindingMemes.values();
  }

  /**
   get all cached ProgramSequenceChords

   @return cached ProgramSequenceChords
   */
  public Collection<ProgramSequenceChord> getAllProgramSequenceChords() {
    return programSequenceChords.values();
  }

  /**
   get all cached ProgramTracks

   @return cached ProgramTracks
   */
  public Collection<ProgramVoiceTrack> getAllProgramTracks() {
    return programTracks.values();
  }

  /**
   get all cached ProgramVoices

   @return cached ProgramVoices
   */
  public Collection<ProgramVoice> getAllProgramVoices() {
    return programVoices.values();
  }

  /**
   get all cached Instruments

   @return cached Instruments
   */
  public Collection<Instrument> getAllInstruments() {
    return instruments.values();
  }

  /**
   Get a collection of all sequences of a particular type for ingest

   @return collection of sequences
   */
  public Collection<Program> getProgramsOfType(ProgramType type) {
    return getAllPrograms().stream()
      .filter(program -> program.getType().equals(type))
      .collect(Collectors.toList());
  }

  /**
   Get a collection of all instruments of a particular type for ingest

   @return collection of instruments
   */
  public Collection<Instrument> getInstrumentsOfType(InstrumentType type) {
    return getAllInstruments().stream()
      .filter(instrument -> instrument.getType().equals(type))
      .collect(Collectors.toList());
  }

  /**
   Get a collection of all entities

   @return collection of all entities
   */
  public Collection<Entity> getAllEntities() {
    return ImmutableList.<Entity>builder()
      .addAll(instrumentAudioChords.values())
      .addAll(instrumentAudios.values())
      .addAll(instrumentAudioEvents.values())
      .addAll(instruments.values())
      .addAll(instrumentMemes.values())
      .addAll(programs.values())
      .addAll(programEvents.values())
      .addAll(programMemes.values())
      .addAll(programPatterns.values())
      .addAll(programSequenceBindings.values())
      .addAll(programSequenceBindingMemes.values())
      .addAll(programSequenceChords.values())
      .addAll(programSequences.values())
      .addAll(programTracks.values())
      .addAll(programVoices.values())
      .build();
  }

  /**
   Get a string representation of the ingest
   */
  @Override
  public String toString() {
    Multiset<String> entityHistogram = ConcurrentHashMultiset.create();
    getAllEntities().forEach((Object obj) -> entityHistogram.add(Text.getSimpleName(obj)));
    List<String> descriptors = Lists.newArrayList();
    Collection<String> names = Ordering.from(String.CASE_INSENSITIVE_ORDER).sortedCopy(entityHistogram.elementSet());
    names.forEach((String name) -> descriptors.add(String.format("%d %s", entityHistogram.count(name), name)));
    return String.join(", ", descriptors);
  }

  /**
   Get memes of program

   @param program to get memes for
   @return memes of program
   */
  public Collection<ProgramMeme> getMemes(Program program) {
    return programMemes.values().stream()
      .filter(m -> m.getProgramId().equals(program.getId()))
      .collect(Collectors.toList());
  }

  /**
   Get events for a given program

   @param program to get events for
   @return events for given program
   */
  public Collection<ProgramSequencePatternEvent> getEvents(Program program) {
    return programEvents.values().stream()
      .filter(m -> m.getProgramId().equals(program.getId()))
      .collect(Collectors.toList());
  }

  /**
   Get events for a given program pattern

   @param programPattern to get events for
   @return events for given program pattern
   */
  public Collection<ProgramSequencePatternEvent> getEvents(ProgramSequencePattern programPattern) {
    return programEvents.values().stream()
      .filter(m -> m.getProgramSequencePatternId().equals(programPattern.getId()))
      .collect(Collectors.toList());
  }

  /**
   Get memes of instrument

   @param instrument to get memes for
   @return memes of instrument
   */
  public Collection<InstrumentMeme> getMemes(Instrument instrument) {
    return instrumentMemes.values().stream()
      .filter(m -> m.getInstrumentId().equals(instrument.getId()))
      .collect(Collectors.toList());
  }

  /**
   Get all Audios for a given instrument id

   @param uuid of instrument to get audios for
   @return audios of instrument id
   */
  public Collection<InstrumentAudio> getAudiosForInstrumentId(UUID uuid) {
    return instrumentAudios.values().stream()
      .filter(a -> a.getInstrumentId().equals(uuid))
      .collect(Collectors.toList());
  }

  /**
   Get all InstrumentAudios for a given Instrument

   @param instrument to get audios for
   @return audios for instrument
   */
  public Collection<InstrumentAudio> getAudios(Instrument instrument) {
    return getAudiosForInstrumentId(instrument.getId());
  }

  /**
   Get all AudioEvents for a given Audio

   @param audio to get events for
   @return events of audio
   */
  public Collection<InstrumentAudioEvent> getEvents(InstrumentAudio audio) {
    return instrumentAudioEvents.values().stream()
      .filter(e -> e.getInstrumentAudioId().equals(audio.getId()))
      .collect(Collectors.toList());
  }

  /**
   Get all AudioChords for a given Audio

   @param audio to get chords for
   @return chords of audio
   */
  public Collection<InstrumentAudioChord> getChords(InstrumentAudio audio) {
    return instrumentAudioChords.values().stream()
      .filter(e -> e.getInstrumentAudioId().equals(audio.getId()))
      .collect(Collectors.toList());
  }

  /**
   Get all ProgramSequenceChords for a given Sequence

   @param sequence to get chords for
   @return chords of sequence
   */
  public Collection<ProgramSequenceChord> getChords(ProgramSequence sequence) {
    return programSequenceChords.values().stream()
      .filter(e -> e.getProgramSequenceId().equals(sequence.getId()))
      .collect(Collectors.toList());
  }

  /**
   Get all program sequence binding memes for program sequence binding

   @param programSequenceBinding to get memes for
   @return memes
   */
  public Collection<ProgramSequenceBindingMeme> getMemes(ProgramSequenceBinding programSequenceBinding) {
    return programSequenceBindingMemes.values().stream()
      .filter(m -> m.getProgramSequenceBindingId().equals(programSequenceBinding.getId()))
      .collect(Collectors.toList());
  }

  /**
   Get all program sequence bindings for a given program

   @param program to get sequence bindings for
   @return all sequence bindings for given program
   */
  public Collection<ProgramSequenceBinding> getSequenceBindings(Program program) {
    return programSequenceBindings.values().stream()
      .filter(m -> m.getProgramId().equals(program.getId()))
      .collect(Collectors.toList());
  }

  /**
   Get all program sequences for a given program

   @param program to get program sequences for
   @return all program sequences for given program
   */
  public Collection<ProgramSequence> getSequences(Program program) {
    return programSequences.values().stream()
      .filter(m -> m.getProgramId().equals(program.getId()))
      .collect(Collectors.toList());
  }

  /**
   Get the program sequence for a given program sequence binding

   @param sequenceBinding to get program sequence for
   @return program sequence for the given program sequence binding
   */
  public ProgramSequence getSequence(ProgramSequenceBinding sequenceBinding) {
    return programSequences.get(sequenceBinding.getProgramSequenceId());
  }

  /**
   Read all AudioEvent that are first in an audio, for all audio in an Instrument

   @param instrument to get audio for
   @return audio events
   @throws HubClientException on failure
   */
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

  /**
   Fetch all memes for a given program at sequence binding offset 0

   @return collection of sequence memes
   @throws HubClientException on failure
   */
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

  /**
   Get sequence bindings at a specified offset

   @param program to get sequence bindings for
   @param offset  to get sequence bindings at
   @return sequence bindings at offset
   */
  public Collection<ProgramSequenceBinding> getProgramSequenceBindingsAtOffset(Program program, Long offset) {
    return getAllProgramSequenceBindings().stream()
      .filter(psb -> psb.getProgramId().equals(program.getId()) && psb.getOffset().equals(offset))
      .collect(Collectors.toList());
  }

  /**
   Get Program voice for a given program event

   @param event to get program voice of
   @return Program voice for the given program event
   */
  public ProgramVoice getVoice(ProgramSequencePatternEvent event) {
    return programVoices.get(getTrack(event).getProgramVoiceId());
  }

  /**
   Get Program track for a given program event

   @param event to get program track of
   @return Program track for the given program event
   */
  public ProgramVoiceTrack getTrack(ProgramSequencePatternEvent event) {
    return programTracks.get(event.getProgramVoiceTrackId());
  }

  /**
   Get all program voices for a given program

   @param program to get program voices for
   @return program voices for the given program
   */
  public Collection<ProgramVoice> getVoices(Program program) {
    return programVoices.values().stream()
      .filter(m -> m.getProgramId().equals(program.getId()))
      .collect(Collectors.toList());
  }

  /**
   Get a count of total entities in this Hub Content

   @return total number of entities in this Hub Content
   */
  public int size() {
    return instrumentAudioChords.size() +
      instrumentAudios.size() +
      instrumentAudioEvents.size() +
      instruments.size() +
      instrumentMemes.size() +
      programs.size() +
      programEvents.size() +
      programMemes.size() +
      programPatterns.size() +
      programSequenceBindings.size() +
      programSequenceBindingMemes.size() +
      programSequenceChords.size() +
      programSequences.size() +
      programTracks.size() +
      programVoices.size();
  }
}
