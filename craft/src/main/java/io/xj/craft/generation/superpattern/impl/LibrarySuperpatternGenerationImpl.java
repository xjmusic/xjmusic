// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.generation.superpattern.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import io.xj.core.config.Config;
import io.xj.core.dao.PhaseChordDAO;
import io.xj.core.dao.PhaseDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.model.chord.ChordMarkovNode;
import io.xj.core.model.chord.ChordNode;
import io.xj.core.model.chord.ChordProgression;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.phase.Phase;
import io.xj.core.model.phase.PhaseType;
import io.xj.core.model.phase_chord.PhaseChord;
import io.xj.core.model.phase_chord.PhaseChordProgression;
import io.xj.core.util.TremendouslyRandom;
import io.xj.craft.digest.cache.DigestCacheProvider;
import io.xj.craft.digest.chord_markov.DigestChordMarkov;
import io.xj.craft.digest.pattern_style.DigestPatternStyle;
import io.xj.craft.generation.GenerationType;
import io.xj.craft.generation.impl.GenerationImpl;
import io.xj.craft.generation.superpattern.LibrarySuperpatternGeneration;
import io.xj.craft.ingest.Ingest;
import io.xj.music.Key;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 [#154548999] Artist wants to generate a Library Superpattern in order to create a Detail pattern that covers the chord progressions of all existing Main Patterns in a Library.
 <p>
 [#154927374] Architect wants Chord Markov digest to compute both forward nodes (likelihood of following node based on observation of preceding nodes) and reverse nodes (likelihood of preceding node based on observation of following nodes in reverse time), in order to implement forward-reverse random walk during generation, in order to generate a superpattern with excellent coverage of the library contents.
 <p>
 [#154882199] Artist expects a generated superpattern to have near-complete coverage of all chord nodes in the library.
 <p>
 [#154855269] Artist expects generation of library superpattern to result in a set of phases of similar size.
 */
public class LibrarySuperpatternGenerationImpl extends GenerationImpl implements LibrarySuperpatternGeneration {
  private static final String GENERATED_PHASE_NAME = "Library Superphase";
  private final Logger log = LoggerFactory.getLogger(LibrarySuperpatternGenerationImpl.class);
  private final Pattern pattern;
  private final Integer markovOrder = Config.chordMarkovOrder();
  private final PhaseDAO phaseDAO;
  private final PhaseChordDAO phaseChordDAO;
  private DigestChordMarkov digestChordMarkov;
  private DigestPatternStyle digestPatternStyle;
  private final List<Phase> generatedPhases = Lists.newArrayList();
  private final Map<BigInteger, List<PhaseChord>> generatedPhaseChords = Maps.newConcurrentMap();
  private final Map<String, ChordNode> coveredNodeMap = Maps.newConcurrentMap();
  private final Integer maxGenerationAttempts = Config.generationCollisionAttemptsMax();
  private final Integer patternPhasesMultiplier = Config.generationPatternPhasesMultiplier();

  /**
   Instantiate a new digest with a collection of target entities

   @param pattern to build superpattern around
   @param ingest  to digest
   */
  @Inject
  public LibrarySuperpatternGenerationImpl(
    @Assisted("pattern") Pattern pattern,
    @Assisted("ingest") Ingest ingest,
    PhaseDAO phaseDAO,
    PhaseChordDAO phaseChordDAO,
    DigestCacheProvider digestCacheProvider
  ) {
    super(ingest, GenerationType.LibrarySuperpattern);
    this.pattern = pattern;
    this.phaseDAO = phaseDAO;
    this.phaseChordDAO = phaseChordDAO;
    try {
      digestPatternStyle = digestCacheProvider.patternStyle(ingest);
      digestChordMarkov = digestCacheProvider.chordMarkov(ingest);
      for (ChordProgression chordProgression : generateChordProgressions())
        createPhaseAndChords(chordProgression);

    } catch (Exception e) {
      log.error("Failed to generate superpattern of ingest {}", ingest, e);
    }
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

    Phase phase = phaseDAO.create(ingest.access(),
      new Phase()
        .setPatternId(pattern.getId())
        .setOffset(BigInteger.ZERO)
        .setName(GENERATED_PHASE_NAME)
        .setTypeEnum(PhaseType.Loop)
        .setTotal(chordProgression.getTotal()));

    // Create phase chords via DAO, for all chords in the chord progression
    PhaseChordProgression phaseChordProgression = new PhaseChordProgression(chordProgression, phase.getId(), Key.of(pattern.getKey()).getRootPitchClass(), chordProgression.getSpacing());
    phaseChordProgression.getChords().forEach(phaseChord -> {
      try {
        cache(phaseChordDAO.create(ingest.access(), phaseChord));
      } catch (Exception e) {
        log.error("Failure while generating phase chord for library superpattern", e); // future: better error transport, somehow show this to the Hub UI user perhaps
      }
    });

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

    double spacing = selectMostPopular(digestPatternStyle.getMainChordSpacingHistogram());
    int limit = selectByLottery(digestPatternStyle.getMainPhasesPerPatternHistogram()) * patternPhasesMultiplier;
    for (int n = 0; n < limit; n++) {
      int total = selectByLottery(digestPatternStyle.getMainPhaseTotalHistogram());
      result.add(generateChordProgression(total, spacing));
    }

    return result;
  }

  /**
   Generate a chord progression, by collision or random splice of forward-reverse random walks

   @param spacing for resulting chord progressions
   @param total   # beats in between chords, for resulting chord progressions
   @return chord progression
   */
  private ChordProgression generateChordProgression(Integer total, Double spacing) throws BusinessException {
    int attempt = 0;
    while (true) {
      attempt++;

      try {
        ChordProgression chordProgression = spliceAtCollision(
          generateChordProgression(digestChordMarkov.getForwardNodeMap(), spacing, total),
          generateChordProgression(digestChordMarkov.getReverseNodeMap(), spacing, total).reversed()
        );
        log.info("Did generate colliding forward and reverse chord progressions in {} attempts.", attempt);
        return chordProgression;

      } catch (Exception e) {
        if (attempt < maxGenerationAttempts) {
          log.debug("Generation {} of max {} resulted in non-colliding forward and reverse chord progressions; will retry.", attempt, maxGenerationAttempts, e);

        } else {
          ChordProgression chordProgression = spliceRandom(
            generateChordProgression(digestChordMarkov.getForwardNodeMap(), spacing, total),
            generateChordProgression(digestChordMarkov.getReverseNodeMap(), spacing, total).reversed()
          );
          log.warn("Failed to generate colliding forward and reverse chord progressions after {} attempts; did splice at random.", attempt, maxGenerationAttempts);
          return chordProgression;
        }
      }
    }
  }

  /**
   FUTURE: this smells nutty. Let's find a better algorithm for generating these things in the first place!!!
   Search for a point at which forward-reverse collide, between a forward-generated chord progression and a reverse-generated chord progression into one final progression, which begins in the forward-generated chords and ends with the reverse-generated chords.
   <p>
   Forward & Reverse chord progressions MUST have the same spacing, total, and number of chords! The output progression will have the same number of chords as the input progressions.
   - initial pass with cursor to discover if there is a point where forward-reverse collide.
   - if there is a point where forward-reverse collide, stitch forward-reverse over that point. (create sub method)
   - if there is no point where forward-reverse collide, throw an exception

   @param opening chord progression to consolidate
   @param closing chord progression to consolidate
   @return final progression, which begins in the forward-generated chords and ends with the reverse-generated chords.
   */
  private ChordProgression spliceAtCollision(ChordProgression opening, ChordProgression closing) throws BusinessException {
    if (!opening.hasSameTotalSpacingChords(closing))
      throw new BusinessException("Forward & Reverse chord progressions MUST have the same spacing, total, and number of chords!");

    for (int n = 0; n < opening.size(); n++)
      if (opening.getChordNodes().get(n).isEquivalentTo(closing.getChordNodes().get(n)))
        return splice(opening, closing, n);

    throw new BusinessException("Forward & Reverse chord progressions had no common point.");
  }

  /**
   Splice a forward and reverse chord progression at a specified index

   @param opening     chord progression
   @param closing     chord progression
   @param spliceIndex at which to splice
   @return spliced final chord progression
   */
  private ChordProgression splice(ChordProgression opening, ChordProgression closing, int spliceIndex) {
    List<ChordNode> chordNodes = Lists.newArrayList();
    for (int n = 0; n < opening.size(); n++)
      if (n < spliceIndex)
        chordNodes.add(opening.getChordNodes().get(n));
      else
        chordNodes.add(closing.getChordNodes().get(n));

    ChordProgression result = new ChordProgression(chordNodes);
    result.setSpacing(opening.getSpacing());
    result.setTotal(opening.getTotal());
    return result;
  }

  /**
   Force splice a forward-generated chord progression and a reverse-generated chord progression into one final progression, which begins in the forward-generated chords and ends with the reverse-generated chords.
   <p>
   Forward & Reverse chord progressions MUST have the same spacing, total, and number of chords! The output progression will have the same number of chords as the input progressions.

   @param opening chord progression to force splice
   @param closing chord progression to force splice
   @return final progression, which begins in the forward-generated chords and ends with the reverse-generated chords.
   */
  private ChordProgression spliceRandom(ChordProgression opening, ChordProgression closing) throws BusinessException {
    if (!opening.hasSameTotalSpacingChords(closing))
      throw new BusinessException("Forward & Reverse chord progressions MUST have the same spacing, total, and number of chords!");

    Integer min = opening.size() / 3;
    Integer max = 2 * opening.size() / 3;
    return splice(opening, closing, min + TremendouslyRandom.zeroToLimit(max - min));
  }

  /**
   Generate a chord progression (phase).
   Select a # of beats total for the progression, by random lottery from all phase totals in ingested main patterns.
   Select the most popular # of beats of inter-chord spacing and use that for everything.

   @param nodeMap to check precedent states for markov observations
   @param spacing for resulting chord progressions
   @param total   # beats in between chords, for resulting chord progressions
   @return chord progression
   */
  private ChordProgression generateChordProgression(Map<String, ChordMarkovNode> nodeMap, Double spacing, Integer total) {
    // note the RESULT is different from the BUFFER (only caches previous N chords)
    List<ChordNode> result = Lists.newArrayList();
    List<ChordNode> buf = Lists.newArrayList();

    // "beginning of phase" marker (null bookend)
    buf.add(new ChordNode());

    // Search Markov nodes for precedent state buffer contents, from 1 to N orders of depth, and add all possibilities
    for (int n = 0; n < total / spacing; n++) {
      ChordNode next = selectRandomNonEndObservation(nodeMap, buf);
      buf.add(next);
      result.add(next);
      if (buf.size() > markovOrder) buf.remove(0);
    }

    // mark these chords as used
    result.forEach(chordNode -> coveredNodeMap.put(chordNode.toString(), chordNode));

    // resulting chord progression includes phase total and chord spacing
    ChordProgression chordProgression = new ChordProgression(result);
    chordProgression.setTotal(total);
    chordProgression.setSpacing(spacing);
    return chordProgression;
  }

  /**
   Get the most popular entry in a histogram

   @param histogram to get most popular entry of
   @return most popular entry
   */
  private <N extends Number> N selectMostPopular(Multiset<N> histogram) {
    N result = null;
    Integer popularity = null;
    for (Multiset.Entry<N> entry : histogram.entrySet()) {
      if (Objects.isNull(popularity) || entry.getCount() > popularity) {
        popularity = entry.getCount();
        result = entry.getElement();
      }
    }
    return result;
  }

  /**
   Selects an integer by random from a histogram; each entry in the histogram is added to the lottery N times, where N is the number of occurrences of the entry in the histogram, such that an entry occurring more often in the histogram is more likely to be selected in the lottery.

   @return randomly selected integer
   */
  private Integer selectByLottery(Multiset<Integer> histogram) {
    List<Integer> lottery = Lists.newArrayList();
    lottery.addAll(histogram);
    return lottery.get(TremendouslyRandom.zeroToLimit(lottery.size()));
  }

  /**
   Search Markov nodes for precedent state buffer contents, from 1 to N orders of depth, and add all possibilities
   add observations N times, such that higher-order observations are N times more likely to be chosen.
   <p>
   Ensures that the walk does NOT arrive at an ending.
   (the forward-reverse walk algorithm deprecates the need for an ending in either direction)
   <p>
   Already-been-used chord nodes (stored in coveredNodeMap) are bumped to a secondary list.
   <p>
   Then use high-quality randomness to select one of the possibilities. Only select a second tier if no primary possibilities were available.

   @param buffer of precedent state
   @return recursive observations
   */
  private ChordNode selectRandomNonEndObservation(Map<String, ChordMarkovNode> nodeMap, List<ChordNode> buffer) {
    List<ChordNode> firstChoices = Lists.newArrayList();
    List<ChordNode> secondChoices = Lists.newArrayList();
    for (int n = 0; n < buffer.size(); n++) {
      String key = new ChordMarkovNode(buffer.subList(n, buffer.size())).precedentStateDescriptor();
      if (nodeMap.containsKey(key))
        for (int r = 0; r <= n; r++) // add observations N times
          nodeMap.get(key).getNodes().forEach(chordNode -> {
            if (isNonEnding(nodeMap, chordNode))
              if (coveredNodeMap.containsKey(chordNode.toString()))
                secondChoices.add(chordNode);
              else
                firstChoices.add(chordNode);
          });
    }

    return !firstChoices.isEmpty() ?
      firstChoices.get(TremendouslyRandom.zeroToLimit(firstChoices.size())) :
      !secondChoices.isEmpty() ?
        secondChoices.get(TremendouslyRandom.zeroToLimit(secondChoices.size())) :
        new ChordNode();
  }

  /**
   Is this chord node NOT an ending or a path to an ending?
   (the forward-reverse walk algorithm deprecates the need for an ending in either direction)

   @param nodeMap   to check whether the observed state leads to an ending
   @param chordNode the observed state to check whether it leads to an ending
   @return true if NOT an ending
   */
  private static boolean isNonEnding(Map<String, ChordMarkovNode> nodeMap, ChordNode chordNode) {
    String key = chordNode.toString();
    if (!nodeMap.containsKey(key)) return false;
    ChordMarkovNode firstNode = nodeMap.get(key);
    for (ChordNode node : firstNode.getNodes())
      if (node.isChord()) return true;
    return false;
  }

  @Override
  public List<Phase> getGeneratedPhases() {
    return Collections.unmodifiableList(generatedPhases);
  }

  @Override
  public Map<BigInteger, List<PhaseChord>> getGeneratedPhaseChords() {
    return Collections.unmodifiableMap(generatedPhaseChords);
  }

  @Override
  public Collection<ChordNode> getCoveredNodes() {
    return coveredNodeMap.values();
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
  public Ingest ingest() throws Exception {
    return ingest;
  }

  @Override
  public JSONObject toJSONObject() {
    JSONObject spObj = new JSONObject();
    spObj.put(KEY_PATTERN_ID, pattern.getId().toString());
    spObj.put(KEY_PATTERN_NAME, pattern.getName());
    spObj.put(KEY_PATTERN_TYPE, pattern.getType());
    JSONArray chordArr = new JSONArray();
    spObj.put(KEY_CHORD_SEQUENCE, chordArr);

    JSONObject result = new JSONObject();
    result.put(KEY_SUPERPATTERN, spObj);
    return result;
  }

}
