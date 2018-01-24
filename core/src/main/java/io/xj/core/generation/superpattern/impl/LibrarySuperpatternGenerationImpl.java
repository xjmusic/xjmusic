// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.generation.superpattern.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import io.xj.core.cache.digest.DigestCacheProvider;
import io.xj.core.config.Config;
import io.xj.core.dao.PhaseChordDAO;
import io.xj.core.dao.PhaseDAO;
import io.xj.core.digest.chord_markov.DigestChordMarkov;
import io.xj.core.evaluation.Evaluation;
import io.xj.core.generation.GenerationType;
import io.xj.core.generation.impl.GenerationImpl;
import io.xj.core.generation.superpattern.LibrarySuperpatternGeneration;
import io.xj.core.model.chord.ChordMarkovNode;
import io.xj.core.model.chord.ChordNode;
import io.xj.core.model.chord.ChordProgression;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.phase.Phase;
import io.xj.core.model.phase.PhaseType;
import io.xj.core.model.phase_chord.PhaseChord;
import io.xj.core.model.phase_chord.PhaseChordProgression;
import io.xj.core.util.TremendouslyRandom;
import io.xj.music.Key;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 [#154548999] Artist wants to generate a Library Superpattern in order to create a Detail pattern that covers the chord progressions of all existing Main Patterns in a Library.
 */
public class LibrarySuperpatternGenerationImpl extends GenerationImpl implements LibrarySuperpatternGeneration {
  private static final String GENERATED_PHASE_NAME = "Library Superphase";
  private final Logger log = LoggerFactory.getLogger(LibrarySuperpatternGenerationImpl.class);
  private final Pattern pattern;
  private final Integer markovOrder = Config.chordMarkovOrder();
  private final Integer phasesToGenerate = Config.generationSuperpatternPhaseCount();
  private final PhaseDAO phaseDAO;
  private final PhaseChordDAO phaseChordDAO;
  private DigestChordMarkov digestChordMarkov;
  private final List<Phase> generatedPhases = Lists.newArrayList();
  private final Map<BigInteger, List<PhaseChord>> generatedPhaseChords = Maps.newConcurrentMap();

  /**
   Instantiate a new digest with a collection of target entities

   @param pattern    to build superpattern around
   @param evaluation to digest
   */
  @Inject
  public LibrarySuperpatternGenerationImpl(
    @Assisted("pattern") Pattern pattern,
    @Assisted("evaluation") Evaluation evaluation,
    PhaseDAO phaseDAO,
    PhaseChordDAO phaseChordDAO,
    DigestCacheProvider digestCacheProvider
  ) {
    super(evaluation, GenerationType.LibrarySuperpattern);
    this.pattern = pattern;
    this.phaseDAO = phaseDAO;
    this.phaseChordDAO = phaseChordDAO;
    try {
      digestChordMarkov = digestCacheProvider.chordMarkov(evaluation);
      for (ChordProgression chordProgression : generateChordProgressions())
        createPhaseAndChords(chordProgression);

    } catch (Exception e) {
      log.error("Failed to generate superpattern of evaluation {}", evaluation, e);
    }
  }

  @Override
  public List<Phase> getGeneratedPhases() {
    return Collections.unmodifiableList(generatedPhases);
  }

  @Override
  public Map<BigInteger, List<PhaseChord>> getGeneratedPhaseChords() {
    return Collections.unmodifiableMap(generatedPhaseChords);
  }

  /**
   Create a phase and all phase chords for a given chord progression

   @param chordProgression to create phase of
   */
  private void createPhaseAndChords(ChordProgression chordProgression) throws Exception {
    if (chordProgression.isEmpty()) {
      log.warn("Cannot create a phase out of an empty chord progression!");
      return;
    }

    Phase phase = phaseDAO.create(evaluation.access(),
      new Phase()
        .setPatternId(pattern.getId())
        .setOffset(BigInteger.ZERO)
        .setName(GENERATED_PHASE_NAME)
        .setTypeEnum(PhaseType.Loop)
        .setTotal(1));

    // Create phase chords via DAO, for all chords in the chord progression
    PhaseChordProgression phaseChordProgression = new PhaseChordProgression(chordProgression, phase.getId(), Key.of(pattern.getKey()).getRootPitchClass());
    phaseChordProgression.getChords().forEach(phaseChord -> {
      try {
        cache(phaseChordDAO.create(evaluation.access(), phaseChord));
      } catch (Exception e) {
        log.error("Failure while generating phase chord for library superpattern", e); // future: better error transport, somehow show this to the Hub UI user perhaps
      }
    });

    phase.setTotal(phaseChordProgression.estimatePhaseTotal());
    phaseDAO.update(evaluation.access(),
      phase.getId(), phase);
    generatedPhases.add(phase);
  }

  /**
   Cache a generated phase chord.
   generatedPhaseChords is keyed by phase id.
   Each phase id contains a list of phase chords.

   @param phaseChord to cache
   */
  private void cache(PhaseChord phaseChord) {
    if (!generatedPhaseChords.containsKey(phaseChord.getPhaseId()))
      generatedPhaseChords.put(phaseChord.getPhaseId(), Lists.newArrayList());

    generatedPhaseChords.get(phaseChord.getPhaseId()).add(phaseChord);
  }

  /**
   Generate superpattern chord progression, from the chord Markov digest.
   - has a method that performs a random walk based on a DigestChordMarkov
   - begin each phase on any chord that appeared at the beginning of an observed sequence.
   - continues its random walk until all chord forms in the library have appeared at least N times.
   - may end a phase if that outcome is selected from the preceding state descriptor-- meaning that it was observed that a sequence ended after that chord, and therefore this phase will now end.
   - transposes all of its observations into the Key of the superpattern it's generating-- meaning that all chord progressions in a song (the ones that matter are at the beginning at end of sequences) are interpreted relative to the key of the pattern they are observed in.
   - Generate phases (1 chord progress = 1 phase) until done

   @return chord progression
   */
  private List<ChordProgression> generateChordProgressions() throws Exception {
    List<ChordProgression> result = Lists.newArrayList();

    int count = 0; // TODO deprecate this counting in favor of analyzing done-ness.

    Boolean continueGenerating = true;
    while (continueGenerating) {
      result.add(generateChordProgression());

      count++;
      if (count >= phasesToGenerate)
        continueGenerating = false; // TODO analyze done-ness and decide whether to continue generating
    }

    return result;
  }

  /**
   Generate a chord progression (phase)

   @return chord progression
   */
  private ChordProgression generateChordProgression() {
    // note the RESULT is different from the BUFFER (only caches previous N chords)
    List<ChordNode> result = Lists.newArrayList();
    List<ChordNode> buf = Lists.newArrayList();

    // "beginning of phase" marker (null bookend)
    buf.add(new ChordNode());

    // Generate chord nodes until phase is done
    Boolean continuePhase = true;
    while (continuePhase) {
      // Search Markov nodes for precedent state buffer contents, from 1 to N orders of depth, and add all possibilities
      List<ChordNode> observations = recursiveObservations(buf);
      ChordNode next = observations.isEmpty() ? new ChordNode() : observations.get(TremendouslyRandom.zeroToLimit(observations.size()));

      // did we just choose the end of a progression? if so, add the just-added chord to the buffer; if buffer is longer than the markov order, shift items from the top until it's the right size.
      if (next.isChord()) {
        buf.add(next);
        result.add(next);
        if (buf.size() > markovOrder) buf.remove(0);

      } else continuePhase = false;
    }

    return new ChordProgression(result);
  }

  /**
   Search Markov nodes for precedent state buffer contents, from 1 to N orders of depth, and add all possibilities
   add observations N times, such that higher-order observations are N times more likely to be chosen

   @param buffer of precedent state
   @return recursive observations
   */
  private List<ChordNode> recursiveObservations(List<ChordNode> buffer) {
    List<ChordNode> result = Lists.newArrayList();
    for (int n = 0; n < buffer.size(); n++) {
      String key = new ChordMarkovNode(buffer.subList(n, buffer.size())).precedentStateDescriptor();
      if (digestChordMarkov.getChordMarkovNodeMap().containsKey(key))
        for (int r = 0; r <= n; r++) // add observations N times
          result.addAll(digestChordMarkov.getChordMarkovNodeMap().get(key).getNodeMap());
    }
    return result;
  }


  @Override
  public Pattern getPattern() {
    return pattern;
  }

  @Override
  public GenerationType type() {
    return type;
  }

  @Override
  public Evaluation evaluation() throws Exception {
    return evaluation;
  }

  /**
   Yes, the order of elements in JSON arrays is preserved,
   per RFC 7159 JavaScript Object Notation (JSON) Data Interchange Format (emphasis mine).

   @return json object containing ORDERED ARRAY of evaluated chord progressions
   */
  @Override
  public JSONObject toJSONObject() {
    JSONObject spObj = new JSONObject();
/*
   FUTURE: to output JSON object, put all phases
    spObj.put(KEY_PHASE_ID, phase.getId().toString());
    spObj.put(KEY_PHASE_NAME, phase.getName());
    spObj.put(KEY_PHASE_TYPE, phase.getType());
*/
    spObj.put(KEY_PATTERN_ID, pattern.getId().toString());
    spObj.put(KEY_PATTERN_NAME, pattern.getName());
    spObj.put(KEY_PATTERN_TYPE, pattern.getType());
    JSONArray chordArr = new JSONArray();
/*
   FUTURE: to output JSON object, put all chords
    phaseChordProgression.getChords().forEach(chord -> chordArr.put(toJSONObject(chord)));
*/
    spObj.put(KEY_CHORD_SEQUENCE, chordArr);

    JSONObject result = new JSONObject();
    result.put(KEY_SUPERPATTERN, spObj);
    return result;
  }

}
