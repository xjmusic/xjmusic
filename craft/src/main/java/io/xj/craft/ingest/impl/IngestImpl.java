// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.ingest.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import io.xj.core.access.impl.Access;
import io.xj.core.cache.entity.EntityCacheProvider;
import io.xj.core.dao.AudioChordDAO;
import io.xj.core.dao.AudioDAO;
import io.xj.core.dao.AudioEventDAO;
import io.xj.core.dao.DAO;
import io.xj.core.dao.InstrumentDAO;
import io.xj.core.dao.InstrumentMemeDAO;
import io.xj.core.dao.LibraryDAO;
import io.xj.core.dao.PatternDAO;
import io.xj.core.dao.PatternMemeDAO;
import io.xj.core.dao.PhaseChordDAO;
import io.xj.core.dao.PhaseDAO;
import io.xj.core.dao.PhaseEventDAO;
import io.xj.core.dao.PhaseMemeDAO;
import io.xj.core.dao.VoiceDAO;
import io.xj.craft.ingest.Ingest;
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
import io.xj.core.model.pattern_meme.PatternMeme;
import io.xj.core.model.phase.Phase;
import io.xj.core.model.phase.PhaseType;
import io.xj.core.model.phase_chord.PhaseChord;
import io.xj.core.model.phase_event.PhaseEvent;
import io.xj.core.model.phase_meme.PhaseMeme;
import io.xj.core.model.voice.Voice;
import io.xj.core.util.Chance;
import io.xj.music.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 [#154350346] Architect wants a universal Ingest Factory, to modularize graph mathematics used during craft to evaluate any combination of Library, Pattern, and Instrument for any purpose.
 */
public class IngestImpl implements Ingest {
  private final Logger log = LoggerFactory.getLogger(IngestImpl.class);
  private final AudioDAO audioDAO;
  private final AudioEventDAO audioEventDAO;
  private final PatternDAO patternDAO;
  private final EntityCacheProvider entityCacheProvider;
  private final InstrumentDAO instrumentDAO;
  private final PatternMemeDAO patternMemeDAO;
  private final InstrumentMemeDAO instrumentMemeDAO;
  private final PhaseDAO phaseDAO;
  private final PhaseMemeDAO phaseMemeDAO;
  private final VoiceDAO voiceDAO;
  private final PhaseEventDAO phaseEventDAO;

  private final Access access;
  private final PhaseChordDAO phaseChordDAO;
  private final AudioChordDAO audioChordDAO;
  private final LibraryDAO libraryDAO;
  private final Collection<Entity> sourceEntities;
  private final Map<BigInteger, Pattern> patternMap = Maps.newConcurrentMap();
  private final Map<BigInteger, PatternMeme> patternMemeMap = Maps.newConcurrentMap();
  private final Map<BigInteger, Phase> phaseMap = Maps.newConcurrentMap();
  private final Map<BigInteger, PhaseMeme> phaseMemeMap = Maps.newConcurrentMap();
  private final Map<BigInteger, Instrument> instrumentMap = Maps.newConcurrentMap();
  private final Map<BigInteger, InstrumentMeme> instrumentMemeMap = Maps.newConcurrentMap();
  private final Map<BigInteger, Library> libraryMap = Maps.newConcurrentMap();
  private final Map<BigInteger, Audio> audioMap = Maps.newConcurrentMap();
  private final Map<BigInteger, AudioChord> audioChordMap = Maps.newConcurrentMap();
  private final Map<BigInteger, AudioEvent> audioEventMap = Maps.newConcurrentMap();
  private final Map<BigInteger, PhaseChord> phaseChordMap = Maps.newConcurrentMap();
  private final Map<BigInteger, Voice> voiceMap = Maps.newConcurrentMap();
  private final Map<BigInteger, PhaseEvent> phaseEventMap = Maps.newConcurrentMap();
  private final Map<BigInteger, Collection<AudioEvent>> _audioWithFirstEvent = Maps.newConcurrentMap();
  private final Map<BigInteger, Map<BigInteger, Collection<Phase>>> _patternPhasesByOffset = Maps.newConcurrentMap();
  private final Map<BigInteger, Map<BigInteger, Map<PhaseType, Phase>>> _patternPhaseByOffsetAndType = Maps.newConcurrentMap();

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
    PatternDAO patternDAO,
    PatternMemeDAO patternMemeDAO,
    PhaseChordDAO phaseChordDAO,
    PhaseDAO phaseDAO,
    PhaseMemeDAO phaseMemeDAO,
    VoiceDAO voiceDAO,
    PhaseEventDAO phaseEventDAO
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
    this.patternDAO = patternDAO;
    this.patternMemeDAO = patternMemeDAO;
    this.phaseChordDAO = phaseChordDAO;
    this.phaseDAO = phaseDAO;
    this.phaseMemeDAO = phaseMemeDAO;
    this.voiceDAO = voiceDAO;
    this.phaseEventDAO = phaseEventDAO;
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
  public Map<BigInteger, Pattern> patternMap() {
    return Collections.unmodifiableMap(patternMap);
  }

  @Override
  public Map<BigInteger, Phase> phaseMap() {
    return Collections.unmodifiableMap(phaseMap);
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
      readAll(Pattern.class, patternMap, libraryMap.keySet(), patternDAO);
      readAll(PatternMeme.class, patternMemeMap, patternMap.keySet(), patternMemeDAO);
      readAll(Phase.class, phaseMap, patternMap.keySet(), phaseDAO);
      readAll(PhaseChord.class, phaseChordMap, phaseMap.keySet(), phaseChordDAO);
      readAll(PhaseEvent.class, phaseEventMap, phaseMap.keySet(), phaseEventDAO);
      readAll(PhaseMeme.class, phaseMemeMap, phaseMap.keySet(), phaseMemeDAO);
      readAll(Voice.class, voiceMap, patternMap.keySet(), voiceDAO);
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
  public Collection<Pattern> patterns() {
    return patternMap.values();
  }

  @Override
  public Collection<Pattern> patterns(PatternType type) {
    ImmutableList.Builder<Pattern> result = ImmutableList.builder();
    patternMap.values().forEach(pattern -> {
      if (Objects.equals(type, pattern.getType()))
        result.add(pattern);
    });
    return result.build();
  }

  @Override
  public Access access() {
    return access;
  }

  @Override
  public Pattern pattern(BigInteger id) {
    return fetchOne(Pattern.class, patternMap, patternDAO, id);
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
  public Collection<Meme> patternAndPhaseMemes(BigInteger patternId, BigInteger phaseOffset, PhaseType... phaseTypes) throws Exception {
    Map<String, Meme> baseMemes = Maps.newConcurrentMap();

    // add pattern memes
    patternMemes(patternId).forEach((patternMeme ->
      baseMemes.put(patternMeme.getName(), patternMeme)));

    // add pattern phase memes
    for (PhaseType phaseType : phaseTypes) {
      Phase phase = phaseAtOffset(patternId, phaseOffset, phaseType);
      if (Objects.nonNull(phase))
        phaseMemes(phase.getId()).forEach((patternMeme ->
          baseMemes.put(patternMeme.getName(), patternMeme)));
    }

    return baseMemes.values();
  }

  @Override
  public Collection<Phase> phases() {
    return phaseMap.values();
  }

  @Override
  public Collection<Phase> phases(BigInteger patternId) {
    return fetchMany(phaseMap, patternId);
  }

  @Override
  public Collection<PhaseMeme> phaseMemes() {
    return phaseMemeMap.values();
  }

  @Override
  public Collection<PhaseMeme> phaseMemes(BigInteger phaseId) {
    return fetchMany(phaseMemeMap, phaseId);
  }

  @Override
  @Nullable
  public Phase phaseAtOffset(BigInteger patternId, BigInteger phaseOffset, PhaseType phaseType) throws Exception {
    if (!_patternPhaseByOffsetAndType.containsKey(patternId))
      _patternPhaseByOffsetAndType.put(patternId, Maps.newConcurrentMap());

    if (!_patternPhaseByOffsetAndType.get(patternId).containsKey(phaseOffset))
      _patternPhaseByOffsetAndType.get(patternId).put(phaseOffset, Maps.newConcurrentMap());

    if (!_patternPhaseByOffsetAndType.get(patternId).get(phaseOffset).containsKey(phaseType)) {
      Phase phase = phaseRandomAtOffset(patternId, phaseOffset, phaseType);
      if (Objects.nonNull(phase)) {
        _patternPhaseByOffsetAndType.get(patternId).get(phaseOffset).put(phaseType, phase);
      }
    }

    return _patternPhaseByOffsetAndType.get(patternId).get(phaseOffset).getOrDefault(phaseType, null);
  }

  @Override
  public Phase phaseRandomAtOffset(BigInteger patternId, BigInteger phaseOffset, PhaseType phaseType) throws Exception {
    EntityRank<Phase> entityRank = new EntityRank<>();
    phasesAtOffset(patternId, phaseOffset).forEach((phase) -> {
      if (Objects.equals(phase.getType(), phaseType)) {
        entityRank.add(phase, Chance.normallyAround(0.0, 1.0));
      }
    });
    return entityRank.getTop();
  }

  @Override
  public Collection<Phase> phasesAtOffset(BigInteger patternId, BigInteger phaseOffset) throws Exception {
    if (!_patternPhasesByOffset.containsKey(patternId))
      _patternPhasesByOffset.put(patternId, Maps.newConcurrentMap());

    if (!_patternPhasesByOffset.get(patternId).containsKey(phaseOffset))
      _patternPhasesByOffset.get(patternId).put(phaseOffset,
        phaseDAO.readAllAtPatternOffset(Access.internal(), patternId, phaseOffset));

    return _patternPhasesByOffset.get(patternId).get(phaseOffset);
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
  public Key phaseKey(BigInteger id) {
    // if null phase return empty key
    Phase phase = fetchOne(Phase.class, phaseMap, phaseDAO, id);
    if (Objects.isNull(phase))
      return Key.of("");

    // if phase has key, use that
    if (Objects.nonNull(phase.getKey()) && !phase.getKey().isEmpty())
      return Key.of(phase.getKey());

    // phase has no key; use pattern key. if null pattern return empty key
    Pattern pattern = pattern(phase.getPatternId());
    if (Objects.isNull(pattern))
      return Key.of("");
    return Key.of(pattern.getKey());
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
  public Collection<PhaseChord> phaseChords() {
    return phaseChordMap.values();
  }

  @Override
  public Collection<PhaseChord> phaseChords(BigInteger phaseId) {
    return fetchMany(phaseChordMap, phaseId);
  }

  @Override
  public Collection<Voice> voices() {
    return voiceMap.values();
  }

  @Override
  public Collection<Voice> voices(BigInteger patternId) {
    return fetchMany(voiceMap, patternId);
  }

  @Override
  public Collection<PhaseEvent> phaseEvents() {
    return phaseEventMap.values();
  }

  @Override
  public Collection<PhaseEvent> phaseVoiceEvents(BigInteger phaseId, BigInteger voiceId) {
    ImmutableList.Builder<PhaseEvent> result = ImmutableList.builder();
    phaseEventMap.values().forEach(phaseEvent -> {
      if (Objects.equals(phaseId, phaseEvent.getPhaseId()) &&
        Objects.equals(voiceId, phaseEvent.getVoiceId()))
        result.add(phaseEvent);
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
    result.addAll(patternMemes());
    result.addAll(patterns());
    result.addAll(phaseChords());
    result.addAll(phaseEvents());
    result.addAll(phaseMemes());
    result.addAll(phases());
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


}
