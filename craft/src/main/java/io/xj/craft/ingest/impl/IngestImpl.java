// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.ingest.impl;

import com.google.common.collect.*;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.core.access.impl.Access;
import io.xj.core.cache.entity.EntityCacheProvider;
import io.xj.core.dao.*;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.audio_chord.AudioChord;
import io.xj.core.model.audio_event.AudioEvent;
import io.xj.core.model.entity.Entity;
import io.xj.core.model.entity.EntityRank;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.instrument_meme.InstrumentMeme;
import io.xj.core.model.library.Library;
import io.xj.core.model.meme.Meme;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.pattern_chord.PatternChord;
import io.xj.core.model.pattern_event.PatternEvent;
import io.xj.core.model.pattern_meme.PatternMeme;
import io.xj.core.model.sequence.Sequence;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.sequence_meme.SequenceMeme;
import io.xj.core.model.voice.Voice;
import io.xj.core.util.Chance;
import io.xj.core.util.Text;
import io.xj.craft.ingest.Ingest;
import io.xj.music.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.*;

/**
 [#154350346] Architect wants a universal Ingest Factory, to modularize graph mathematics used during craft to evaluate any combination of Library, Sequence, and Instrument for any purpose.
 */
public class IngestImpl implements Ingest {
  private final Logger log = LoggerFactory.getLogger(IngestImpl.class);
  private final AudioDAO audioDAO;
  private final AudioEventDAO audioEventDAO;
  private final SequenceDAO sequenceDAO;
  private final EntityCacheProvider entityCacheProvider;
  private final InstrumentDAO instrumentDAO;
  private final SequenceMemeDAO sequenceMemeDAO;
  private final InstrumentMemeDAO instrumentMemeDAO;
  private final PatternDAO patternDAO;
  private final PatternMemeDAO patternMemeDAO;
  private final VoiceDAO voiceDAO;
  private final PatternEventDAO patternEventDAO;

  private final Access access;
  private final PatternChordDAO patternChordDAO;
  private final AudioChordDAO audioChordDAO;
  private final LibraryDAO libraryDAO;
  private final Collection<Entity> sourceEntities;
  private final Map<BigInteger, Sequence> sequenceMap = Maps.newConcurrentMap();
  private final Map<BigInteger, SequenceMeme> sequenceMemeMap = Maps.newConcurrentMap();
  private final Map<BigInteger, Pattern> patternMap = Maps.newConcurrentMap();
  private final Map<BigInteger, PatternMeme> patternMemeMap = Maps.newConcurrentMap();
  private final Map<BigInteger, Instrument> instrumentMap = Maps.newConcurrentMap();
  private final Map<BigInteger, InstrumentMeme> instrumentMemeMap = Maps.newConcurrentMap();
  private final Map<BigInteger, Library> libraryMap = Maps.newConcurrentMap();
  private final Map<BigInteger, Audio> audioMap = Maps.newConcurrentMap();
  private final Map<BigInteger, AudioChord> audioChordMap = Maps.newConcurrentMap();
  private final Map<BigInteger, AudioEvent> audioEventMap = Maps.newConcurrentMap();
  private final Map<BigInteger, PatternChord> patternChordMap = Maps.newConcurrentMap();
  private final Map<BigInteger, Voice> voiceMap = Maps.newConcurrentMap();
  private final Map<BigInteger, PatternEvent> patternEventMap = Maps.newConcurrentMap();
  private final Map<BigInteger, Collection<AudioEvent>> _audioWithFirstEvent = Maps.newConcurrentMap();
  private final Map<BigInteger, Map<BigInteger, Collection<Pattern>>> _sequencePatternsByOffset = Maps.newConcurrentMap();
  private final Map<BigInteger, Map<BigInteger, Map<PatternType, Pattern>>> _sequencePatternByOffsetAndType = Maps.newConcurrentMap();

  @Inject
  public IngestImpl(
    @Assisted("access") Access access,
    @Assisted("entities") Collection<Entity> sourceEntities,
    AudioChordDAO audioChordDAO,
    AudioDAO audioDAO,
    AudioEventDAO audioEventDAO,
    EntityCacheProvider entityCacheProvider,
    InstrumentDAO instrumentDAO,
    InstrumentMemeDAO instrumentMemeDAO,
    LibraryDAO libraryDAO,
    SequenceDAO sequenceDAO,
    SequenceMemeDAO sequenceMemeDAO,
    PatternChordDAO patternChordDAO,
    PatternDAO patternDAO,
    PatternMemeDAO patternMemeDAO,
    VoiceDAO voiceDAO,
    PatternEventDAO patternEventDAO
  ) {
    this.access = access;
    this.audioChordDAO = audioChordDAO;
    this.audioDAO = audioDAO;
    this.audioEventDAO = audioEventDAO;
    this.sourceEntities = Lists.newArrayList(sourceEntities);
    this.entityCacheProvider = entityCacheProvider;
    this.instrumentDAO = instrumentDAO;
    this.instrumentMemeDAO = instrumentMemeDAO;
    this.libraryDAO = libraryDAO;
    this.sequenceDAO = sequenceDAO;
    this.sequenceMemeDAO = sequenceMemeDAO;
    this.patternChordDAO = patternChordDAO;
    this.patternDAO = patternDAO;
    this.patternMemeDAO = patternMemeDAO;
    this.voiceDAO = voiceDAO;
    this.patternEventDAO = patternEventDAO;
    readAll(); // must happen before chord progressions are computed
  }

  /**
   Fetch all entities of a particular class with a particular parent id

   @param <N>       class of entity
   @param entityMap of all entities to source
   @param parentId  to match
   @return collection of entities matching parent id
   */
  private static <N extends Entity> Collection<N> fetchMany(Map<BigInteger, N> entityMap, BigInteger parentId) {
    ImmutableList.Builder<N> result = ImmutableList.builder();
    entityMap.values().forEach(entity -> {
      if (Objects.equals(parentId, entity.getParentId()))
        result.add(entity);
    });
    return result.build();
  }

  @Override
  public Map<BigInteger, Sequence> sequenceMap() {
    return Collections.unmodifiableMap(sequenceMap);
  }

  @Override
  public Map<BigInteger, Pattern> patternMap() {
    return Collections.unmodifiableMap(patternMap);
  }

  @Override
  public Map<BigInteger, Instrument> instrumentMap() {
    return Collections.unmodifiableMap(instrumentMap);
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
      readAll(PatternMeme.class, patternMemeMap, patternMap.keySet(), patternMemeDAO);
      readAll(Voice.class, voiceMap, sequenceMap.keySet(), voiceDAO);
    } catch (Exception e) {
      log.error("Failed to read all entities for ingest.", e);
    }
  }

  /**
   Read all Entities and put results into map
   NOTE: the order of these operations is important! it is managed by the main readAll() process
   */
  private <N extends Entity> void readAll(Class<N> entityClass, Map<BigInteger, N> entityMap, Collection<BigInteger> parentIds, DAO<N> dao) throws Exception {
    dao.readAll(access, parentIds).forEach(entity -> entityMap.put(entity.getId(), entity));

    for (BigInteger id : entityIds(entityMap, entityClass))
      if (!entityMap.containsKey(id))
        entityMap.put(id, dao.readOne(access, id));
  }

  /**
   Read all Entities and put results into map
   NOTE: the order of these operations is important! it is managed by the main readAll() process
   */
  private <N extends Entity> void readAll(Class<N> entityClass, Map<BigInteger, N> entityMap, DAO<N> dao) throws Exception {
    for (BigInteger id : entityIds(entityMap, entityClass))
      if (!entityMap.containsKey(id))
        entityMap.put(id, dao.readOne(access, id));
  }

  /**
   Get collection of Id, starting with a map of entity, and adding entity filtered by class from all source entities

   @param entityClass use only this class of source entities
   @return collection of id
   */
  private <N extends Entity> Collection<BigInteger> entityIds(Map<BigInteger, N> baseEntities, Class<N> entityClass) {
    Map<BigInteger, Boolean> result = Maps.newConcurrentMap();
    baseEntities.forEach((id, entity) -> result.put(id, true));
    sourceEntities.forEach(entity -> {
      if (entityClass.equals(entity.getClass())) {
        result.put(entity.getId(), true);
      }
    });
    return result.keySet();
  }

  @Override
  public Collection<Sequence> sequences() {
    return sequenceMap.values();
  }

  @Override
  public Collection<Sequence> sequences(SequenceType type) {
    ImmutableList.Builder<Sequence> result = ImmutableList.builder();
    sequenceMap.values().forEach(sequence -> {
      if (Objects.equals(type, sequence.getType()))
        result.add(sequence);
    });
    return result.build();
  }

  @Override
  public Access access() {
    return access;
  }

  @Override
  public Sequence sequence(BigInteger id) {
    return fetchOne(Sequence.class, sequenceMap, sequenceDAO, id);
  }

  @Override
  public Collection<SequenceMeme> sequenceMemes() {
    return sequenceMemeMap.values();
  }

  @Override
  public Collection<SequenceMeme> sequenceMemes(BigInteger sequenceId) {
    return fetchMany(sequenceMemeMap, sequenceId);
  }

  @Override
  public Collection<Meme> sequenceAndPatternMemes(BigInteger sequenceId, BigInteger sequencePatternOffset, PatternType... patternTypes) throws Exception {
    Map<String, Meme> baseMemes = Maps.newConcurrentMap();

    // add sequence memes
    sequenceMemes(sequenceId).forEach((sequenceMeme ->
      baseMemes.put(sequenceMeme.getName(), sequenceMeme)));

    // add sequence pattern memes
    for (PatternType patternType : patternTypes) {
      Pattern pattern = patternAtOffset(sequenceId, sequencePatternOffset, patternType);
      if (Objects.nonNull(pattern))
        patternMemes(pattern.getId()).forEach((sequenceMeme ->
          baseMemes.put(sequenceMeme.getName(), sequenceMeme)));
    }

    return baseMemes.values();
  }

  @Override
  public Collection<Pattern> patterns() {
    return patternMap.values();
  }

  @Override
  public Collection<Pattern> patterns(BigInteger sequenceId) {
    return fetchMany(patternMap, sequenceId);
  }

  @Override
  public Collection<PatternMeme> patternMemes() {
    return patternMemeMap.values();
  }

  @Override
  public Collection<PatternMeme> patternMemes(BigInteger patternId) {
    return fetchMany(patternMemeMap, patternId);
  }

  @Override
  @Nullable
  public Pattern patternAtOffset(BigInteger sequenceId, BigInteger sequencePatternOffset, PatternType patternType) throws Exception {
    if (!_sequencePatternByOffsetAndType.containsKey(sequenceId))
      _sequencePatternByOffsetAndType.put(sequenceId, Maps.newConcurrentMap());

    if (!_sequencePatternByOffsetAndType.get(sequenceId).containsKey(sequencePatternOffset))
      _sequencePatternByOffsetAndType.get(sequenceId).put(sequencePatternOffset, Maps.newConcurrentMap());

    if (!_sequencePatternByOffsetAndType.get(sequenceId).get(sequencePatternOffset).containsKey(patternType)) {
      Pattern pattern = patternRandomAtOffset(sequenceId, sequencePatternOffset, patternType);
      if (Objects.nonNull(pattern)) {
        _sequencePatternByOffsetAndType.get(sequenceId).get(sequencePatternOffset).put(patternType, pattern);
      }
    }

    return _sequencePatternByOffsetAndType.get(sequenceId).get(sequencePatternOffset).getOrDefault(patternType, null);
  }

  @Override
  public Pattern patternRandomAtOffset(BigInteger sequenceId, BigInteger sequencePatternOffset, PatternType patternType) throws Exception {
    EntityRank<Pattern> entityRank = new EntityRank<>();
    patternsAtOffset(sequenceId, sequencePatternOffset).forEach((pattern) -> {
      if (Objects.equals(pattern.getType(), patternType)) {
        entityRank.add(pattern, Chance.normallyAround(0.0, 1.0));
      }
    });
    return entityRank.getTop();
  }

  @Override
  public Collection<Pattern> patternsAtOffset(BigInteger sequenceId, BigInteger sequencePatternOffset) throws Exception {
    if (!_sequencePatternsByOffset.containsKey(sequenceId))
      _sequencePatternsByOffset.put(sequenceId, Maps.newConcurrentMap());

    if (!_sequencePatternsByOffset.get(sequenceId).containsKey(sequencePatternOffset))
      _sequencePatternsByOffset.get(sequenceId).put(sequencePatternOffset,
        patternDAO.readAllAtSequenceOffset(Access.internal(), sequenceId, sequencePatternOffset));

    return _sequencePatternsByOffset.get(sequenceId).get(sequencePatternOffset);
  }

  @Override
  public Collection<Instrument> instruments() {
    return instrumentMap.values();
  }

  @Override
  public Collection<Instrument> instruments(InstrumentType type) {
    ImmutableList.Builder<Instrument> result = ImmutableList.builder();
    instrumentMap.values().forEach(instrument -> {
      if (Objects.equals(type, instrument.getType()))
        result.add(instrument);
    });
    return result.build();
  }

  @Override
  public Instrument instrument(BigInteger id) {
    return fetchOne(Instrument.class, instrumentMap, instrumentDAO, id);
  }

  @Override
  public Collection<AudioEvent> instrumentAudioFirstEvents(BigInteger instrumentId) throws Exception {
    if (!_audioWithFirstEvent.containsKey(instrumentId))
      _audioWithFirstEvent.put(instrumentId, audioEventDAO.readAllFirstEventsForInstrument(Access.internal(), instrumentId));

    return _audioWithFirstEvent.get(instrumentId);
  }

  @Override
  public Collection<InstrumentMeme> instrumentMemes() {
    return instrumentMemeMap.values();
  }

  @Override
  public Collection<InstrumentMeme> instrumentMemes(BigInteger instrumentId) {
    return fetchMany(instrumentMemeMap, instrumentId);
  }

  @Override
  public Collection<Library> libraries() {
    return libraryMap.values();
  }

  @Override
  public Collection<Audio> audios() {
    return audioMap.values();
  }

  @Override
  public Collection<Audio> audios(BigInteger instrumentId) {
    return fetchMany(audioMap, instrumentId);
  }

  @Override
  public Audio audio(BigInteger id) {
    return fetchOne(Audio.class, audioMap, audioDAO, id);
  }

  @Override
  public Key patternKey(BigInteger id) {
    // if null pattern return empty key
    Pattern pattern = fetchOne(Pattern.class, patternMap, patternDAO, id);
    if (Objects.isNull(pattern))
      return Key.of("");

    // if pattern has key, use that
    if (Objects.nonNull(pattern.getKey()) && !pattern.getKey().isEmpty())
      return Key.of(pattern.getKey());

    // pattern has no key; use sequence key. if null sequence return empty key
    Sequence sequence = sequence(pattern.getSequenceId());
    if (Objects.isNull(sequence))
      return Key.of("");
    return Key.of(sequence.getKey());
  }

  @Override
  public Collection<AudioChord> audioChords() {
    return audioChordMap.values();
  }

  @Override
  public Collection<AudioEvent> audioEvents() {
    return audioEventMap.values();
  }

  @Override
  public Collection<PatternChord> patternChords() {
    return patternChordMap.values();
  }

  @Override
  public Collection<PatternChord> patternChords(BigInteger patternId) {
    return fetchMany(patternChordMap, patternId);
  }

  @Override
  public Collection<Voice> voices() {
    return voiceMap.values();
  }

  @Override
  public Collection<Voice> voices(BigInteger sequenceId) {
    return fetchMany(voiceMap, sequenceId);
  }

  @Override
  public Collection<PatternEvent> patternEvents() {
    return patternEventMap.values();
  }

  @Override
  public Collection<PatternEvent> patternVoiceEvents(BigInteger patternId, BigInteger voiceId) {
    ImmutableList.Builder<PatternEvent> result = ImmutableList.builder();
    patternEventMap.values().forEach(patternEvent -> {
      if (Objects.equals(patternId, patternEvent.getPatternId()) &&
        Objects.equals(voiceId, patternEvent.getVoiceId()))
        result.add(patternEvent);
    });
    return result.build();
  }

  @Override
  public Map<BigInteger, Audio> audioMap() {
    return Collections.unmodifiableMap(audioMap);
  }

  @Override
  public Collection<Entity> all() {
    ImmutableList.Builder<Entity> result = ImmutableList.builder();
    result.addAll(audioChords());
    result.addAll(audioEvents());
    result.addAll(audios());
    result.addAll(instrumentMemes());
    result.addAll(instruments());
    result.addAll(libraries());
    result.addAll(sequenceMemes());
    result.addAll(sequences());
    result.addAll(patternChords());
    result.addAll(patternEvents());
    result.addAll(patternMemes());
    result.addAll(patterns());
    result.addAll(voices());
    return result.build();
  }

  /**
   Fetch one entity, ideally from an entity map, but if necessary fetch from dao and cache that result in case we need it again.

   @param entityMap to fetch from as primary source
   @param entityDAO to readOne() from, as secondary source
   @param entityId  to fetch
   @param <N>       class of entity
   @return entity
   */
  @Nullable
  private <N extends Entity> N fetchOne(Class<N> entityClass, Map<BigInteger, N> entityMap, DAO<N> entityDAO, BigInteger entityId) {
    if (entityMap.containsKey(entityId))
      return entityMap.get(entityId);

    return entityCacheProvider.fetchOne(access, entityClass, entityDAO, entityId);
  }

  @Override
  public String toString() {
    return Text.entityHistogram(all());
  }

}
