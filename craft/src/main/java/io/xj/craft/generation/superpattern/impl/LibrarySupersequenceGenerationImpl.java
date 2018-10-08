// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.generation.supersequence.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.core.config.Config;
import io.xj.core.dao.PatternChordDAO;
import io.xj.core.dao.PatternDAO;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.pattern_chord.PatternChord;
import io.xj.core.model.sequence.Sequence;
import io.xj.core.util.TremendouslyRandom;
import io.xj.craft.chord.ChordMarkovNode;
import io.xj.craft.chord.ChordNode;
import io.xj.craft.chord.ChordProgression;
import io.xj.craft.chord.PatternChordProgression;
import io.xj.craft.digest.Digest;
import io.xj.craft.digest.cache.DigestCacheProvider;
import io.xj.craft.digest.chord_markov.DigestChordMarkov;
import io.xj.craft.digest.sequence_style.DigestSequenceStyle;
import io.xj.craft.generation.GenerationType;
import io.xj.craft.generation.impl.GenerationImpl;
import io.xj.craft.generation.supersequence.LibrarySupersequenceGeneration;
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
import java.util.Set;

/**
 [#154548999] Artist wants to generate a Library Supersequence in order to create a Detail sequence that covers the chord progressions of all existing Main Sequences in a Library.
 <p>
 [#154855269] Artist expects generation of library supersequence to result in a set of patterns of similar size.
 <p>
 [#154882199] Artist expects a generated supersequence to have near-complete coverage of all chord nodes in the library. NOTE: Revised algorithm does not discard entropy using any sort of filter-- entropy of original "dice roll" is maintained, and the reconciliation of forward-reverse chord markov walks is performed using a "tonal similarity" comparison of all entities involved. For extra credit, we should make all potential pattern totals available to that algorithm, choosing the one that suits the best available match of chord for splicing forward and reverse sequences.
 <p>
 [#154927374] Architect wants Chord Markov digest to compute both forward nodes (likelihood of following node based on observation of preceding nodes) and reverse nodes (likelihood of preceding node based on observation of following nodes in reverse time), in order to implement forward-reverse random walk during generation, in order to generate a supersequence with excellent coverage of the library contents.
 <p>
 [#154985984] Architect wants to splice the forward and reverse walk at the best possible point, determined by scoring all possible pattern totals (from sequence style digest) combined with all possible splice points (fuzzy matched by tonal similarity), in order to retain the highest quality entropy from the original walks.
 */
public class LibrarySupersequenceGenerationImpl extends GenerationImpl implements LibrarySupersequenceGeneration {
  private static final double defaultChordSpacing = 4.0;
  private static final int defaultMaxPatternTotal = 16;
  private static final int defaultNumPatterns = 1;
  private static final int defaultPatternTotal = 16;
  private static final String GENERATED_PATTERN_NAME = "Library Superpattern";
  private final int spliceSafetyMargin = Config.generationSpliceSafetyMargin();
  private final DigestChordMarkov digestChordMarkov;
  private final DigestSequenceStyle digestSequenceStyle;
  private final double chordSpacing;
  private final int markovOrder = Config.chordMarkovOrder();
  private final int maxPatternTotal;
  private final int numPatterns;
  private final int sequencePatternsMultiplier = Config.generationSequencePatternsMultiplier();
  private final List<Pattern> generatedPatterns = Lists.newArrayList();
  private final Logger log = LoggerFactory.getLogger(LibrarySupersequenceGenerationImpl.class);
  private final Map<BigInteger, List<PatternChord>> generatedPatternChords = Maps.newConcurrentMap();
  private final Map<String, ChordNode> coveredNodeMap = Maps.newConcurrentMap();
  private final Sequence sequence;
  private final PatternChordDAO patternChordDAO;
  private final PatternDAO patternDAO;
  private final Set<Integer> progressionSizes;

  /**
   Instantiate a new digest with a collection of target entities

   @param sequence to build supersequence around
   @param ingest   to digest
   */
  @Inject
  public LibrarySupersequenceGenerationImpl(
    @Assisted("sequence") Sequence sequence,
    @Assisted("ingest") Ingest ingest,
    PatternDAO patternDAO,
    PatternChordDAO patternChordDAO,
    DigestCacheProvider digestCacheProvider
  ) {
    super(ingest, GenerationType.LibrarySupersequence);
    this.sequence = sequence;
    this.patternDAO = patternDAO;
    this.patternChordDAO = patternChordDAO;
    digestSequenceStyle = digestCacheProvider.sequenceStyle(ingest);
    digestChordMarkov = digestCacheProvider.chordMarkov(ingest);
    chordSpacing = Digest.mostPopular(digestSequenceStyle.getMainChordSpacingHistogram(), defaultChordSpacing);
    progressionSizes = Digest.elementsDividedBy(digestSequenceStyle.getMainPatternTotalHistogram(), chordSpacing, defaultPatternTotal);
    numPatterns = Digest.lottery(digestSequenceStyle.getMainPatternsPerSequenceHistogram(), defaultNumPatterns) * sequencePatternsMultiplier;
    maxPatternTotal = (int) Digest.max(digestSequenceStyle.getMainPatternTotalStats(), defaultMaxPatternTotal);
    try {
      for (ChordProgression chordProgression : generateChordProgressions())
        createPatternAndChords(chordProgression);

    } catch (Exception e) {
      log.error("Failed to generate supersequence of ingest {}", ingest, e);
    }
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
    if (!chordNode.isChord()) return false;
    if (!nodeMap.containsKey(key)) return false;
    ChordMarkovNode firstNode = nodeMap.get(key);
    for (ChordNode node : firstNode.getNodes())
      if (node.isChord()) return true;
    return false;
  }

  /**
   Create a pattern and all pattern entities for a given chord progression

   @param chordProgression to create pattern of
   */
  private void createPatternAndChords(ChordProgression chordProgression) throws Exception {
    if (chordProgression.isEmpty()) {
      log.warn("Cannot create a pattern out of an empty chord progression!");
      return;
    }

    Pattern pattern = patternDAO.create(ingest.access(),
      new Pattern()
        .setSequenceId(sequence.getId())
        .setName(GENERATED_PATTERN_NAME)
        .setTypeEnum(PatternType.Loop)
        .setTotal((int) Math.floor(chordSpacing * chordProgression.size())));

    // Create pattern entities via DAO, for all entities in the chord progression
    PatternChordProgression patternChordProgression = new PatternChordProgression(chordProgression, pattern.getId(), Key.of(sequence.getKey()).getRootPitchClass(), chordSpacing);
    patternChordProgression.getChords().forEach(patternChord -> {
      try {
        cache(patternChordDAO.create(ingest.access(), patternChord));
      } catch (Exception e) {
        log.error("Failure while generating pattern chord for library supersequence", e); // future: better error transport, somehow show this to the Hub UI user perhaps
      }
    });

    generatedPatterns.add(pattern);
  }

  /**
   Generate supersequence chord progression, from the chord Markov digest.
   - has a method that performs a random walk based on a DigestChordMarkov
   - begin each pattern on any chord that appeared at the beginning of an observed sequence.
   - continues its random walk until all chord forms in the library have appeared at least N times.
   - may end a pattern if that outcome is selected from the preceding state descriptor-- meaning that it was observed that a sequence ended after that chord, and therefore this pattern will now end.
   - transposes all of its observations into the Key of the supersequence it's generating-- meaning that all chord progressions in a song (the ones that matter are at the beginning at end of sequences) are interpreted relative to the key of the sequence they are observed in.
   - Generate patterns (1 chord progress = 1 pattern) until done

   @return chord progression
   */
  private List<ChordProgression> generateChordProgressions() {
    List<ChordProgression> result = Lists.newArrayList();

    for (int n = 0; n < numPatterns; n++) {
      result.add(generateChordProgression(maxPatternTotal, chordSpacing));
    }

    return result;
  }

  /**
   Generate a chord progression, by collision or random splice of forward-reverse random walks

   @param spacing for resulting chord progressions
   @param total   # beats in between entities, for resulting chord progressions
   @return chord progression
   */
  private ChordProgression generateChordProgression(Integer total, Double spacing) {
    return
      generateChordProgression(digestChordMarkov.getForwardNodeMap(), spacing, total)
        .spliceAtCollision(
          generateChordProgression(digestChordMarkov.getReverseNodeMap(), spacing, total).reversed(),
          progressionSizes, spliceSafetyMargin);
  }

  /**
   Generate a chord progression (pattern).
   Select a # of beats total for the progression, by random lottery from all pattern totals in ingested main sequences.
   Select the most popular # of beats of inter-chord spacing and use that for everything.

   @param nodeMap to check precedent states for markov observations
   @param spacing for resulting chord progressions
   @param total   # beats in between entities, for resulting chord progressions
   @return chord progression
   */
  private ChordProgression generateChordProgression(Map<String, ChordMarkovNode> nodeMap, Double spacing, Integer total) {
    // note the RESULT is different from the BUFFER (only caches previous N entities)
    List<ChordNode> result = Lists.newArrayList();
    List<ChordNode> buf = Lists.newArrayList();

    // "beginning of pattern" marker (null bookend)
    buf.add(new ChordNode());

    // Search Markov nodes for precedent state buffer contents, from 1 to N orders of depth, and add all possibilities
    for (int n = 0; n < total / spacing; n++) {
      ChordNode next = selectRandomNonEndingObservation(nodeMap, buf);
      buf.add(next);
      result.add(next);
      if (buf.size() > markovOrder) buf.remove(0);
    }

    // mark these entities as used
    result.forEach(chordNode -> coveredNodeMap.put(chordNode.toString(), chordNode));

    // resulting chord progression includes pattern total and chord spacing
    return new ChordProgression(result);
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
  private ChordNode selectRandomNonEndingObservation(Map<String, ChordMarkovNode> nodeMap, List<ChordNode> buffer) {
    List<ChordNode> firstChoices = Lists.newArrayList();
    List<ChordNode> secondChoices = Lists.newArrayList();
    int size = buffer.size();
    for (int n = 0; n < size; n++) {
      String key = new ChordMarkovNode(buffer.subList(n, size)).precedentStateDescriptor();
      if (nodeMap.containsKey(key))
        for (int r = 0; r <= n; r++) // add observations N times
          nodeMap.get(key).getNodes().forEach(chordNode -> {
            if (isNonEnding(nodeMap, chordNode)) {
              if (coveredNodeMap.containsKey(chordNode.toString()))
                secondChoices.add(chordNode);
              else
                firstChoices.add(chordNode);
            }
          });
    }

    if (!firstChoices.isEmpty())
      return firstChoices.get(TremendouslyRandom.zeroToLimit(firstChoices.size()));
    else if (!secondChoices.isEmpty())
      return secondChoices.get(TremendouslyRandom.zeroToLimit(secondChoices.size()));
    else
      return new ChordNode();
  }

  /**
   Cache a generated pattern chord.
   generatedPatternChords is keyed by pattern id.
   Each pattern id contains a list of pattern entities.

   @param patternChord to cache
   */
  private void cache(PatternChord patternChord) {
    if (!generatedPatternChords.containsKey(patternChord.getPatternId()))
      generatedPatternChords.put(patternChord.getPatternId(), Lists.newArrayList());

    generatedPatternChords.get(patternChord.getPatternId()).add(patternChord);
  }

  @Override
  public List<Pattern> getGeneratedPatterns() {
    return Collections.unmodifiableList(generatedPatterns);
  }

  @Override
  public Map<BigInteger, List<PatternChord>> getGeneratedPatternChords() {
    return Collections.unmodifiableMap(generatedPatternChords);
  }

  @Override
  public Collection<ChordNode> getCoveredNodes() {
    return coveredNodeMap.values();
  }

  @Override
  public Sequence getSequence() {
    return sequence;
  }

  @Override
  public GenerationType type() {
    return type;
  }

  @Override
  public Ingest ingest() {
    return ingest;
  }

  @Override
  public JSONObject toJSONObject() {
    JSONObject spObj = new JSONObject();
    spObj.put(KEY_SEQUENCE_ID, sequence.getId().toString());
    spObj.put(KEY_SEQUENCE_NAME, sequence.getName());
    spObj.put(KEY_SEQUENCE_TYPE, sequence.getType());
    JSONArray chordArr = new JSONArray();
    spObj.put(KEY_CHORD_SEQUENCE, chordArr);

    JSONObject result = new JSONObject();
    result.put(KEY_SUPERSEQUENCE, spObj);
    return result;
  }

}
