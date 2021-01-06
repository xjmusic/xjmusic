// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.client;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.inject.Inject;
import io.xj.Instrument;
import io.xj.InstrumentAudio;
import io.xj.InstrumentAudioChord;
import io.xj.InstrumentAudioEvent;
import io.xj.InstrumentMeme;
import io.xj.Program;
import io.xj.ProgramMeme;
import io.xj.ProgramSequence;
import io.xj.ProgramSequenceBinding;
import io.xj.ProgramSequenceBindingMeme;
import io.xj.ProgramSequenceChord;
import io.xj.ProgramSequenceChordVoicing;
import io.xj.ProgramSequencePattern;
import io.xj.ProgramSequencePatternEvent;
import io.xj.ProgramVoice;
import io.xj.ProgramVoiceTrack;
import io.xj.lib.entity.Entities;
import io.xj.lib.util.Text;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 [#154350346] to ingest any combination of Programs, Instruments, or Libraries (with their Programs and Instruments)
 <p>
 Refactoring this class ala [#173803936] `HubContent` extends common `EntityStore` implementation
 */
public class HubContent {
  private final Map<Class<?>/*Type*/, Map<String/*ID*/, Object>> store = Maps.newConcurrentMap();

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
   Get all available sequence pattern offsets of a given sequence

   @param sequenceBinding to get available sequence pattern offsets for
   @return collection of available sequence pattern offsets
   */
  public Collection<Long> getAvailableOffsets(ProgramSequenceBinding sequenceBinding) throws HubClientException {
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
  public InstrumentAudioChord getInstrumentAudioChord(String id) throws HubClientException {
    return get(InstrumentAudioChord.class, id);
  }

  /**
   get cached InstrumentAudio by id

   @param id of InstrumentAudio to get
   @return InstrumentAudio
   */
  public InstrumentAudio getInstrumentAudio(String id) throws HubClientException {
    return get(InstrumentAudio.class, id);
  }

  /**
   get cached InstrumentAudioEvent by id

   @param id of InstrumentAudioEvent to get
   @return InstrumentAudioEvent
   */
  public InstrumentAudioEvent getInstrumentAudioEvent(String id) throws HubClientException {
    return get(InstrumentAudioEvent.class, id);
  }

  /**
   get cached Instrument by id

   @param id of Instrument to get
   @return Instrument
   */
  public Instrument getInstrument(String id) throws HubClientException {
    return get(Instrument.class, id);
  }

  /**
   get cached InstrumentMeme by id

   @param id of InstrumentMeme to get
   @return InstrumentMeme
   */
  public InstrumentMeme getInstrumentMeme(String id) throws HubClientException {
    return get(InstrumentMeme.class, id);
  }

  /**
   get cached Program by id

   @param id of Program to get
   @return Program
   */
  public Program getProgram(String id) throws HubClientException {
    return get(Program.class, id);
  }

  /**
   get cached ProgramSequencePatternEvent by id

   @param id of ProgramSequencePatternEvent to get
   @return ProgramSequencePatternEvent
   */
  public ProgramSequencePatternEvent getProgramSequencePatternEvent(String id) throws HubClientException {
    return get(ProgramSequencePatternEvent.class, id);
  }

  /**
   get cached ProgramMeme by id

   @param id of ProgramMeme to get
   @return ProgramMeme
   */
  public ProgramMeme getProgramMeme(String id) throws HubClientException {
    return get(ProgramMeme.class, id);
  }

  /**
   get cached ProgramSequencePattern by id

   @param id of ProgramSequencePattern to get
   @return ProgramSequencePattern
   */
  public ProgramSequencePattern getProgramSequencePattern(String id) throws HubClientException {
    return get(ProgramSequencePattern.class, id);
  }

  /**
   get cached ProgramSequenceBinding by id

   @param id of ProgramSequenceBinding to get
   @return ProgramSequenceBinding
   */
  public ProgramSequenceBinding getProgramSequenceBinding(String id) throws HubClientException {
    return get(ProgramSequenceBinding.class, id);
  }

  /**
   get cached ProgramSequenceBindingMeme by id

   @param id of ProgramSequenceBindingMeme to get
   @return ProgramSequenceBindingMeme
   */
  public ProgramSequenceBindingMeme getProgramSequenceBindingMeme(String id) throws HubClientException {
    return get(ProgramSequenceBindingMeme.class, id);
  }

  /**
   get cached ProgramSequenceChord by id

   @param id of ProgramSequenceChord to get
   @return ProgramSequenceChord
   */
  public ProgramSequenceChord getProgramSequenceChord(String id) throws HubClientException {
    return get(ProgramSequenceChord.class, id);
  }

  /**
   get cached ProgramSequence by id

   @param id of ProgramSequence to get
   @return ProgramSequence
   */
  public ProgramSequence getProgramSequence(String id) throws HubClientException {
    return get(ProgramSequence.class, id);
  }

  /**
   get cached ProgramVoiceTrack by id

   @param id of ProgramVoiceTrack to get
   @return ProgramVoiceTrack
   */
  public ProgramVoiceTrack getProgramVoiceTrack(String id) throws HubClientException {
    return get(ProgramVoiceTrack.class, id);
  }

  /**
   get cached ProgramVoice by id

   @param id of ProgramVoice to get
   @return ProgramVoice
   */
  public ProgramVoice getProgramVoice(String id) throws HubClientException {
    return get(ProgramVoice.class, id);
  }

  /**
   get all cached Programs

   @return cached Programs
   */
  public Collection<Program> getAllPrograms() throws HubClientException {
    return getAll(Program.class);
  }

  /**
   get all cached InstrumentAudios

   @return cached InstrumentAudios
   */
  public Collection<InstrumentAudio> getAllInstrumentAudios() throws HubClientException {
    return getAll(InstrumentAudio.class);
  }

  /**
   get all cached InstrumentAudioChords

   @return cached InstrumentAudioChords
   */
  public Collection<InstrumentAudioChord> getAllInstrumentAudioChords() throws HubClientException {
    return getAll(InstrumentAudioChord.class);
  }

  /**
   get all cached InstrumentAudioEvents

   @return cached InstrumentAudioEvents
   */
  public Collection<InstrumentAudioEvent> getAllInstrumentAudioEvents() throws HubClientException {
    return getAll(InstrumentAudioEvent.class);
  }

  /**
   get all cached InstrumentMemes

   @return cached InstrumentMemes
   */
  public Collection<InstrumentMeme> getAllInstrumentMemes() throws HubClientException {
    return getAll(InstrumentMeme.class);
  }

  /**
   get all cached ProgramEvents

   @return cached ProgramEvents
   */
  public Collection<ProgramSequencePatternEvent> getAllProgramSequencePatternEvents() throws HubClientException {
    return getAll(ProgramSequencePatternEvent.class);
  }

  /**
   get all cached ProgramMemes

   @return cached ProgramMemes
   */
  public Collection<ProgramMeme> getAllProgramMemes() throws HubClientException {
    return getAll(ProgramMeme.class);
  }

  /**
   get all cached ProgramPatterns

   @return cached ProgramPatterns
   */
  public Collection<ProgramSequencePattern> getAllProgramSequencePatterns() throws HubClientException {
    return getAll(ProgramSequencePattern.class);
  }

  /**
   get all cached ProgramSequences

   @return cached ProgramSequences
   */
  public Collection<ProgramSequence> getAllProgramSequences() throws HubClientException {
    return getAll(ProgramSequence.class);
  }

  /**
   get all cached ProgramSequenceBindings

   @return cached ProgramSequenceBindings
   */
  public Collection<ProgramSequenceBinding> getAllProgramSequenceBindings() throws HubClientException {
    return getAll(ProgramSequenceBinding.class);
  }

  /**
   get all cached ProgramSequenceBindingMemes

   @return cached ProgramSequenceBindingMemes
   */
  public Collection<ProgramSequenceBindingMeme> getAllProgramSequenceBindingMemes() throws HubClientException {
    return getAll(ProgramSequenceBindingMeme.class);
  }

  /**
   get all cached ProgramSequenceChords

   @return cached ProgramSequenceChords
   */
  public Collection<ProgramSequenceChord> getAllProgramSequenceChords() throws HubClientException {
    return getAll(ProgramSequenceChord.class);
  }

  /**
   get all cached ProgramSequenceChordVoicings

   @return cached ProgramSequenceChordVoicings
   */
  public Collection<ProgramSequenceChordVoicing> getAllProgramSequenceChordVoicings() throws HubClientException {
    return getAll(ProgramSequenceChordVoicing.class);
  }

  /**
   get all cached ProgramTracks

   @return cached ProgramTracks
   */
  public Collection<ProgramVoiceTrack> getAllProgramVoiceTracks() throws HubClientException {
    return getAll(ProgramVoiceTrack.class);
  }

  /**
   get all cached ProgramVoices

   @return cached ProgramVoices
   */
  public Collection<ProgramVoice> getAllProgramVoices() throws HubClientException {
    return getAll(ProgramVoice.class);
  }

  /**
   get all cached Instruments

   @return cached Instruments
   */
  public Collection<Instrument> getAllInstruments() throws HubClientException {
    return getAll(Instrument.class);
  }

  /**
   Get a collection of all sequences of a particular type for ingest

   @return collection of sequences
   */
  public Collection<Program> getProgramsOfType(Program.Type type) throws HubClientException {
    return getAllPrograms().stream()
      .filter(program -> program.getType().equals(type))
      .collect(Collectors.toList());
  }

  /**
   Get a collection of all instruments of a particular type for ingest

   @return collection of instruments
   */
  public Collection<Instrument> getInstrumentsOfType(Instrument.Type type) throws HubClientException {
    return getAllInstruments().stream()
      .filter(instrument -> instrument.getType().equals(type))
      .collect(Collectors.toList());
  }

  /**
   Get a string representation of the ingest
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
  public Collection<ProgramMeme> getMemes(Program program) throws HubClientException {
    return getAllProgramMemes().stream()
      .filter(m -> m.getProgramId().equals(program.getId()))
      .collect(Collectors.toList());
  }

  /**
   Get events for a given program

   @param program to get events for
   @return events for given program
   */
  public Collection<ProgramSequencePatternEvent> getEvents(Program program) throws HubClientException {
    return getAllProgramSequencePatternEvents().stream()
      .filter(m -> m.getProgramId().equals(program.getId()))
      .collect(Collectors.toList());
  }

  /**
   Get events for a given program pattern

   @param programPattern to get events for
   @return events for given program pattern
   */
  public Collection<ProgramSequencePatternEvent> getEvents(ProgramSequencePattern programPattern) throws HubClientException {
    return getAllProgramSequencePatternEvents().stream()
      .filter(m -> m.getProgramSequencePatternId().equals(programPattern.getId()))
      .collect(Collectors.toList());
  }

  /**
   Get memes of instrument

   @param instrument to get memes for
   @return memes of instrument
   */
  public Collection<InstrumentMeme> getMemes(Instrument instrument) throws HubClientException {
    return getAllInstrumentMemes().stream()
      .filter(m -> m.getInstrumentId().equals(instrument.getId()))
      .collect(Collectors.toList());
  }

  /**
   Get all Audios for a given instrument id

   @param uuid of instrument to get audios for
   @return audios of instrument id
   */
  public Collection<InstrumentAudio> getAudiosForInstrumentId(String uuid) throws HubClientException {
    return getAllInstrumentAudios().stream()
      .filter(a -> a.getInstrumentId().equals(uuid))
      .collect(Collectors.toList());
  }

  /**
   Get all InstrumentAudios for a given Instrument

   @param instrument to get audios for
   @return audios for instrument
   */
  public Collection<InstrumentAudio> getAudios(Instrument instrument) throws HubClientException {
    return getAudiosForInstrumentId(instrument.getId());
  }

  /**
   Get all AudioEvents for a given Audio

   @param audio to get events for
   @return events of audio
   */
  public Collection<InstrumentAudioEvent> getEvents(InstrumentAudio audio) throws HubClientException {
    return getAllInstrumentAudioEvents().stream()
      .filter(e -> e.getInstrumentAudioId().equals(audio.getId()))
      .collect(Collectors.toList());
  }

  /**
   Get all AudioChords for a given Audio

   @param audio to get chords for
   @return chords of audio
   */
  public Collection<InstrumentAudioChord> getChords(InstrumentAudio audio) throws HubClientException {
    return getAllInstrumentAudioChords().stream()
      .filter(e -> e.getInstrumentAudioId().equals(audio.getId()))
      .collect(Collectors.toList());
  }

  /**
   Get all ProgramSequenceChords for a given Sequence

   @param sequence to get chords for
   @return chords of sequence
   */
  public Collection<ProgramSequenceChord> getChords(ProgramSequence sequence) throws HubClientException {
    return getAllProgramSequenceChords().stream()
      .filter(e -> e.getProgramSequenceId().equals(sequence.getId()))
      .collect(Collectors.toList());
  }

  /**
   Get all ProgramSequenceChordVoicings for a given Sequence Chord

   @param chord to get voicings for
   @return chords of sequence
   */
  public Collection<ProgramSequenceChordVoicing> getVoicings(ProgramSequenceChord chord) throws HubClientException {
    return getAllProgramSequenceChordVoicings().stream()
      .filter(e -> e.getProgramSequenceChordId().equals(chord.getId()))
      .collect(Collectors.toList());
  }

  /**
   Get all program sequence binding memes for program sequence binding

   @param programSequenceBinding to get memes for
   @return memes
   */
  public Collection<ProgramSequenceBindingMeme> getMemes(ProgramSequenceBinding programSequenceBinding) throws HubClientException {
    return getAllProgramSequenceBindingMemes().stream()
      .filter(m -> m.getProgramSequenceBindingId().equals(programSequenceBinding.getId()))
      .collect(Collectors.toList());
  }

  /**
   Get all program sequence bindings for a given program

   @param program to get sequence bindings for
   @return all sequence bindings for given program
   */
  public Collection<ProgramSequenceBinding> getSequenceBindings(Program program) throws HubClientException {
    return getAllProgramSequenceBindings().stream()
      .filter(m -> m.getProgramId().equals(program.getId()))
      .collect(Collectors.toList());
  }

  /**
   Get all program sequences for a given program

   @param program to get program sequences for
   @return all program sequences for given program
   */
  public Collection<ProgramSequence> getSequences(Program program) throws HubClientException {
    return getAllProgramSequences().stream()
      .filter(m -> m.getProgramId().equals(program.getId()))
      .collect(Collectors.toList());
  }

  /**
   Get the program sequence for a given program sequence binding

   @param sequenceBinding to get program sequence for
   @return program sequence for the given program sequence binding
   */
  public ProgramSequence getProgramSequence(ProgramSequenceBinding sequenceBinding) throws HubClientException {
    return getProgramSequence(sequenceBinding.getProgramSequenceId());
  }

  /**
   Read all AudioEvent that are first in an audio, for all audio in an Instrument

   @param instrument to get audio for
   @return audio events
   */
  public Collection<InstrumentAudioEvent> getFirstEventsOfAudiosOfInstrument(Instrument instrument) throws HubClientException {
    Map<String, InstrumentAudioEvent> result = Maps.newHashMap();
    for (InstrumentAudio audio : getAudios(instrument)) {
      getEvents(audio).stream().filter(search -> search.getInstrumentAudioId().equals(audio.getId())).forEach(audioEvent -> {
        String key = audioEvent.getInstrumentAudioId();
        if (result.containsKey(key)) {
          if (audioEvent.getPosition() < result.get(key).getPosition()) {
            result.put(key, audioEvent);
          }
        } else {
          result.put(key, audioEvent);
        }
      });
    }
    return result.values();
  }

  /**
   Fetch all memes for a given program at sequence binding offset 0

   @return collection of sequence memes
   */
  public Collection<String> getMemesAtBeginning(Program program) throws HubClientException {
    Map<String, Boolean> memes = Maps.newHashMap();

    // add sequence memes
    getMemes(program).forEach((meme ->
      memes.put(meme.getName(), true)));

    // add sequence binding memes
    for (ProgramSequenceBinding sequenceBinding : getProgramSequenceBindingsAtOffset(program, 0L))
      for (ProgramSequenceBindingMeme meme : getMemes(sequenceBinding))
        memes.put(meme.getName(), true);

    return memes.keySet();
  }

  /**
   Get sequence bindings at a specified offset

   @param program to get sequence bindings for
   @param offset  to get sequence bindings at
   @return sequence bindings at offset
   */
  public Collection<ProgramSequenceBinding> getProgramSequenceBindingsAtOffset(Program program, Long offset) throws HubClientException {
    return getAllProgramSequenceBindings().stream()
      .filter(psb ->
        Objects.equals(psb.getProgramId(), program.getId()) &&
          Objects.equals(psb.getOffset(), offset))
      .collect(Collectors.toList());
  }

  /**
   Get program sequence chord voicings

   @param programId to get sequence chord voicings of
   @return sequence chord voicings for program
   */
  public List<ProgramSequenceChordVoicing> getProgramSequenceChordVoicings(String programId) throws HubClientException {
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
  public ProgramVoice getVoice(ProgramSequencePatternEvent event) throws HubClientException {
    return getProgramVoice(getTrack(event).getProgramVoiceId());
  }

  /**
   Get Program track for a given program event

   @param event to get program track of
   @return Program track for the given program event
   */
  public ProgramVoiceTrack getTrack(ProgramSequencePatternEvent event) throws HubClientException {
    return getProgramVoiceTrack(event.getProgramVoiceTrackId());
  }

  /**
   Get all program voices for a given program

   @param program to get program voices for
   @return program voices for the given program
   */
  public Collection<ProgramVoice> getVoices(Program program) throws HubClientException {
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
   @throws HubClientException on failure, or if nonexistent
   */
  private <E> E get(Class<E> type, String id) throws HubClientException {
    try {
      if (store.containsKey(type) && store.get(type).containsKey(id))
        //noinspection unchecked
        return (E) store.get(type).get(id);
      // otherwise
      throw new HubClientException(String.format("%s[%s] not in content!", type.getSimpleName(), id));

    } catch (Exception e) {
      throw new HubClientException(String.format("Failed to retrieve %s[%s]!", type.getSimpleName(), id), e);
    }
  }

  /**
   Get all entities of a given type from the store, or throw an exception

   @param type to get
   @param <E>  class
   @return entity
   @throws HubClientException on failure, or if nonexistent
   */
  private <E> Collection<E> getAll(Class<E> type) throws HubClientException {
    try {
      if (store.containsKey(type))
        //noinspection unchecked
        return (Collection<E>) store.get(type).values();
      return ImmutableList.of();

    } catch (Exception e) {
      throw new HubClientException(String.format("Failed to retrieve all %s!", type.getSimpleName()), e);
    }
  }

  /**
   Get all entities from the store

   @return all entities
   */
  public Collection<Object> getAll() {
    return store.values().stream()
      .flatMap(map -> map.values().stream()).collect(Collectors.toList());
  }

}
