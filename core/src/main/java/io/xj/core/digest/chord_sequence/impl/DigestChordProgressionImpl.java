// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.digest.chord_sequence.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import io.xj.core.config.Config;
import io.xj.core.digest.DigestType;
import io.xj.core.digest.chord_sequence.DigestChordProgression;
import io.xj.core.digest.impl.DigestImpl;
import io.xj.core.evaluation.Evaluation;
import io.xj.core.model.chord.Chord;
import io.xj.core.model.chord.ChordProgression;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.phase.Phase;
import io.xj.core.model.phase_chord.PhaseChord;
import io.xj.core.model.phase_chord.PhaseChordProgression;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 In-memory cache of evaluation of all chords in a library
 <p>
 [#154234716] Architect wants evaluation of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 */
public class DigestChordProgressionImpl extends DigestImpl implements DigestChordProgression {
  private Map<String, DigestChordProgressionItem> evaluatedSequenceMap = Maps.newConcurrentMap();

  /**
   Instantiate a new digest with a collection of target entities

   @param evaluation to digest
   */
  @Inject
  public DigestChordProgressionImpl(
    @Assisted("evaluation") Evaluation evaluation
  ) {
    super(evaluation, DigestType.DigestChordProgression);
    try {
      digest();
      prune();
    } catch (Exception e) {
      Logger log = LoggerFactory.getLogger(DigestChordProgressionImpl.class);
      log.error("Failed to digest chords of evaluation {}", evaluation, e);
    }
  }

  /**
   Express a chord as a JSON object

   @param chord to express
   @return JSON object
   */
  private static JSONObject toJSONObject(Chord chord) {
    JSONObject result = new JSONObject();
    result.put(KEY_CHORD_ID, chord.getId());
    result.put(KEY_CHORD_NAME, chord.getName());
    result.put(KEY_CHORD_POSITION, chord.getPosition());
    return result;
  }

  /**
   Get the score for an evaluated sequence,
   in order to prioritize which are kept and which are deprecated

   @param evaluatedSequence to score
   @return score
   */
  private static int score(DigestChordProgressionItem evaluatedSequence) {
    int descriptorLength = evaluatedSequence.getDescriptorLength();
    Map<BigInteger, Boolean> uniqueParents = Maps.newConcurrentMap();
    evaluatedSequence.getUsages().forEach(chordProgression -> uniqueParents.put(chordProgression.getParentId(), true));
    int diversity = uniqueParents.keySet().size();
    return (int) (evaluatedSequence.getUsages().size() * (descriptorLength - 1) * StrictMath.pow(diversity, 2));
  }

  @Override
  public Map<String, DigestChordProgressionItem> getEvaluatedSequenceMap() {
    return Collections.unmodifiableMap(evaluatedSequenceMap);
  }

  @Override
  public List<String> getSortedDescriptors() {
    List<String> result = Lists.newArrayList(evaluatedSequenceMap.keySet());
    result.sort(Comparator.comparingInt(o -> -score(evaluatedSequenceMap.get(o)))); // determines the order of the first step of the pruning operation that's about to happen
    return result;
  }

  @Override
  public JSONObject toJSONObject() {
    JSONObject result = new JSONObject();
    JSONArray sequencesArr = new JSONArray();

    List<DigestChordProgressionItem> evaluatedSequences = Lists.newArrayList(evaluatedSequenceMap.values());
    evaluatedSequences.sort(DigestChordProgressionItem.byUsageTimesLengthDescending);
    evaluatedSequences.forEach(evaluatedSequence -> sequencesArr.put(toJSONObject(evaluatedSequence)));

    result.put(KEY_CHORD_PROGRESSIONS, sequencesArr);
    return result;
  }

  /**
   Digest entities from evaluation
   */
  private void digest() throws Exception {
    for (PhaseChordProgression chordProgression : computeAllChordProgressions())
      storeDigestedSequence(chordProgression);
  }

  /**
   Compute all possible chord progressions for contents in evaluation
   */
  private Collection<PhaseChordProgression> computeAllChordProgressions() {
    Collection<PhaseChordProgression> result = Lists.newArrayList();

    evaluation.phases().forEach(phase ->
      result.addAll(computeChordProgressions(phase.getId(), phaseChordsOf(evaluation.phaseChords(), phase.getId()))));

    return result;
  }


  /**
   Get only the audio chords of a particular audio

   @param phaseChords to search for chords
   @param parentId    (phase) to get phase chords of
   @return collection of audio chords
   */
  private static <C extends PhaseChord> Collection<C> phaseChordsOf(Collection<C> phaseChords, Object parentId) {
    Collection<C> result = Lists.newArrayList();
    phaseChords.forEach(chord -> {
      if (Objects.equals(parentId, chord.getParentId())) result.add(chord);
    });
    return result;
  }

  /**
   Compute all possible chord progressions given a set of chords (e.g. from a Phase or Audio)

   @param parentId parent of chords
   @param chords   to compute all possible sequences of
   @return array of phaseMap
   */
  private static Collection<PhaseChordProgression> computeChordProgressions(BigInteger parentId, Collection<PhaseChord> chords) {
    List<PhaseChordProgression> result = Lists.newArrayList();

    List<PhaseChord> allChords = Lists.newArrayList(chords);
    allChords.sort(Chord.byPositionAscending);

    int totalChords = allChords.size();
    for (int fromChord = 0; fromChord < totalChords; fromChord++) {
      int maxToChord = Math.min(totalChords, fromChord + Config.evaluationChordProgressionLengthMax());
      for (int toChord = fromChord; toChord < maxToChord; toChord++) {
        List<PhaseChord> subset = Lists.newArrayList();
        for (int i = fromChord; i <= toChord; i++) {
          subset.add(allChords.get(i));
        }
        result.add(new PhaseChordProgression(parentId, subset));
      }
    }

    return result;
  }


  /**
   Express all the chord progressions for this descriptor as a JSON object for reporting

   @return evaluated library chord progression as JSON
   */
  private JSONObject toJSONObject(DigestChordProgressionItem evaluatedSequence) {
    JSONObject result = new JSONObject();
    JSONArray chordSequenceArr = new JSONArray();
    evaluatedSequence.getUsages().forEach(chordProgression -> chordSequenceArr.put(toJSONObject(chordProgression)));
    result.put(KEY_CHORD_PROGRESSIONS, chordSequenceArr);
    result.put(KEY_DESCRIPTOR, evaluatedSequence.getChordProgression().toString());
    return result;
  }

  /**
   Express all the chord progressions for this descriptor as a JSON object for reporting

   @return evaluated library chord progression as JSON
   */
  private JSONObject toJSONObject(PhaseChordProgression chordProgression) {
    JSONObject result = new JSONObject();

    Phase phase = getPhase(chordProgression.getParentId());
    result.put(KEY_PHASE_ID, phase.getId().toString());
    result.put(KEY_PHASE_NAME, phase.getName());
    result.put(KEY_PHASE_TYPE, phase.getType());

    Pattern pattern = getPattern(phase.getPatternId());
    result.put(KEY_PATTERN_ID, pattern.getId().toString());
    result.put(KEY_PATTERN_NAME, pattern.getName());
    result.put(KEY_PATTERN_TYPE, pattern.getType());

    JSONArray chordArr = new JSONArray();
    chordProgression.getChords().forEach(chord -> chordArr.put(toJSONObject(chord)));
    result.put(KEY_CHORDS, chordArr);
    return result;
  }

  /**
   Put a Chord progression into the in-memory store

   @param chordProgression to store
   */
  private void storeDigestedSequence(PhaseChordProgression chordProgression) {
    ChordProgression descriptor = chordProgression.getChordProgression();
    String descriptorString = descriptor.toString();
    if (!evaluatedSequenceMap.containsKey(descriptorString))
      evaluatedSequenceMap.put(descriptorString, new DigestChordProgressionItem(descriptor));

    evaluatedSequenceMap.get(descriptorString).add(chordProgression);
  }

  /**
   Prune redundant subsets of all sequences
   Config property evaluationChordProgressionPreserveLengthMin() specifies a threshold X, where during pruning of redundant subsets of chord progressions, a redundant subset with length greater than or equal to X will have its chord progressions preserved, meaning that they are moved into the evaluation that is deprecating their original sequence descriptor.
   Also, we only preserve one redundant subset per unique parent id
   */
  private void prune() {
    Map<String, DigestChordProgressionItem> prunedSequenceMap = Maps.newConcurrentMap();

    // Starting with the highest scored descriptors,
    getSortedDescriptors().forEach(descriptorString -> {
      //
      DigestChordProgressionItem evaluatedSequence = evaluatedSequenceMap.get(descriptorString);
      evaluatedSequence.getUsages().forEach(chordProgression -> {
        //
        if (!prunedSequenceMap.containsKey(descriptorString))
          prunedSequenceMap.put(descriptorString, new DigestChordProgressionItem(new ChordProgression(descriptorString)));
        prunedSequenceMap.get(descriptorString).addIfUniqueParent(chordProgression);
      });
    });

    // Prune redundant subsets
    List<String> prunedDescriptors = Lists.newArrayList(prunedSequenceMap.keySet());
    prunedDescriptors.sort(Comparator.comparingInt(o -> new ChordProgression(o).size())); // determines the order of the matrix operation that's about to happen
    List<String> redundantDescriptors = Lists.newArrayList();
    prunedDescriptors.forEach(haystack -> prunedDescriptors.forEach(needle -> {
      //
      DigestChordProgressionItem evaluatedNeedle = prunedSequenceMap.get(needle);
      DigestChordProgressionItem evaluatedHaystack = prunedSequenceMap.get(haystack);
      if (new ChordProgression(haystack).isRedundantSubset(new ChordProgression(needle), Config.evaluationChordProgressionRedundancyThreshold())) {
        redundantDescriptors.add(needle);
        //
        // preserve if length greater than or equal to threshold
        // BUT don't preserve if we already have one with this parent
        // AND don't preserve if we have already preserved these chords (by id)
        if (evaluatedNeedle.getDescriptorLength() >= Config.evaluationChordProgressionPreserveLengthMin())
          evaluatedNeedle.getUsages().forEach((PhaseChordProgression candidate) -> {
            if (candidate.getChords().size() >= evaluatedHaystack.getDescriptorLength() - Config.evaluationChordProgressionRedundancyThreshold())
              evaluatedHaystack.addIfUniqueParent(candidate);
          });
      }
    }));

    prunedDescriptors.removeAll(redundantDescriptors);
    evaluatedSequenceMap = prunedSequenceMap;
  }

}

