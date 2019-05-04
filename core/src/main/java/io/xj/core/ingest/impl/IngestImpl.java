// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.ingest.impl;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.AudioChordDAO;
import io.xj.core.dao.AudioDAO;
import io.xj.core.dao.AudioEventDAO;
import io.xj.core.dao.DAO;
import io.xj.core.dao.InstrumentDAO;
import io.xj.core.dao.InstrumentMemeDAO;
import io.xj.core.dao.LibraryDAO;
import io.xj.core.dao.PatternChordDAO;
import io.xj.core.dao.PatternDAO;
import io.xj.core.dao.PatternEventDAO;
import io.xj.core.dao.SequenceDAO;
import io.xj.core.dao.SequenceMemeDAO;
import io.xj.core.dao.SequencePatternDAO;
import io.xj.core.dao.SequencePatternMemeDAO;
import io.xj.core.dao.VoiceDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.audio_chord.AudioChord;
import io.xj.core.model.audio_event.AudioEvent;
import io.xj.core.model.entity.Entity;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.instrument_meme.InstrumentMeme;
import io.xj.core.model.library.Library;
import io.xj.core.model.meme.Meme;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern_chord.PatternChord;
import io.xj.core.model.pattern_event.PatternEvent;
import io.xj.core.model.sequence.Sequence;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.sequence_meme.SequenceMeme;
import io.xj.core.model.sequence_pattern.SequencePattern;
import io.xj.core.model.sequence_pattern_meme.SequencePatternMeme;
import io.xj.core.model.voice.Voice;
import io.xj.core.util.Text;
import io.xj.core.ingest.Ingest;
import io.xj.music.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 [#154350346] Architect wants a universal Ingest Factory, to modularize graph mathematics used during craft to evaluate any combination of Library, Sequence, and Instrument for any purpose.
 */
public class IngestImpl implements Ingest {
  private final Logger log = LoggerFactory.getLogger(IngestImpl.class);
  private final AudioDAO audioDAO;
  private final AudioEventDAO audioEventDAO;
  private final SequenceDAO sequenceDAO;
  private final InstrumentDAO instrumentDAO;
  private final SequenceMemeDAO sequenceMemeDAO;
  private final InstrumentMemeDAO instrumentMemeDAO;
  private final PatternDAO patternDAO;
  private final SequencePatternMemeDAO sequencePatternMemeDAO;
  private final VoiceDAO voiceDAO;
  private final PatternEventDAO patternEventDAO;
  private final SequencePatternDAO sequencePatternDAO;

  private final Access access;
  private final PatternChordDAO patternChordDAO;
  private final AudioChordDAO audioChordDAO;
  private final LibraryDAO libraryDAO;
  private final Collection<Entity> sourceEntities;
  private final Map<BigInteger, Sequence> sequenceMap = Maps.newConcurrentMap();
  private final Map<BigInteger, SequenceMeme> sequenceMemeMap = Maps.newConcurrentMap();
  private final Map<BigInteger, Pattern> patternMap = Maps.newConcurrentMap();
  private final Map<BigInteger, Instrument> instrumentMap = Maps.newConcurrentMap();
  private final Map<BigInteger, InstrumentMeme> instrumentMemeMap = Maps.newConcurrentMap();
  private final Map<BigInteger, Library> libraryMap = Maps.newConcurrentMap();
  private final Map<BigInteger, Audio> audioMap = Maps.newConcurrentMap();
  private final Map<BigInteger, AudioChord> audioChordMap = Maps.newConcurrentMap();
  private final Map<BigInteger, AudioEvent> audioEventMap = Maps.newConcurrentMap();
  private final Map<BigInteger, PatternChord> patternChordMap = Maps.newConcurrentMap();
  private final Map<BigInteger, Voice> voiceMap = Maps.newConcurrentMap();
  private final Map<BigInteger, PatternEvent> patternEventMap = Maps.newConcurrentMap();
  private final Map<BigInteger, SequencePattern> sequencePatternMap = Maps.newConcurrentMap();
  private final Map<BigInteger, SequencePatternMeme> sequencePatternMemeMap = Maps.newConcurrentMap();

  @Inject
  public IngestImpl(
    @Assisted("access") Access access,
    @Assisted("entities") Collection<Entity> sourceEntities,
    AudioChordDAO audioChordDAO,
    AudioDAO audioDAO,
    AudioEventDAO audioEventDAO,
    InstrumentDAO instrumentDAO,
    InstrumentMemeDAO instrumentMemeDAO,
    LibraryDAO libraryDAO,
    SequenceDAO sequenceDAO,
    SequenceMemeDAO sequenceMemeDAO,
    PatternChordDAO patternChordDAO,
    PatternDAO patternDAO,
    SequencePatternMemeDAO sequencePatternMemeDAO,
    VoiceDAO voiceDAO,
    PatternEventDAO patternEventDAO,
    SequencePatternDAO sequencePatternDAO
  ) {
    this.access = access;
    this.audioChordDAO = audioChordDAO;
    this.audioDAO = audioDAO;
    this.audioEventDAO = audioEventDAO;
    this.sourceEntities = Lists.newArrayList(sourceEntities);
    this.instrumentDAO = instrumentDAO;
    this.instrumentMemeDAO = instrumentMemeDAO;
    this.libraryDAO = libraryDAO;
    this.sequenceDAO = sequenceDAO;
    this.sequenceMemeDAO = sequenceMemeDAO;
    this.patternChordDAO = patternChordDAO;
    this.patternDAO = patternDAO;
    this.sequencePatternMemeDAO = sequencePatternMemeDAO;
    this.voiceDAO = voiceDAO;
    this.patternEventDAO = patternEventDAO;
    this.sequencePatternDAO = sequencePatternDAO;
    readAll(); // must happen before chord progressions are computed
  }

  @Override
    public Map<BigInteger, Sequence> getSequenceMap() {
    return Collections.unmodifiableMap(sequenceMap);
  }

  @Override
    public Map<BigInteger, Pattern> getPatternMap() {
    return Collections.unmodifiableMap(patternMap);
  }

  @Override
    public Map<BigInteger, Instrument> getInstrumentMap() {
    return Collections.unmodifiableMap(instrumentMap);
  }

  @Override
    public Collection<Sequence> getAllSequences() {
    return sequenceMap.values();
  }

  @Override
    public Collection<Sequence> getSequencesOfType(SequenceType type) {
    ImmutableList.Builder<Sequence> result = ImmutableList.builder();
    sequenceMap.values().forEach(sequence -> {
      if (type == sequence.getType())
        result.add(sequence);
    });
    return result.build();
  }

  @Override
  public Access getAccess() {
    return access;
  }

  @Override
    public Sequence getSequence(BigInteger id) throws CoreException {
    return fetchOne(sequenceMap, id);
  }

  @Override
    public SequencePattern getSequencePattern(BigInteger id) throws CoreException {
    return fetchOne(sequencePatternMap, id);
  }

  @Override
    public Collection<SequenceMeme> getAllSequenceMemes() {
    return sequenceMemeMap.values();
  }

  @Override
    public Collection<SequenceMeme> getSequenceMemesOfSequence(BigInteger sequenceId) {
    return fetchMany(sequenceMemeMap, sequenceId);
  }

  @Override
    public Collection<Meme> getMemesAtBeginningOfSequence(BigInteger sequenceId) {
    Map<String, Meme> memes = Maps.newConcurrentMap();

    // add sequence memes
    getSequenceMemesOfSequence(sequenceId).forEach((sequenceMeme ->
      memes.put(sequenceMeme.getName(), sequenceMeme)));

    // add sequence pattern memes
    for (SequencePattern sequencePattern : getSequencePatternsOfSequenceAtOffset(sequenceId, BigInteger.ZERO)) {
      for (SequencePatternMeme sequenceMeme : getSequencePatternMemesOfSequencePattern(sequencePattern.getId())) {
        memes.put(sequenceMeme.getName(), sequenceMeme);
      }
    }

    return memes.values();
  }

  @Override
    public Collection<Pattern> getAllPatterns() {
    return patternMap.values();
  }

  @Override
    public Collection<Pattern> getPatternsOfSequence(BigInteger sequenceId) {
    return fetchMany(patternMap, sequenceId);
  }

  @Override
    public Collection<SequencePattern> getAllSequencePatterns() {
    return sequencePatternMap.values();
  }

  @Override
    public Collection<SequencePatternMeme> getAllSequencePatternMemes() {
    return sequencePatternMemeMap.values();
  }

  @Override
    public Collection<SequencePatternMeme> getSequencePatternMemesOfSequencePattern(BigInteger sequencePatternId) {
    return fetchMany(sequencePatternMemeMap, sequencePatternId);
  }

  @Override
    public Collection<SequencePattern> getSequencePatternsOfSequence(BigInteger sequenceId) {
    return fetchMany(sequencePatternMap, sequenceId);
  }

  @Override
    public Collection<SequencePattern> getSequencePatternsOfSequenceAtOffset(BigInteger sequenceId, BigInteger sequencePatternOffset) {
    Collection<SequencePattern> result = Lists.newArrayList();
    getSequencePatternsOfSequence(sequenceId).forEach(sequencePattern -> {
      if (sequencePatternOffset.equals(sequencePattern.getOffset())) {
        result.add(sequencePattern);
      }
    });
    return result;
  }

  @Override
    public Collection<BigInteger> getAvailableSequencePatternOffsets(BigInteger sequenceId) {
    Multiset<BigInteger> uniqueOffsets = ConcurrentHashMultiset.create();
    getSequencePatternsOfSequence(sequenceId)
      .forEach(sequencePattern -> uniqueOffsets.add(sequencePattern.getOffset()));
    return uniqueOffsets.elementSet();
  }

  @Override
    public Collection<Instrument> getAllInstruments() {
    return instrumentMap.values();
  }

  @Override
    public Collection<Instrument> getInstrumentsOfType(InstrumentType type) {
    ImmutableList.Builder<Instrument> result = ImmutableList.builder();
    instrumentMap.values().forEach(instrument -> {
      if (type == instrument.getType())
        result.add(instrument);
    });
    return result.build();
  }

  @Override
    public Instrument getInstrument(BigInteger id) throws CoreException {
    return fetchOne(instrumentMap, id);
  }

  @Override
    public Collection<AudioEvent> getFirstEventsOfAudiosOfInstrument(BigInteger instrumentId) {
    Map<String, AudioEvent> result = Maps.newConcurrentMap();
    getAudioEventsOfInstrument(instrumentId).forEach(audioEvent -> {
      String key = audioEvent.getAudioId().toString();
      if (result.containsKey(key)) {
        if (audioEvent.getPosition() < result.get(key).getPosition()) {
          result.put(key, audioEvent);
        }
      } else {
        result.put(key, audioEvent);
      }
    });
    return result.values();
  }

  @Override
    public Collection<InstrumentMeme> getAllInstrumentMemes() {
    return instrumentMemeMap.values();
  }

  @Override
    public Collection<InstrumentMeme> getMemesOfInstrument(BigInteger instrumentId) {
    return fetchMany(instrumentMemeMap, instrumentId);
  }

  @Override
    public Collection<Library> getAllLibraries() {
    return libraryMap.values();
  }

  @Override
    public Collection<Audio> getAllAudios() {
    return audioMap.values();
  }

  @Override
    public Collection<Audio> getAudiosOfInstrument(BigInteger instrumentId) {
    return fetchMany(audioMap, instrumentId);
  }

  @Override
    public Collection<AudioEvent> getAudioEventsOfInstrument(BigInteger instrumentId) {
    Collection<AudioEvent> result = Lists.newArrayList();
    getAudiosOfInstrument(instrumentId).forEach(audio -> result.addAll(fetchMany(audioEventMap, audio.getId())));
    return result;
  }

  @Override
    public Audio getAudio(BigInteger id) throws CoreException {
    return fetchOne(audioMap, id);
  }

  @Override
    public Key getKeyOfPattern(BigInteger id) throws CoreException {
    // if null pattern return empty key
    Pattern pattern = fetchOne(patternMap, id);
    if (Objects.isNull(pattern))
      return Key.of("");

    // if pattern has key, use that
    if (Objects.nonNull(pattern.getKey()) && !pattern.getKey().isEmpty())
      return Key.of(pattern.getKey());

    // pattern has no key; use sequence key. if null sequence return empty key
    Sequence sequence = getSequence(pattern.getSequenceId());
    if (Objects.isNull(sequence))
      return Key.of("");
    return Key.of(sequence.getKey());
  }

  @Override
    public Collection<AudioChord> getAllAudioChords() {
    return audioChordMap.values();
  }

  @Override
    public Collection<AudioEvent> getAllAudioEvents() {
    return audioEventMap.values();
  }

  @Override
    public Collection<PatternChord> getAllPatternChords() {
    return patternChordMap.values();
  }

  @Override
    public Collection<PatternChord> getChordsOfPattern(BigInteger patternId) {
    return fetchMany(patternChordMap, patternId);
  }

  @Override
    public Collection<Voice> getAllVoices() {
    return voiceMap.values();
  }

  @Override
    public Collection<Voice> getVoicesOfSequence(BigInteger sequenceId) {
    return fetchMany(voiceMap, sequenceId);
  }

  @Override
    public Collection<PatternEvent> getAllPatternEvents() {
    return patternEventMap.values();
  }

  @Override
    public Collection<PatternEvent> getEventsOfPatternByVoice(BigInteger patternId, BigInteger voiceId) {
    ImmutableList.Builder<PatternEvent> result = ImmutableList.builder();
    patternEventMap.values().forEach(patternEvent -> {
      if (Objects.equals(patternId, patternEvent.getPatternId()) &&
        Objects.equals(voiceId, patternEvent.getVoiceId()))
        result.add(patternEvent);
    });
    return result.build();
  }

  @Override
    public Map<BigInteger, Audio> getAudioMap() {
    return Collections.unmodifiableMap(audioMap);
  }

  @Override
    public Collection<Entity> getAllEntities() {
    ImmutableList.Builder<Entity> result = ImmutableList.builder();
    result.addAll(getAllAudioChords());
    result.addAll(getAllAudioEvents());
    result.addAll(getAllAudios());
    result.addAll(getAllInstrumentMemes());
    result.addAll(getAllInstruments());
    result.addAll(getAllLibraries());
    result.addAll(getAllSequenceMemes());
    result.addAll(getAllSequences());
    result.addAll(getAllPatternChords());
    result.addAll(getAllPatternEvents());
    result.addAll(getAllSequencePatterns());
    result.addAll(getAllSequencePatternMemes());
    result.addAll(getAllPatterns());
    result.addAll(getAllVoices());
    return result.build();
  }

  @Override
    public String toString() {
    return Text.entityHistogram(getAllEntities());
  }

  @Override
    public Pattern fetchOnePattern(BigInteger id) throws CoreException {
    return fetchOne(patternMap, id);
  }

  /**
   Fetch one entity from an entity map

   @param <E>       class of entity
   @param entityMap to fetch from as primary source
   @param entityId  to fetch
   @return entity
   */
  private static <E extends Entity> E fetchOne(Map<BigInteger, E> entityMap, BigInteger entityId) throws CoreException {
    if (entityMap.isEmpty()) {
      throw new CoreException(String.format("Cannot fetch one entity from empty %s map", entityMap.values().getClass()));
    }

    if (Objects.isNull(entityId)) {
      throw new CoreException(String.format("Cannot fetch null id from %s entity map", entityMap.values().iterator().next().getClass().getSimpleName()));
    }

    if (!entityMap.containsKey(entityId)) {
      throw new CoreException(String.format("Cannot fetch entityId=%s from %s entity map", entityId, entityMap.values().iterator().next().getClass().getSimpleName()));
    }

    return entityMap.get(entityId);
  }

  /**
   Fetch all entities of a particular class with a particular parent id

   @param <E>       class of entity
   @param entityMap of all entities to source
   @param parentId  to match
   @return collection of entities matching parent id
   */
  private static <E extends Entity> Collection<E> fetchMany(Map<BigInteger, E> entityMap, BigInteger parentId) {
    ImmutableList.Builder<E> result = ImmutableList.builder();
    entityMap.values().forEach(entity -> {
      if (Objects.equals(parentId, entity.getParentId()))
        result.add(entity);
    });
    return result.build();
  }

  /**
   Read all records via DAO
   for all entities and children
   NOTE: the order of operations inside here is important! hierarchical order, from parents to children
   */
  private void readAll() {
    try {
      readAll(Library.class, libraryMap, libraryDAO);
      readAll(Instrument.class, instrumentMap, libraryMap.keySet(), instrumentDAO);
      readAll(InstrumentMeme.class, instrumentMemeMap, instrumentMap.keySet(), instrumentMemeDAO);
      readAll(Audio.class, audioMap, instrumentMap.keySet(), audioDAO);
      readAll(AudioChord.class, audioChordMap, audioMap.keySet(), audioChordDAO);
      readAll(AudioEvent.class, audioEventMap, audioMap.keySet(), audioEventDAO);
      readAll(Sequence.class, sequenceMap, libraryMap.keySet(), sequenceDAO);
      readAll(SequenceMeme.class, sequenceMemeMap, sequenceMap.keySet(), sequenceMemeDAO);
      readAll(Pattern.class, patternMap, sequenceMap.keySet(), patternDAO);
      readAll(PatternChord.class, patternChordMap, patternMap.keySet(), patternChordDAO);
      readAll(PatternEvent.class, patternEventMap, patternMap.keySet(), patternEventDAO);
      readAll(SequencePattern.class, sequencePatternMap, sequenceMap.keySet(), sequencePatternDAO);
      readAll(SequencePatternMeme.class, sequencePatternMemeMap, sequencePatternMap.keySet(), sequencePatternMemeDAO);
      readAll(Voice.class, voiceMap, sequenceMap.keySet(), voiceDAO);
    } catch (Exception e) {
      log.error("Failed to read all entities for ingest.", e);
    }
  }

  /**
   Read all Entities and put results into map
   NOTE: the order of these operations is important! it is managed by the main readAll() process
   */
  private <E extends Entity> void readAll(Class<E> entityClass, Map<BigInteger, E> entityMap, Collection<BigInteger> parentIds, DAO<E> dao) throws CoreException {
    try {
      dao.readAll(access, parentIds).forEach(entity -> entityMap.put(entity.getId(), entity));

      for (BigInteger id : entityIds(entityMap, entityClass))
        if (!entityMap.containsKey(id))
          entityMap.put(id, dao.readOne(access, id));
    } catch (CoreException e) {
      throw new CoreException(String.format("Failed to retrieve entityMap=%s, parentIds=%s, dao=%s", entityMap, parentIds, dao.getClass().getInterfaces()[0].getName()), e);
    }
  }

  /**
   Read all Entities and put results into map
   NOTE: the order of these operations is important! it is managed by the main readAll() process
   */
  private <E extends Entity> void readAll(Class<E> entityClass, Map<BigInteger, E> entityMap, DAO<E> dao) throws CoreException {
    try {
      for (BigInteger id : entityIds(entityMap, entityClass))
        if (!entityMap.containsKey(id))
          entityMap.put(id, dao.readOne(access, id));
    } catch (CoreException e) {
      throw new CoreException(String.format("Failed to retrieve entityMap=%s, dao=%s", entityMap, dao.getClass().getInterfaces()[0].getName()), e);
    }
  }

  /**
   Get collection of Id, starting with a map of entity, and adding entity filtered by class from all source entities

   @param entityClass use only this class of source entities
   @return collection of id
   */
  private <E extends Entity> Collection<BigInteger> entityIds(Map<BigInteger, E> baseEntities, Class<E> entityClass) {
    Map<BigInteger, Boolean> result = Maps.newConcurrentMap();
    baseEntities.forEach((id, entity) -> result.put(id, true));
    sourceEntities.forEach(entity -> {
      if (entityClass.equals(entity.getClass())) {
        result.put(entity.getId(), true);
      }
    });
    return result.keySet();
  }

}
