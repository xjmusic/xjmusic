// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.analysis.library_chord;

import io.xj.core.config.Config;
import io.xj.core.model.analysis.Analysis;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.chord.Chord;
import io.xj.core.model.chord.ChordSequence;
import io.xj.core.model.chord.ChordSequenceType;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.phase.Phase;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 In-memory cache of analysis of all chords in a library
 <p>
 [#154234716] Architect wants analysis of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 */
public class LibraryChordAnalysis extends Analysis {
  private static final String KEY_AUDIO_ID = "audioId";
  private static final String KEY_AUDIO_NAME = "audioName";
  private static final String KEY_CHORD_ID = "chordId";
  private static final String KEY_CHORD_NAME = "chordName";
  private static final String KEY_CHORD_POSITION = "chordPosition";
  private static final String KEY_CHORD_SEQUENCES = "chordSequences";
  private static final String KEY_CHORD_SEQUENCES_BY_DESCRIPTOR = "chordSequencesByDescriptor";
  private static final String KEY_CHORDS = "chords";
  private static final String KEY_DESCRIPTOR = "descriptor";
  private static final String KEY_PHASE_ID = "phaseId";
  private static final String KEY_PHASE_NAME = "phaseName";
  private static final String KEY_PHASE_TYPE = "phaseType";
  private static final String KEY_CHORD_SEQUENCE_TYPE = "chordSequenceType";
  private static final String KEY_PATTERN_ID = "patternId";
  private static final String KEY_PATTERN_NAME = "patternName";
  private static final String KEY_PATTERN_TYPE = "patternType";
  private static final String KEY_INSTRUMENT_ID = "instrumentId";
  private static final String KEY_INSTRUMENT_DESCRIPTION = "instrumentDescription";
  private static final String KEY_INSTRUMENT_TYPE = "instrumentType";
  Map<String, AnalyzedLibraryChordSequence> analyzedSequenceMap = Maps.newConcurrentMap();

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
   Get the score for an analyzed sequence,
   in order to prioritize which are kept and which are deprecated

   @param analyzedSequence to score
   @return score
   */
  static int score(AnalyzedLibraryChordSequence analyzedSequence) {
    int descriptorLength = analyzedSequence.getDescriptorLength();
    Map<BigInteger, Boolean> uniqueParents = Maps.newConcurrentMap();
    analyzedSequence.getUsages().forEach(chordSequence -> uniqueParents.put(chordSequence.getParentId(), true));
    int diversity = uniqueParents.keySet().size();
    return (int) (analyzedSequence.getUsages().size() * (descriptorLength - 1) * StrictMath.pow(diversity, 2));
  }


  /**
   Determine if chord sequence has at least one unique chord

   @param idMap         of chord id that are not unique
   @param chordSequence of chords
   @return true if at least one chord is unique
   */
  private static boolean isUniqueChordSequence(Map<BigInteger, String> idMap, ChordSequence chordSequence) {
    for (Chord chord : chordSequence.getChords())
      if (!idMap.containsKey(chord.getId())) return true;

    return false;
  }

  /**
   Express all the chord sequences for this descriptor as a JSON object for reporting

   @return analyzed library chord sequence as JSON
   */
  private JSONObject toJSONObject(AnalyzedLibraryChordSequence analyzedSequence) {
    JSONObject result = new JSONObject();
    JSONArray chordSequenceArr = new JSONArray();
    analyzedSequence.getUsages().forEach(chordSequence -> chordSequenceArr.put(toJSONObject(chordSequence)));
    result.put(KEY_CHORD_SEQUENCES, chordSequenceArr);
    result.put(KEY_DESCRIPTOR, analyzedSequence.getDescriptor());
    return result;
  }

  /**
   Express all the chord sequences for this descriptor as a JSON object for reporting

   @return analyzed library chord sequence as JSON
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
  public void putSequence(ChordSequence chordSequence) {
    String descriptor = chordSequence.getDescriptor();
    if (!analyzedSequenceMap.containsKey(descriptor))
      analyzedSequenceMap.put(descriptor, new AnalyzedLibraryChordSequence(descriptor));
    analyzedSequenceMap.get(descriptor).add(chordSequence);
  }

  /**
   Yes, the order of elements in JSON arrays is preserved,
   per RFC 7159 JavaScript Object Notation (JSON) Data Interchange Format (emphasis mine).

   @return json object containing ORDERED ARRAY of analyzed chord sequences
   */
  @Override
  public JSONObject toJSONObject() {
    JSONObject result = new JSONObject();
    JSONArray sequencesArr = new JSONArray();

    List<AnalyzedLibraryChordSequence> analyzedSequences = Lists.newArrayList(analyzedSequenceMap.values());
    analyzedSequences.sort(AnalyzedLibraryChordSequence.byUsageTimesLengthDescending);
    analyzedSequences.forEach(analyzedSequence -> sequencesArr.put(toJSONObject(analyzedSequence)));

    result.put(KEY_CHORD_SEQUENCES_BY_DESCRIPTOR, sequencesArr);
    return result;
  }

  /**
   Prune redundant subsets of all sequences
   Config property analysisChordSequencePreserveLengthMin() specifies a threshold X, where during pruning of redundant subsets of chord sequence descriptors, a redundant subset with length greater than or equal to X will have its chord sequences preserved, meaning that they are moved into the analysis that is deprecating their original sequence descriptor.
   Also, we only preserve one redundant subset per unique parent id
   */
  public void prune() {
    List<String> scoredDescriptors = Lists.newArrayList(analyzedSequenceMap.keySet());
    scoredDescriptors.sort(Comparator.comparingInt(o -> -score(analyzedSequenceMap.get(o)))); // determines the order of the first step of the pruning operation that's about to happen
    Map<String, AnalyzedLibraryChordSequence> prunedSequenceMap = Maps.newConcurrentMap();

    // Starting with the highest scored descriptors,
    scoredDescriptors.forEach(descriptor -> {
      //
      AnalyzedLibraryChordSequence analyzedSequence = analyzedSequenceMap.get(descriptor);
      analyzedSequence.getUsages().forEach(chordSequence -> {
        //
        if (!prunedSequenceMap.containsKey(descriptor))
          prunedSequenceMap.put(descriptor, new AnalyzedLibraryChordSequence(descriptor));
        prunedSequenceMap.get(descriptor).addIfUniqueParent(chordSequence);
      });
    });

    // Prune redundant subsets
    List<String> prunedDescriptors = Lists.newArrayList(prunedSequenceMap.keySet());
    prunedDescriptors.sort(Comparator.comparingInt(o -> ChordSequence.splitDescriptor(o).size())); // determines the order of the 2-dimensional matrix operation that's about to happen
    List<String> redundantDescriptors = Lists.newArrayList();
    prunedDescriptors.forEach(haystack -> prunedDescriptors.forEach(needle -> {
      //
      AnalyzedLibraryChordSequence analyzedNeedle = prunedSequenceMap.get(needle);
      AnalyzedLibraryChordSequence analyzedHaystack = prunedSequenceMap.get(haystack);
      if (ChordSequence.isRedundantSubsetOfDescriptor(needle, haystack, Config.analysisChordSequenceRedundancyThreshold())) {
        redundantDescriptors.add(needle);
        //
        // preserve if length greater than or equal to threshold
        // BUT don't preserve if we already have one with this parent
        // AND don't preserve if we have already preserved these chords (by id)
        if (analyzedNeedle.getDescriptorLength() >= Config.analysisChordSequencePreserveLengthMin())
          analyzedNeedle.getUsages().forEach((ChordSequence candidate) -> {
            if (candidate.getChords().size() >= analyzedHaystack.getDescriptorLength() - Config.analysisChordSequenceRedundancyThreshold())
              analyzedHaystack.addIfUniqueParent(candidate);
          });
      }
    }));

    prunedDescriptors.removeAll(redundantDescriptors);
    analyzedSequenceMap = prunedSequenceMap;
  }

}

