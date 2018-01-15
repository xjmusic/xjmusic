// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.evaluation.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.config.Config;
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
import io.xj.core.evaluation.Evaluation;
import io.xj.core.model.Entity;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.audio_chord.AudioChord;
import io.xj.core.model.audio_event.AudioEvent;
import io.xj.core.model.chord.Chord;
import io.xj.core.model.chord.ChordSequence;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument_meme.InstrumentMeme;
import io.xj.core.model.library.Library;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern_meme.PatternMeme;
import io.xj.core.model.phase.Phase;
import io.xj.core.model.phase_chord.PhaseChord;
import io.xj.core.model.phase_event.PhaseEvent;
import io.xj.core.model.phase_meme.PhaseMeme;
import io.xj.core.model.voice.Voice;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 [#214] If a Chain has Patterns associated with it directly, prefer those choices to any in the Library
 */
public class EvaluationImpl implements Evaluation {
  public static final String KEY_ONE = "evaluation";
  public static final String KEY_MANY = "evaluations";
  private final Logger log = LoggerFactory.getLogger(EvaluationImpl.class);
  private final AudioDAO audioDAO;
  private final AudioEventDAO audioEventDAO;
  private final PatternDAO patternDAO;
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

  @Inject
  public EvaluationImpl(
    @Assisted("access") Access access,
    @Assisted("entities") Collection<Entity> sourceEntities,
    AudioChordDAO audioChordDAO,
    AudioDAO audioDAO,
    AudioEventDAO audioEventDAO,
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
    readAll(); // must happen before chord sequences are computed
  }

  /**
   Compute all possible chord sequences given a set of chords (e.g. from a Phase or Audio)

   @param parentId parent of chords
   @param chords   to compute all possible sequences of
   @return array of phaseMap
   */
  private static Collection<ChordSequence> computeChordSequences(BigInteger parentId, Collection<Chord> chords) {
    List<ChordSequence> result = Lists.newArrayList();

    List<Chord> allChords = Lists.newArrayList(chords);
    allChords.sort(Chord.byPositionAscending);

    int totalChords = allChords.size();
    for (int fromChord = 0; fromChord < totalChords; fromChord++) {
      int maxToChord = Math.min(totalChords, fromChord + Config.evaluationChordSequenceLengthMax());
      for (int toChord = fromChord; toChord < maxToChord; toChord++) {
        List<Chord> subset = Lists.newArrayList();
        for (int i = fromChord; i <= toChord; i++) {
          subset.add(allChords.get(i));
        }
        result.add(new ChordSequence(parentId, subset));
      }
    }

    return result;
  }

  /**
   Get only the audio chords of a particular audio

   @param chordMap to search for chords
   @return collection of audio chords
   */
  private static <C extends Chord> Collection<Chord> chordsOf(Map<BigInteger, C> chordMap, BigInteger parentId) {
    Collection<Chord> result = Lists.newArrayList();
    chordMap.values().forEach(chord -> {
      if (Objects.equal(parentId, chord.getParentId())) result.add(chord);
    });
    return result;
  }

  @Override
  public Map<BigInteger, Pattern> getPatternMap() {
    return Collections.unmodifiableMap(patternMap);
  }

  @Override
  public Map<BigInteger, Phase> getPhaseMap() {
    return Collections.unmodifiableMap(phaseMap);
  }

  @Override
  public Map<BigInteger, Instrument> getInstrumentMap() {
    return Collections.unmodifiableMap(instrumentMap);
  }

  /**
   Compute all possible chord sequences for contents in evaluation
   */
  private Collection<ChordSequence> computeAllChordSequences() {
    Collection<ChordSequence> result = Lists.newArrayList();

    phaseMap.keySet().forEach(phaseId ->
      result.addAll(computeChordSequences(phaseId, chordsOf(phaseChordMap, phaseId))));
    audioMap.keySet().forEach(audioId ->
      result.addAll(computeChordSequences(audioId, chordsOf(audioChordMap, audioId))));

    return result;
  }

  /**
   Read all records via DAO
   for all entities and children
   NOTE: the order of operations inside here is important! hierarchical order, from parents to children
   */
  private void readAll() {
    try {
      readAll(Library.class, libraryMap, Lists.newArrayList(), libraryDAO);
      readAll(Instrument.class, instrumentMap, libraryMap.keySet(), instrumentDAO);
      readAll(InstrumentMeme.class, instrumentMemeMap, instrumentMap.keySet(), instrumentMemeDAO);
      readAll(Audio.class, audioMap, instrumentMap.keySet(), audioDAO);
      readAll(AudioChord.class, audioChordMap, audioMap.keySet(), audioChordDAO);
      readAll(AudioEvent.class, audioEventMap, audioMap.keySet(), audioEventDAO);
      readAll(Pattern.class, patternMap, libraryMap.keySet(), patternDAO);
      readAll(PatternMeme.class, patternMemeMap, patternMap.keySet(), patternMemeDAO);
      readAll(Phase.class, phaseMap, patternMap.keySet(), phaseDAO);
      readAll(PhaseChord.class, phaseChordMap, phaseMap.keySet(), phaseChordDAO);
      readAll(PhaseMeme.class, phaseMemeMap, phaseMemeMap.keySet(), phaseMemeDAO);
      readAll(Voice.class, voiceMap, patternMap.keySet(), voiceDAO);
      readAll(PhaseEvent.class, phaseEventMap, phaseEventMap.keySet(), phaseEventDAO);
    } catch (Exception e) {
      log.error("Failed to read all entities for evaluation.", e);
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
  public Collection<Pattern> getPatterns() {
    return patternMap.values();
  }

  @Override

  public Collection<PatternMeme> getPatternMemes() {
    return patternMemeMap.values();
  }

  @Override
  public Collection<Phase> getPhases() {
    return phaseMap.values();
  }

  @Override
  public Collection<PhaseMeme> getPhaseMemes() {
    return phaseMemeMap.values();
  }

  @Override
  public Collection<Instrument> getInstruments() {
    return instrumentMap.values();
  }

  @Override
  public Collection<InstrumentMeme> getInstrumentMemes() {
    return instrumentMemeMap.values();
  }

  @Override
  public Collection<Library> getLibraries() {
    return libraryMap.values();
  }

  @Override
  public Collection<Audio> getAudios() {
    return audioMap.values();
  }

  @Override
  public Collection<AudioChord> getAudioChords() {
    return audioChordMap.values();
  }

  @Override
  public Collection<AudioEvent> getAudioEvents() {
    return audioEventMap.values();
  }

  @Override
  public Collection<PhaseChord> getPhaseChords() {
    return phaseChordMap.values();
  }

  @Override
  public Collection<Voice> getVoices() {
    return voiceMap.values();
  }

  @Override
  public Collection<PhaseEvent> getPhaseEvents() {
    return phaseEventMap.values();
  }

  @Override
  public Collection<ChordSequence> getChordSequences() {
    return computeAllChordSequences();
  }

  @Override
  public Map<BigInteger, Audio> getAudioMap() {
    return Collections.unmodifiableMap(audioMap);
  }
}
