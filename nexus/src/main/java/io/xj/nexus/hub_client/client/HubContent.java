// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.hub_client.client;

import com.google.common.collect.*;
import com.google.inject.Inject;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.*;
import io.xj.lib.entity.Entities;
import io.xj.lib.util.Text;

import java.util.*;
import java.util.stream.Collectors;

/**
 [#154350346] to ingest any combination of Programs, Instruments, or Libraries (with their Programs and Instruments)
 <p>
 Refactoring this class ala [#173803936] `HubContent` extends common `EntityStore` implementation
 */
public class HubContent {
  private final Map<Class<?>/*Type*/, Map<UUID/*ID*/, Object>> store = Maps.newConcurrentMap();

  @Inject
  public HubContent(
    Collection<?> entities
  ) throws HubClientException {
    try {
      for (Object entity : entities) {
        store.putIfAbsent(entity.getClass(), Maps.newConcurrentMap());
        store.get(entity.getClass()).put(Entities.getId(entity), entity);
      }

    } catch (Exception e) {
      throw new HubClientException(e);
    }
  }

  /**
   Get all available sequence pattern offsets of a given sequence, sorted by offset

   @param sequenceBinding to get available sequence pattern offsets for
   @return collection of available sequence pattern offsets
   */
  public Collection<Integer> getAvailableOffsets(ProgramSequenceBinding sequenceBinding) {
    return getAllProgramSequenceBindings().stream()
      .filter(psb -> psb.getProgramId().equals(sequenceBinding.getProgramId()))
      .map(ProgramSequenceBinding::getOffset)
      .distinct()
      .sorted(Integer::compareTo)
      .collect(Collectors.toList());
  }

  /**
   get cached InstrumentAudio by id

   @param id of InstrumentAudio to get
   @return InstrumentAudio
   */
  public Optional<InstrumentAudio> getInstrumentAudio(UUID id) {
    return get(InstrumentAudio.class, id);
  }

  /**
   get cached Instrument by id

   @param id of Instrument to get
   @return Instrument
   */
  public Optional<Instrument> getInstrument(UUID id) {
    return get(Instrument.class, id);
  }

  /**
   get cached Program by id

   @param id of Program to get
   @return Program
   */
  public Optional<Program> getProgram(UUID id) {
    return get(Program.class, id);
  }

  /**
   get cached ProgramSequencePatternEvent by id

   @param id of ProgramSequencePatternEvent to get
   @return ProgramSequencePatternEvent
   */
  public Optional<ProgramSequencePatternEvent> getProgramSequencePatternEvent(UUID id) {
    return get(ProgramSequencePatternEvent.class, id);
  }

  /**
   get cached ProgramSequenceBinding by id

   @param id of ProgramSequenceBinding to get
   @return ProgramSequenceBinding
   */
  public Optional<ProgramSequenceBinding> getProgramSequenceBinding(UUID id) {
    return get(ProgramSequenceBinding.class, id);
  }

  /**
   get cached ProgramSequence by id

   @param id of ProgramSequence to get
   @return ProgramSequence
   */
  public Optional<ProgramSequence> getProgramSequence(UUID id) {
    return get(ProgramSequence.class, id);
  }

  /**
   get cached ProgramVoiceTrack by id

   @param id of ProgramVoiceTrack to get
   @return ProgramVoiceTrack
   */
  public Optional<ProgramVoiceTrack> getProgramVoiceTrack(UUID id) {
    return get(ProgramVoiceTrack.class, id);
  }

  /**
   get cached ProgramVoice by id

   @param id of ProgramVoice to get
   @return ProgramVoice
   */
  public Optional<ProgramVoice> getProgramVoice(UUID id) {
    return get(ProgramVoice.class, id);
  }

  /**
   get all cached Programs

   @return cached Programs
   */
  public Collection<Program> getAllPrograms() {
    return getAll(Program.class);
  }

  /**
   get all cached InstrumentAudios

   @return cached InstrumentAudios
   */
  public Collection<InstrumentAudio> getAllInstrumentAudios() {
    return getAll(InstrumentAudio.class);
  }

  /**
   get all cached InstrumentMemes

   @return cached InstrumentMemes
   */
  public Collection<InstrumentMeme> getAllInstrumentMemes() {
    return getAll(InstrumentMeme.class);
  }

  /**
   get all cached ProgramEvents

   @return cached ProgramEvents
   */
  public Collection<ProgramSequencePatternEvent> getAllProgramSequencePatternEvents() {
    return getAll(ProgramSequencePatternEvent.class);
  }

  /**
   get all cached ProgramMemes

   @return cached ProgramMemes
   */
  public Collection<ProgramMeme> getAllProgramMemes() {
    return getAll(ProgramMeme.class);
  }

  /**
   get all cached ProgramPatterns

   @return cached ProgramPatterns
   */
  public Collection<ProgramSequencePattern> getAllProgramSequencePatterns() {
    return getAll(ProgramSequencePattern.class);
  }

  /**
   get all cached ProgramSequences

   @return cached ProgramSequences
   */
  public Collection<ProgramSequence> getAllProgramSequences() {
    return getAll(ProgramSequence.class);
  }

  /**
   get all cached ProgramSequenceBindings

   @return cached ProgramSequenceBindings
   */
  public Collection<ProgramSequenceBinding> getAllProgramSequenceBindings() {
    return getAll(ProgramSequenceBinding.class);
  }

  /**
   get all cached ProgramSequenceBindingMemes

   @return cached ProgramSequenceBindingMemes
   */
  public Collection<ProgramSequenceBindingMeme> getAllProgramSequenceBindingMemes() {
    return getAll(ProgramSequenceBindingMeme.class);
  }

  /**
   get all cached ProgramSequenceChords

   @return cached ProgramSequenceChords
   */
  public Collection<ProgramSequenceChord> getAllProgramSequenceChords() {
    return getAll(ProgramSequenceChord.class);
  }

  /**
   get all cached ProgramSequenceChordVoicings

   @return cached ProgramSequenceChordVoicings
   */
  public Collection<ProgramSequenceChordVoicing> getAllProgramSequenceChordVoicings() {
    return getAll(ProgramSequenceChordVoicing.class);
  }

  /**
   get all cached ProgramVoices

   @return cached ProgramVoices
   */
  public Collection<ProgramVoice> getAllProgramVoices() {
    return getAll(ProgramVoice.class);
  }

  /**
   get all cached Instruments

   @return cached Instruments
   */
  public Collection<Instrument> getAllInstruments() {
    return getAll(Instrument.class);
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
      .filter(instrument -> {
        return instrument.getType().equals(type);
      })
      .collect(Collectors.toList());
  }

  /**
   Get a string representation of ingest
   */
  @Override
  public String toString() {
    Multiset<String> entityHistogram = ConcurrentHashMultiset.create();
    getAll().forEach((Object obj) -> entityHistogram.add(Text.getSimpleName(obj)));
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
    return getProgramMemes(program.getId());
  }

  /**
   Get memes of program

   @param programId to get memes for
   @return memes of program
   */
  public Collection<ProgramMeme> getProgramMemes(UUID programId) {
    return getAllProgramMemes().stream()
      .filter(m -> m.getProgramId().equals(programId))
      .collect(Collectors.toList());
  }

  /**
   Get events for a given program

   @param programId to get events for
   @return events for given program
   */
  public Collection<ProgramSequencePatternEvent> getEvents(UUID programId) {
    return getAllProgramSequencePatternEvents().stream()
      .filter(m -> m.getProgramId().equals(programId))
      .collect(Collectors.toList());
  }

  /**
   Get events for a given program pattern

   @param programPattern to get events for
   @return events for given program pattern
   */
  public Collection<ProgramSequencePatternEvent> getEvents(ProgramSequencePattern programPattern) {
    return getAllProgramSequencePatternEvents().stream()
      .filter(m -> m.getProgramSequencePatternId().equals(programPattern.getId()))
      .sorted(Comparator.comparing(ProgramSequencePatternEvent::getPosition))
      .collect(Collectors.toList());
  }

  /**
   Get memes of instrument

   @param instrument to get memes for
   @return memes of instrument
   */
  public Collection<InstrumentMeme> getMemes(Instrument instrument) {
    return getAllInstrumentMemes().stream()
      .filter(m -> m.getInstrumentId().equals(instrument.getId()))
      .collect(Collectors.toList());
  }

  /**
   Get all Audios for a given instrument id

   @param id of instrument to get audios for
   @return audios of instrument id
   */
  public Collection<InstrumentAudio> getAudiosForInstrumentId(UUID id) {
    return getAllInstrumentAudios().stream()
      .filter(a -> a.getInstrumentId().equals(id))
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
   Get all ProgramSequenceChords for a given Sequence

   @param sequence to get chords for
   @return chords of sequence
   */
  public Collection<ProgramSequenceChord> getChords(ProgramSequence sequence) {
    return getAllProgramSequenceChords().stream()
      .filter(e -> e.getProgramSequenceId().equals(sequence.getId()))
      .collect(Collectors.toList());
  }

  /**
   Get all ProgramSequenceChordVoicings for a given Sequence Chord

   @param chord to get voicings for
   @return chords of sequence
   */
  public Collection<ProgramSequenceChordVoicing> getVoicings(ProgramSequenceChord chord) {
    return getAllProgramSequenceChordVoicings().stream()
      .filter(e -> e.getProgramSequenceChordId().equals(chord.getId()))
      .collect(Collectors.toList());
  }

  /**
   Get all program sequence binding memes for program sequence binding

   @param programSequenceBinding to get memes for
   @return memes
   */
  public Collection<ProgramSequenceBindingMeme> getMemes(ProgramSequenceBinding programSequenceBinding) {
    return getAllProgramSequenceBindingMemes().stream()
      .filter(m -> m.getProgramSequenceBindingId().equals(programSequenceBinding.getId()))
      .collect(Collectors.toList());
  }

  /**
   Get the program sequence for a given program sequence binding

   @param sequenceBinding to get program sequence for
   @return program sequence for the given program sequence binding
   */
  public Optional<ProgramSequence> getProgramSequence(ProgramSequenceBinding sequenceBinding) {
    return getProgramSequence(sequenceBinding.getProgramSequenceId());
  }

  /**
   Fetch all memes for a given program at sequence binding offset 0

   @return collection of sequence memes
   */
  public Collection<String> getMemesAtBeginning(Program program) {
    Map<String, Boolean> memes = Maps.newHashMap();

    // add sequence memes
    getMemes(program).forEach((meme ->
      memes.put(meme.getName(), true)));

    // add sequence binding memes
    for (ProgramSequenceBinding sequenceBinding : getProgramSequenceBindingsAtOffset(program, 0))
      for (ProgramSequenceBindingMeme meme : getMemes(sequenceBinding))
        memes.put(meme.getName(), true);

    return memes.keySet();
  }

  /**
   Get sequence bindings at a specified offset.
   If the target offset is not found in the chosen Main Program,
   we'll find the nearest matching offset, and return all bindings at that offset.
   <p>
   [#177052278] Chain should always be able to determine main sequence binding offset

   @param program to get sequence bindings for
   @param offset  to get sequence bindings at
   @return sequence bindings at offset
   */
  public Collection<ProgramSequenceBinding> getProgramSequenceBindingsAtOffset(Program program, Integer offset) {
    return getSequenceBindingsAtProgramOffset(program.getId(), offset);
  }

  /**
   Get sequence bindings at a specified offset.
   If the target offset is not found in the chosen Main Program,
   we'll find the nearest matching offset, and return all bindings at that offset.
   <p>
   [#177052278] Chain should always be able to determine main sequence binding offset

   @param programId to get sequence bindings for
   @param offset    to get sequence bindings at
   @return sequence bindings at offset
   */
  public Collection<ProgramSequenceBinding> getSequenceBindingsAtProgramOffset(UUID programId, Integer offset) {
    var candidates = getAllProgramSequenceBindings().stream()
      .filter(psb -> Objects.equals(psb.getProgramId(), programId))
      .collect(Collectors.toList());
    var actualOffset = candidates.stream()
      .map(ProgramSequenceBinding::getOffset)
      .min(Comparator.comparing(psbOffset -> Math.abs(psbOffset - offset)));
    if (actualOffset.isEmpty())
      return ImmutableList.of();
    return getAllProgramSequenceBindings().stream()
      .filter(psb ->
        Objects.equals(psb.getProgramId(), programId) &&
          Objects.equals(psb.getOffset(), actualOffset.get()))
      .collect(Collectors.toList());
  }

  /**
   Get program sequence chord voicings

   @param programId to get sequence chord voicings of
   @return sequence chord voicings for program
   */
  public List<ProgramSequenceChordVoicing> getProgramSequenceChordVoicings(UUID programId) {
    var voicings = getAllProgramSequenceChordVoicings();
    return voicings.stream()
      .filter(voicing -> voicing.getProgramId().equals(programId))
      .collect(Collectors.toList());
  }

  /**
   Get Program voice for a given program event

   @param event to get program voice of
   @return Program voice for the given program event
   */
  public Optional<ProgramVoice> getVoice(ProgramSequencePatternEvent event) {
    var track = getTrack(event);
    if (track.isEmpty()) return Optional.empty();
    return getProgramVoice(track.get().getProgramVoiceId());
  }

  /**
   Get Program track for a given program event

   @param event to get program track of
   @return Program track for the given program event
   */
  public Optional<ProgramVoiceTrack> getTrack(ProgramSequencePatternEvent event) {
    return getProgramVoiceTrack(event.getProgramVoiceTrackId());
  }

  /**
   Get all program voices for a given program

   @param program to get program voices for
   @return program voices for the given program
   */
  public Collection<ProgramVoice> getVoices(Program program) {
    return getAllProgramVoices().stream()
      .filter(m -> m.getProgramId().equals(program.getId()))
      .collect(Collectors.toList());
  }

  /**
   Get a count of total entities in this Hub Content

   @return total number of entities in this Hub Content
   */
  public int size() {
    return store.values().stream()
      .mapToInt(Map::size)
      .sum();
  }

  /**
   Get an entity of a given type and id from the store, or throw an exception

   @param type to get
   @param id   to get
   @param <E>  class
   @return entity
   */
  private <E> Optional<E> get(Class<E> type, UUID id) {
    try {
      if (store.containsKey(type) && store.get(type).containsKey(id))
        //noinspection unchecked
        return Optional.of((E) store.get(type).get(id));
      // otherwise
      return Optional.empty();

    } catch (Exception e) {
      return Optional.empty();
    }
  }

  /**
   Get all entities of a given type from the store, or throw an exception

   @param type to get
   @param <E>  class
   @return entity
   */
  private <E> Collection<E> getAll(Class<E> type) {
    if (store.containsKey(type))
      //noinspection unchecked
      return (Collection<E>) store.get(type).values();
    return ImmutableList.of();
  }

  /**
   Get all entities from the store

   @return all entities
   */
  public Collection<Object> getAll() {
    return store.values().stream()
      .flatMap(map -> map.values().stream()).collect(Collectors.toList());
  }

  /**
   Get the instrument type for the given audio id

   @param instrumentAudioId for which to get instrument type
   @return instrument type
   @throws HubClientException on failure
   */
  public InstrumentType getInstrumentTypeForAudioId(UUID instrumentAudioId) throws HubClientException {
    return getInstrument(
      getInstrumentAudio(instrumentAudioId)
        .orElseThrow(() -> new HubClientException("Can't get Instrument Audio!"))
        .getInstrumentId())
      .orElseThrow(() -> new HubClientException("Can't get Instrument!"))
      .getType();
  }

  /**
   Get all template bindings
   <p>
   Templates: enhanced preview chain creation for artists in Lab UI #178457569

   @return all template bindings
   */
  public Collection<TemplateBinding> getAllTemplateBindings() {
    return getAll(TemplateBinding.class);
  }

  /**
   Get the template

   @return template
   @throws HubClientException on failure
   */
  public Template getTemplate() throws HubClientException {
    return getAll(Template.class).stream().findFirst().orElseThrow(() -> new HubClientException("Has no Template"));
  }

}
