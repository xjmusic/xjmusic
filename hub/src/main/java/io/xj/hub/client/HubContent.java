// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.client;

import com.google.common.collect.*;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.ingest.HubContentPayload;
import io.xj.hub.tables.pojos.*;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;
import io.xj.lib.music.Note;
import io.xj.lib.util.Text;

import java.util.*;
import java.util.stream.Collectors;

/**
 * to ingest any combination of Programs, Instruments, or Libraries (with their Programs and Instruments) https://www.pivotaltracker.com/story/show/154350346
 * <p>
 * Refactoring this class ala `HubContent` extends common `EntityStore` implementation https://www.pivotaltracker.com/story/show/173803936
 */
public class HubContent {
  private final Map<Class<?>/*Type*/, Map<UUID/*ID*/, Object>> store = Maps.newConcurrentMap();

  public HubContent(
    Collection<?> entities
  ) throws HubClientException {
    try {
      for (Object entity : entities)
        put(entity);

    } catch (Exception e) {
      throw new HubClientException(e);
    }
  }

  /**
   * Create a hub content object from a payload
   *
   * @param payload from which to get content object
   * @return hub content
   * @throws HubClientException on failure
   */
  public static HubContent from(HubContentPayload payload) throws HubClientException {
    return new HubContent(payload.getAllEntities());
  }

  /**
   * Get all available sequence pattern offsets of a given sequence, sorted by offset
   *
   * @param sequenceBinding to get available sequence pattern offsets for
   * @return collection of available sequence pattern offsets
   */
  public Collection<Integer> getAvailableOffsets(ProgramSequenceBinding sequenceBinding) {
    return getProgramSequenceBindings().stream()
      .filter(psb -> psb.getProgramId().equals(sequenceBinding.getProgramId()))
      .map(ProgramSequenceBinding::getOffset)
      .distinct()
      .sorted(Integer::compareTo)
      .collect(Collectors.toList());
  }

  /**
   * Get all Audios for a given instrument id
   *
   * @param id of instrument to get audios for
   * @return audios of instrument id
   */
  public Collection<InstrumentAudio> getAudiosForInstrumentId(UUID id) {
    return getInstrumentAudios().stream()
      .filter(a -> id.equals(a.getInstrumentId()))
      .collect(Collectors.toList());
  }

  /**
   * Get all InstrumentAudios for a given Instrument
   *
   * @param instrument to get audios for
   * @return audios for instrument
   */
  public Collection<InstrumentAudio> getAudios(Instrument instrument) {
    return getAudiosForInstrumentId(instrument.getId());
  }

  /**
   * Get sequence bindings at a specified offset.
   * If the target offset is not found in the chosen Main Program,
   * we'll find the nearest matching offset, and return all bindings at that offset.
   * <p>
   * Chain should always be able to determine main sequence binding offset https://www.pivotaltracker.com/story/show/177052278
   *
   * @param program to get sequence bindings for
   * @param offset  to get sequence bindings at
   * @return sequence bindings at offset
   */
  public Collection<ProgramSequenceBinding> getBindingsAtOffset(Program program, Integer offset) {
    return getBindingsAtOffset(program.getId(), offset);
  }

  /**
   * Get sequence bindings at a specified offset.
   * If the target offset is not found in the chosen Main Program,
   * we'll find the nearest matching offset, and return all bindings at that offset.
   * <p>
   * Chain should always be able to determine main sequence binding offset https://www.pivotaltracker.com/story/show/177052278
   *
   * @param programId to get sequence bindings for
   * @param offset    to get sequence bindings at
   * @return sequence bindings at offset
   */
  public Collection<ProgramSequenceBinding> getBindingsAtOffset(UUID programId, Integer offset) {
    var candidates = getProgramSequenceBindings().stream()
      .filter(psb -> Objects.equals(psb.getProgramId(), programId)).toList();
    var actualOffset = candidates.stream()
      .map(ProgramSequenceBinding::getOffset)
      .min(Comparator.comparing(psbOffset -> Math.abs(psbOffset - offset)));
    if (actualOffset.isEmpty())
      return ImmutableList.of();
    return getProgramSequenceBindings().stream()
      .filter(psb ->
        Objects.equals(psb.getProgramId(), programId) &&
          Objects.equals(psb.getOffset(), actualOffset.get()))
      .collect(Collectors.toList());
  }

  /**
   * Get all ProgramSequenceChords for a given Sequence
   *
   * @param sequence to get chords for
   * @return chords of sequence
   */
  public Collection<ProgramSequenceChord> getChords(ProgramSequence sequence) {
    return getProgramSequenceChords().stream()
      .filter(e -> sequence.getId().equals(e.getProgramSequenceId()))
      .collect(Collectors.toList());
  }

  /**
   * Get events for a given program
   *
   * @param programId to get events for
   * @return events for given program
   */
  public Collection<ProgramSequencePatternEvent> getEvents(UUID programId) {
    return getProgramSequencePatternEvents().stream()
      .filter(m -> programId.equals(m.getProgramId()))
      .collect(Collectors.toList());
  }

  /**
   * Get events for a given program pattern
   *
   * @param pattern to get events for
   * @return events for given program pattern
   */
  public List<ProgramSequencePatternEvent> getEvents(ProgramSequencePattern pattern) {
    return getEventsForPatternId(pattern.getId());
  }

  /**
   * Get events for a given program sequence pattern id
   *
   * @param patternId for which to get events
   * @return events for given pattern id
   */
  public List<ProgramSequencePatternEvent> getEventsForPatternId(UUID patternId) {
    return getProgramSequencePatternEvents().stream()
      .filter(m -> patternId.equals(m.getProgramSequencePatternId()))
      .sorted(Comparator.comparing(ProgramSequencePatternEvent::getPosition))
      .collect(Collectors.toList());
  }

  /**
   * get cached Instrument by id
   *
   * @param id of Instrument to get
   * @return Instrument
   */
  public Optional<Instrument> getInstrument(UUID id) {
    return get(Instrument.class, id);
  }

  /**
   * get cached InstrumentAudio by id
   *
   * @param id of InstrumentAudio to get
   * @return InstrumentAudio
   */
  public Optional<InstrumentAudio> getInstrumentAudio(UUID id) {
    return get(InstrumentAudio.class, id);
  }

  /**
   * get all cached InstrumentAudios
   *
   * @return cached InstrumentAudios
   */
  public Collection<InstrumentAudio> getInstrumentAudios() {
    return getAll(InstrumentAudio.class);
  }

  /**
   * Get all instrument audios for the given instrument id
   *
   * @param instrumentId for which to get tracks
   * @return tracks for instrument
   */
  public Collection<InstrumentAudio> getInstrumentAudios(UUID instrumentId) {
    return getAll(InstrumentAudio.class).stream()
      .filter(track -> Objects.equals(instrumentId, track.getInstrumentId()))
      .toList();
  }

  /**
   * Get all instrument audios for the given instrument type
   *
   * @param types of instrument
   * @param modes of instrument
   * @return all audios for instrument type
   */
  public Collection<InstrumentAudio> getInstrumentAudios(Collection<InstrumentType> types, Collection<InstrumentMode> modes) {
    return getInstruments(types, modes).stream()
      .flatMap(instrument -> getInstrumentAudios(instrument.getId()).stream())
      .toList();
  }

  /**
   * get all cached InstrumentMemes
   *
   * @return cached InstrumentMemes
   */
  public Collection<InstrumentMeme> getInstrumentMemes() {
    return getAll(InstrumentMeme.class);
  }

  /**
   * Get memes of instrument
   *
   * @param instrumentId to get memes for
   * @return memes of instrument
   */
  public Collection<InstrumentMeme> getInstrumentMemes(UUID instrumentId) {
    return getAll(InstrumentMeme.class).stream()
      .filter(m -> instrumentId.equals(m.getInstrumentId()))
      .collect(Collectors.toList());
  }

  /**
   * get all cached Instruments
   *
   * @return cached Instruments
   */
  public Collection<Instrument> getInstruments() {
    return getAll(Instrument.class);
  }

  /**
   * Get a collection of all instruments of a particular type for ingest
   *
   * @return collection of instruments
   */
  public Collection<Instrument> getInstruments(InstrumentType type) {
    return getInstruments().stream()
      .filter(instrument -> type.equals(instrument.getType()))
      .collect(Collectors.toList());
  }

  /**
   * Get a collection of all instruments of a particular type for ingest
   *
   * @param types of instrument; empty list is a wildcard
   * @param modes of instrument; empty list is a wildcard
   * @return collection of instruments
   */
  public Collection<Instrument> getInstruments(Collection<InstrumentType> types, Collection<InstrumentMode> modes) {
    return getInstruments().stream()
      .filter(instrument -> modes.isEmpty() || modes.contains(instrument.getMode()))
      .filter(instrument -> types.isEmpty() || types.contains(instrument.getType()))
      .collect(Collectors.toList());
  }

  /**
   * Get the instrument type for the given audio id
   *
   * @param instrumentAudioId for which to get instrument type
   * @return instrument type
   * @throws HubClientException on failure
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
   * Get the instrument type for the given event
   *
   * @param event for which to get instrument type
   * @return instrument type
   * @throws HubClientException on failure
   */
  public InstrumentType getInstrumentTypeForEvent(ProgramSequencePatternEvent event) throws HubClientException {
    return
      getVoice(event)
        .orElseThrow(() -> new HubClientException("Can't get Program Voice!"))
        .getType();
  }

  /**
   * Get memes of instrument
   *
   * @param instrumentId to get memes for
   * @return memes of instrument
   */
  public Collection<InstrumentMeme> getMemesForInstrumentId(UUID instrumentId) {
    return getInstrumentMemes().stream()
      .filter(m -> instrumentId.equals(m.getInstrumentId()))
      .collect(Collectors.toList());
  }

  /**
   * Get memes of program
   *
   * @param programId to get memes for
   * @return memes of program
   */
  public Collection<ProgramMeme> getMemesForProgramId(UUID programId) {
    return getProgramMemes(programId);
  }

  /**
   * Get all program sequence binding memes for program sequence binding
   *
   * @param programSequenceBindingId to get memes for
   * @return memes
   */
  public Collection<ProgramSequenceBindingMeme> getMemesForProgramSequenceBindingId(UUID programSequenceBindingId) {
    return getProgramSequenceBindingMemes().stream()
      .filter(m -> programSequenceBindingId.equals(m.getProgramSequenceBindingId()))
      .collect(Collectors.toList());
  }

  /**
   * Fetch all memes for a given program at sequence binding offset 0
   *
   * @return collection of sequence memes
   */
  public Collection<String> getMemesAtBeginning(Program program) {
    Map<String, Boolean> memes = Maps.newHashMap();

    // add sequence memes
    getMemesForProgramId(program.getId()).forEach((meme ->
      memes.put(meme.getName(), true)));

    // add sequence binding memes
    for (ProgramSequenceBinding sequenceBinding : getBindingsAtOffset(program, 0))
      for (ProgramSequenceBindingMeme meme : getMemesForProgramSequenceBindingId(sequenceBinding.getId()))
        memes.put(meme.getName(), true);

    return memes.keySet();
  }

  /**
   * Get the pattern id for an event id
   *
   * @param eventId for which to get pattern
   * @return pattern id
   */
  public UUID getPatternIdForEventId(UUID eventId) throws HubClientException {
    return getProgramSequencePatternEvent(eventId)
      .orElseThrow(() -> new HubClientException(String.format("content does not content ProgramSequencePatternEvent[%s]", eventId)))
      .getProgramSequencePatternId();
  }

  /**
   * get cached Program by id
   *
   * @param id of Program to get
   * @return Program
   */
  public Optional<Program> getProgram(UUID id) {
    return get(Program.class, id);
  }

  /**
   * get all cached Programs
   *
   * @return cached Programs
   */
  public Collection<Program> getPrograms() {
    return getAll(Program.class);
  }

  /**
   * Get a collection of all sequences of a particular type for ingest
   *
   * @return collection of sequences
   */
  public Collection<Program> getPrograms(ProgramType type) {
    return getPrograms().stream()
      .filter(program -> program.getType().equals(type))
      .collect(Collectors.toList());
  }

  /**
   * Get all program memes
   *
   * @return memes of program
   */
  public Collection<ProgramMeme> getProgramMemes() {
    return getAll(ProgramMeme.class);
  }

  /**
   * Get memes of program
   *
   * @param programId to get memes for
   * @return memes of program
   */
  public Collection<ProgramMeme> getProgramMemes(UUID programId) {
    return getAll(ProgramMeme.class).stream()
      .filter(m -> programId.equals(m.getProgramId()))
      .collect(Collectors.toList());
  }

  /**
   * get cached ProgramSequence by id
   *
   * @param id of ProgramSequence to get
   * @return ProgramSequence
   */
  public Optional<ProgramSequence> getProgramSequence(UUID id) {
    return get(ProgramSequence.class, id);
  }

  /**
   * Get the program sequence for a given program sequence binding
   *
   * @param sequenceBinding to get program sequence for
   * @return program sequence for the given program sequence binding
   */
  public Optional<ProgramSequence> getProgramSequence(ProgramSequenceBinding sequenceBinding) {
    return getProgramSequence(sequenceBinding.getProgramSequenceId());
  }

  /**
   * get all cached ProgramSequences
   *
   * @return cached ProgramSequences
   */
  public Collection<ProgramSequence> getProgramSequences() {
    return getAll(ProgramSequence.class);
  }

  /**
   * get all cached ProgramSequences
   *
   * @param mainProgramId to search for sequences
   * @return cached ProgramSequences
   */
  public Collection<ProgramSequence> getProgramSequences(UUID mainProgramId) {
    return getAll(ProgramSequence.class).stream().filter(s -> mainProgramId.equals(s.getProgramId())).toList();
  }

  /**
   * get cached ProgramSequenceBinding by id
   *
   * @param id of ProgramSequenceBinding to get
   * @return ProgramSequenceBinding
   */
  public Optional<ProgramSequenceBinding> getProgramSequenceBinding(UUID id) {
    return get(ProgramSequenceBinding.class, id);
  }

  /**
   * get all cached ProgramSequenceBindings
   *
   * @return cached ProgramSequenceBindings
   */
  public Collection<ProgramSequenceBinding> getProgramSequenceBindings() {
    return getAll(ProgramSequenceBinding.class);
  }

  /**
   * Get all sequence bindings for the given program
   *
   * @param programId for which to get bindings
   * @return sequence bindings
   */
  public Collection<ProgramSequenceBinding> getSequenceBindingsForProgram(UUID programId) {
    return getAll(ProgramSequenceBinding.class).stream().filter(b -> programId.equals(b.getProgramId())).collect(Collectors.toSet());
  }

  /**
   * get all cached ProgramSequenceBindingMemes
   *
   * @return cached ProgramSequenceBindingMemes
   */
  public Collection<ProgramSequenceBindingMeme> getProgramSequenceBindingMemes() {
    return getAll(ProgramSequenceBindingMeme.class);
  }

  /**
   * Get memes for sequence binding
   *
   * @param programSequenceBindingId for which to get memes
   * @return memes for sequence bindings
   */
  public Collection<ProgramSequenceBindingMeme> getMemesForSequenceBinding(UUID programSequenceBindingId) {
    return getAll(ProgramSequenceBindingMeme.class).stream().filter(b -> programSequenceBindingId.equals(b.getProgramSequenceBindingId())).collect(Collectors.toSet());
  }

  /**
   * get cached ProgramSequencePattern by id
   *
   * @param id of ProgramSequencePattern to get
   * @return ProgramSequencePattern
   */
  public Optional<ProgramSequencePattern> getProgramSequencePattern(UUID id) {
    return get(ProgramSequencePattern.class, id);
  }

  /**
   * get all cached ProgramPatterns
   *
   * @return cached ProgramPatterns
   */
  public Collection<ProgramSequencePattern> getProgramSequencePatterns() {
    return getAll(ProgramSequencePattern.class);
  }

  /**
   * get cached ProgramSequencePatternEvent by id
   *
   * @param id of ProgramSequencePatternEvent to get
   * @return ProgramSequencePatternEvent
   */
  public Optional<ProgramSequencePatternEvent> getProgramSequencePatternEvent(UUID id) {
    return get(ProgramSequencePatternEvent.class, id);
  }

  /**
   * get all cached ProgramEvents
   *
   * @return cached ProgramEvents
   */
  public Collection<ProgramSequencePatternEvent> getProgramSequencePatternEvents() {
    return getAll(ProgramSequencePatternEvent.class);
  }

  /**
   * get cached ProgramSequenceChord by id
   *
   * @param id of ProgramSequenceChord to get
   * @return ProgramSequenceChord
   */
  public Optional<ProgramSequenceChord> getProgramSequenceChord(UUID id) {
    return get(ProgramSequenceChord.class, id);
  }

  /**
   * get all cached ProgramSequenceChords
   *
   * @return cached ProgramSequenceChords
   */
  public Collection<ProgramSequenceChord> getProgramSequenceChords() {
    return getAll(ProgramSequenceChord.class);
  }

  /**
   * get all cached ProgramSequenceChords
   *
   * @return cached ProgramSequenceChords
   */
  public Collection<ProgramSequenceChord> getProgramSequenceChords(UUID mainProgramId) {
    return getAll(ProgramSequenceChord.class).stream().filter(s -> mainProgramId.equals(s.getProgramId())).toList();
  }

  /**
   * Get program sequence chord voicings
   *
   * @param programId to get sequence chord voicings of
   * @return sequence chord voicings for program
   */
  public List<ProgramSequenceChordVoicing> getProgramSequenceChordVoicings(UUID programId) {
    return getAll(ProgramSequenceChordVoicing.class).stream()
      .filter(v -> v.getProgramId().equals(programId))
      .filter(v -> Note.containsAnyValidNotes(v.getNotes()))
      .collect(Collectors.toList());
  }

  /**
   * get cached ProgramVoice by id
   *
   * @param id of ProgramVoice to get
   * @return ProgramVoice
   */
  public Optional<ProgramVoice> getProgramVoice(UUID id) {
    return get(ProgramVoice.class, id);
  }

  /**
   * get all cached ProgramVoices
   *
   * @return cached ProgramVoices
   */
  public Collection<ProgramVoice> getProgramVoices() {
    return getAll(ProgramVoice.class);
  }

  /**
   * get cached ProgramVoiceTrack by id
   *
   * @param id of ProgramVoiceTrack to get
   * @return ProgramVoiceTrack
   */
  public Optional<ProgramVoiceTrack> getProgramVoiceTrack(UUID id) {
    return get(ProgramVoiceTrack.class, id);
  }

  /**
   * Get all program voice tracks for the given program id
   *
   * @param programId for which to get tracks
   * @return tracks for program
   */
  public Collection<ProgramVoiceTrack> getProgramVoiceTracks(UUID programId) {
    return getAll(ProgramVoiceTrack.class).stream()
      .filter(track -> Objects.equals(programId, track.getProgramId()))
      .toList();
  }

  /**
   * Get all program voice tracks for the given program type
   *
   * @param type of program
   * @return all voice tracks for program type
   */
  public Collection<ProgramVoiceTrack> getProgramVoiceTracks(ProgramType type) {
    return getPrograms(type).stream()
      .flatMap(program -> getProgramVoiceTracks(program.getId()).stream())
      .toList();
  }

  /**
   * Get the template
   *
   * @return template
   * @throws HubClientException on failure
   */
  public Template getTemplate() throws HubClientException {
    return getAll(Template.class).stream().findFirst().orElseThrow(() -> new HubClientException("Has no Template"));
  }

  /**
   * Get all template bindings
   * <p>
   * Templates: enhanced preview chain creation for artists in Lab UI https://www.pivotaltracker.com/story/show/178457569
   *
   * @return all template bindings
   */
  public Collection<TemplateBinding> getTemplateBindings() {
    return getAll(TemplateBinding.class);
  }

  /**
   * Get Program track for a given program event
   *
   * @param event to get program track of
   * @return Program track for the given program event
   */
  public Optional<ProgramVoiceTrack> getTrack(ProgramSequencePatternEvent event) {
    return getProgramVoiceTrack(event.getProgramVoiceTrackId());
  }

  /**
   * Get all track names for a given program voice
   *
   * @param voice for which to get track names
   * @return names of tracks for the given voice
   */
  public List<String> getTrackNames(ProgramVoice voice) {
    return getAll(ProgramVoiceTrack.class).stream()
      .filter(t -> voice.getId().equals(t.getProgramVoiceId()))
      .map(ProgramVoiceTrack::getName)
      .toList();
  }

  /**
   * Get all ProgramSequenceChordVoicings for a given Sequence Chord
   *
   * @param chord to get voicings for
   * @return chords of sequence
   */
  public Collection<ProgramSequenceChordVoicing> getVoicings(ProgramSequenceChord chord) {
    return getAll(ProgramSequenceChordVoicing.class).stream()
      .filter(e -> chord.getId().equals(e.getProgramSequenceChordId()))
      .collect(Collectors.toList());
  }

  /**
   * Get Program voice for a given program event
   *
   * @param event to get program voice of
   * @return Program voice for the given program event
   */
  public Optional<ProgramVoice> getVoice(ProgramSequencePatternEvent event) {
    var track = getTrack(event);
    if (track.isEmpty()) return Optional.empty();
    return getProgramVoice(track.get().getProgramVoiceId());
  }

  /**
   * Get all program voices for a given program
   *
   * @param program to get program voices for
   * @return program voices for the given program
   */
  public Collection<ProgramVoice> getVoices(Program program) {
    return getProgramVoices().stream()
      .filter(m -> m.getProgramId().equals(program.getId()))
      .collect(Collectors.toList());
  }

  /**
   * Put an object to the store
   *
   * @param entity to store
   * @throws EntityException on failure
   */
  public HubContent put(Object entity) throws EntityException {
    store.putIfAbsent(entity.getClass(), Maps.newConcurrentMap());
    store.get(entity.getClass()).put(Entities.getId(entity), entity);
    return this;
  }

  /**
   * Override the ship key of a template we found-- in case a template is shipped to a different
   *
   * @param shipKey new value
   * @throws HubClientException on failure
   */
  public void setTemplateShipKey(String shipKey) throws HubClientException {
    try {
      var template = getTemplate();
      template.setShipKey(shipKey);
      put(template);
    } catch (EntityException e) {
      throw new HubClientException(e);
    }
  }

  /**
   * Get a count of total entities in this Hub Content
   *
   * @return total number of entities in this Hub Content
   */
  public int size() {
    return store.values().stream()
      .mapToInt(Map::size)
      .sum();
  }

  @Override
  public String toString() {
    Multiset<String> entityHistogram = ConcurrentHashMultiset.create();
    store.values().stream()
      .flatMap(map -> map.values().stream()).toList()
      .forEach((Object obj) -> entityHistogram.add(Text.getSimpleName(obj)));
    List<String> descriptors = Lists.newArrayList();
    Collection<String> names = Ordering.from(String.CASE_INSENSITIVE_ORDER).sortedCopy(entityHistogram.elementSet());
    names.forEach((String name) -> descriptors.add(String.format("%d %s", entityHistogram.count(name), name)));
    return String.join(", ", descriptors);
  }

  /**
   * Get an entity of a given type and id from the store, or throw an exception
   *
   * @param type to get
   * @param id   to get
   * @param <E>  class
   * @return entity
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
   * Get all entities of a given type from the store, or throw an exception
   *
   * @param type to get
   * @param <E>  class
   * @return entity
   */
  private <E> Collection<E> getAll(Class<E> type) {
    if (store.containsKey(type))
      //noinspection unchecked
      return (Collection<E>) store.get(type).values();
    return ImmutableList.of();
  }

  /**
   * Whether the content contains instruments of the given type
   *
   * @param type of instrument for which to search
   * @return true if present
   */
  public boolean hasInstruments(InstrumentType type) {
    return getInstruments().stream()
      .anyMatch(instrument -> type.equals(instrument.getType()));
  }

  /**
   * Whether the content contains instruments of the given mode
   *
   * @param mode of instrument for which to search
   * @return true if present
   */
  public boolean hasInstruments(InstrumentMode mode) {
    return getInstruments().stream()
      .anyMatch(instrument -> mode.equals(instrument.getMode()));
  }

  /**
   * Whether the content contains instruments of the given type
   *
   * @param type of instrument for which to search
   * @param mode of instrument for which to search
   * @return true if present
   */
  public boolean hasInstruments(InstrumentType type, InstrumentMode mode) {
    return getInstruments().stream()
      .anyMatch(instrument -> type.equals(instrument.getType()) && mode.equals(instrument.getMode()));
  }

}
