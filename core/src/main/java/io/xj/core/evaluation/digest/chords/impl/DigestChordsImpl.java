// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.evaluation.digest.chords.impl;

import io.xj.core.config.Config;
import io.xj.core.evaluation.digest.DigestType;
import io.xj.core.evaluation.Evaluation;
import io.xj.core.evaluation.digest.chords.DigestChords;
import io.xj.core.evaluation.impl.DigestImpl;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.chord.Chord;
import io.xj.core.model.chord.ChordSequence;
import io.xj.core.model.chord.ChordSequenceType;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.phase.Phase;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 In-memory cache of evaluation of all chords in a library
 <p>
 [#154234716] Architect wants evaluation of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 */
public class DigestChordsImpl extends DigestImpl implements DigestChords {
  private final Logger log = LoggerFactory.getLogger(DigestChordsImpl.class);
  Map<String, DigestChordsItem> evaluatedSequenceMap = Maps.newConcurrentMap();

  /**
   Instantiate a new digest with a collection of target entities

   @param evaluation to digest
   */
  @Inject
  public DigestChordsImpl(
    @Assisted("evaluation") Evaluation evaluation
  ) {
    super(evaluation, DigestType.DigestChords);
    try {
      digest();
      prune();
    } catch (Exception e) {
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
  private static int score(DigestChordsItem evaluatedSequence) {
    int descriptorLength = evaluatedSequence.getDescriptorLength();
    Map<BigInteger, Boolean> uniqueParents = Maps.newConcurrentMap();
    evaluatedSequence.getUsages().forEach(chordSequence -> uniqueParents.put(chordSequence.getParentId(), true));
    int diversity = uniqueParents.keySet().size();
    return (int) (evaluatedSequence.getUsages().size() * (descriptorLength - 1) * StrictMath.pow(diversity, 2));
  }

  /**
   Yes, the order of elements in JSON arrays is preserved,
   per RFC 7159 JavaScript Object Notation (JSON) Data Interchange Format (emphasis mine).

   @return json object containing ORDERED ARRAY of evaluated chord sequences
   */
  @Override
  public JSONObject toJSONObject() {
    JSONObject result = new JSONObject();
    JSONArray sequencesArr = new JSONArray();

    List<DigestChordsItem> evaluatedSequences = Lists.newArrayList(evaluatedSequenceMap.values());
    evaluatedSequences.sort(DigestChordsItem.byUsageTimesLengthDescending);
    evaluatedSequences.forEach(evaluatedSequence -> sequencesArr.put(toJSONObject(evaluatedSequence)));

    result.put(KEY_CHORD_SEQUENCES_BY_DESCRIPTOR, sequencesArr);
    return result;
  }

  /**
   Digest entities from evaluation
   */
  private void digest() throws Exception {
    for (ChordSequence chordSequence : evaluation.chordSequences())
      storeDigestedSequence(chordSequence);
  }

  /**
   Express all the chord sequences for this descriptor as a JSON object for reporting

   @return evaluated library chord sequence as JSON
   */
  private JSONObject toJSONObject(DigestChordsItem evaluatedSequence) {
    JSONObject result = new JSONObject();
    JSONArray chordSequenceArr = new JSONArray();
    evaluatedSequence.getUsages().forEach(chordSequence -> chordSequenceArr.put(toJSONObject(chordSequence)));
    result.put(KEY_CHORD_SEQUENCES, chordSequenceArr);
    result.put(KEY_DESCRIPTOR, evaluatedSequence.getDescriptor());
    return result;
  }

  /**
   Express all the chord sequences for this descriptor as a JSON object for reporting

   @return evaluated library chord sequence as JSON
   */
  private JSONObject toJSONObject(ChordSequence chordSequence) {
    JSONObject result = new JSONObject();
    ChordSequenceType chordSequenceType = chordSequence.getType();

    if (ChordSequenceType.PhaseChordSequence == chordSequenceType) {
      Phase phase = getPhase(chordSequence.getParentId());
      result.put(KEY_PHASE_ID, phase.getId().toString());
      result.put(KEY_PHASE_NAME, phase.getName());
      result.put(KEY_PHASE_TYPE, phase.getType());
      Pattern pattern = getPattern(phase.getPatternId());
      result.put(KEY_PATTERN_ID, pattern.getId().toString());
      result.put(KEY_PATTERN_NAME, pattern.getName());
      result.put(KEY_PATTERN_TYPE, pattern.getType());

    } else if (ChordSequenceType.AudioChordSequence == chordSequenceType) {
      Audio audio = getAudio(chordSequence.getParentId());
      result.put(KEY_AUDIO_ID, audio.getId().toString());
      result.put(KEY_AUDIO_NAME, audio.getName());
      Instrument instrument = getInstrument(audio.getInstrumentId());
      result.put(KEY_INSTRUMENT_ID, instrument.getId().toString());
      result.put(KEY_INSTRUMENT_DESCRIPTION, instrument.getDescription());
      result.put(KEY_INSTRUMENT_TYPE, instrument.getType());
    }

    JSONArray chordArr = new JSONArray();
    chordSequence.getChords().forEach(chord -> chordArr.put(toJSONObject(chord)));
    result.put(KEY_CHORDS, chordArr);
    result.put(KEY_CHORD_SEQUENCE_TYPE, chordSequence.getType().toString());
    return result;
  }

  /**
   Put a Chord sequence into the in-memory store

   @param chordSequence to store
   */
  private void storeDigestedSequence(ChordSequence chordSequence) {
    String descriptor = chordSequence.getDescriptor();
    if (!evaluatedSequenceMap.containsKey(descriptor))
      evaluatedSequenceMap.put(descriptor, new DigestChordsItem(descriptor));

    evaluatedSequenceMap.get(descriptor).add(chordSequence);
  }

  /**
   Prune redundant subsets of all sequences
   Config property evaluationChordSequencePreserveLengthMin() specifies a threshold X, where during pruning of redundant subsets of chord sequence descriptors, a redundant subset with length greater than or equal to X will have its chord sequences preserved, meaning that they are moved into the evaluation that is deprecating their original sequence descriptor.
   Also, we only preserve one redundant subset per unique parent id
   */
  private void prune() {
    List<String> scoredDescriptors = Lists.newArrayList(evaluatedSequenceMap.keySet());
    scoredDescriptors.sort(Comparator.comparingInt(o -> -score(evaluatedSequenceMap.get(o)))); // determines the order of the first step of the pruning operation that's about to happen
    Map<String, DigestChordsItem> prunedSequenceMap = Maps.newConcurrentMap();

    // Starting with the highest scored descriptors,
    scoredDescriptors.forEach(descriptor -> {
      //
      DigestChordsItem evaluatedSequence = evaluatedSequenceMap.get(descriptor);
      evaluatedSequence.getUsages().forEach(chordSequence -> {
        //
        if (!prunedSequenceMap.containsKey(descriptor))
          prunedSequenceMap.put(descriptor, new DigestChordsItem(descriptor));
        prunedSequenceMap.get(descriptor).addIfUniqueParent(chordSequence);
      });
    });

    // Prune redundant subsets
    List<String> prunedDescriptors = Lists.newArrayList(prunedSequenceMap.keySet());
    prunedDescriptors.sort(Comparator.comparingInt(o -> ChordSequence.splitDescriptor(o).size())); // determines the order of the 2-dimensional matrix operation that's about to happen
    List<String> redundantDescriptors = Lists.newArrayList();
    prunedDescriptors.forEach(haystack -> prunedDescriptors.forEach(needle -> {
      //
      DigestChordsItem evaluatedNeedle = prunedSequenceMap.get(needle);
      DigestChordsItem evaluatedHaystack = prunedSequenceMap.get(haystack);
      if (ChordSequence.isRedundantSubsetOfDescriptor(needle, haystack, Config.evaluationChordSequenceRedundancyThreshold())) {
        redundantDescriptors.add(needle);
        //
        // preserve if length greater than or equal to threshold
        // BUT don't preserve if we already have one with this parent
        // AND don't preserve if we have already preserved these chords (by id)
        if (evaluatedNeedle.getDescriptorLength() >= Config.evaluationChordSequencePreserveLengthMin())
          evaluatedNeedle.getUsages().forEach((ChordSequence candidate) -> {
            if (candidate.getChords().size() >= evaluatedHaystack.getDescriptorLength() - Config.evaluationChordSequenceRedundancyThreshold())
              evaluatedHaystack.addIfUniqueParent(candidate);
          });
      }
    }));

    prunedDescriptors.removeAll(redundantDescriptors);
    evaluatedSequenceMap = prunedSequenceMap;
  }

}

