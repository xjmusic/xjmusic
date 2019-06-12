// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest.chord_progression.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.core.config.Config;
import io.xj.core.ingest.Ingest;
import io.xj.core.model.entity.Chord;
import io.xj.core.model.program.sub.SequenceChord;
import io.xj.craft.chord.ChordProgression;
import io.xj.craft.chord.SequenceChordProgression;
import io.xj.craft.digest.DigestType;
import io.xj.craft.digest.chord_progression.DigestChordProgression;
import io.xj.craft.digest.impl.DigestImpl;
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
 [#166746925] DEPRECATE SUPERSEQENCE/SUPERPATTERN FOR NOW

 <p>
 In-memory cache of ingest of all entities in a library
 <p>
 [#154234716] Architect wants ingest of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 */
public class DigestChordProgressionImpl extends DigestImpl implements DigestChordProgression {
  private Map<String, DigestChordProgressionItem> evaluatedSequenceMap = Maps.newHashMap();

  /**
   Instantiate a new digest with a collection of target entities

   @param ingest to digest
   */
  @Inject
  public DigestChordProgressionImpl(
    @Assisted("ingest") Ingest ingest
  ) {
    super(ingest, DigestType.DigestChordProgression);
    try {
      digest();
      prune();
    } catch (Exception e) {
      Logger log = LoggerFactory.getLogger(DigestChordProgressionImpl.class);
      log.error("Failed to digest entities of ingest {}", ingest, e);
    }
  }

  /**
   Express a chord as a JSON object

   @param chord to express
   @return JSON object
   */
  private static JSONObject toJSONObject(SequenceChord chord) {
    JSONObject result = new JSONObject();
//    result.put(KEY_CHORD_ID, chord.getId());   [#166746925] DEPRECATE SUPERSEQENCE/SUPERPATTERN FOR NOW
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
    Map<BigInteger, Boolean> uniqueParents = Maps.newHashMap();
    evaluatedSequence.getUsages().forEach(chordProgression -> uniqueParents.put(chordProgression.getParentId(), true));
    int diversity = uniqueParents.keySet().size();
    return (int) (evaluatedSequence.getUsages().size() * (descriptorLength - 1) * StrictMath.pow(diversity, 2));
  }

  /**
   Get only the audio entities of a particular audio

   @param sequenceChords to search for entities
   @param parentId       (pattern) to get pattern entities of
   @return collection of audio entities
   */
  private static <C extends SequenceChord> Collection<C> sequenceChordsOf(Collection<C> sequenceChords, Object parentId) {
    Collection<C> result = Lists.newArrayList();
    sequenceChords.forEach(sequenceChord -> {
      if (Objects.equals(parentId, sequenceChord.getParentId())) result.add(sequenceChord);
    });
    return result;
  }

  /**
   Compute all possible chord progressions given a set of entities (e.g. from a Pattern or Audio)

   @param parentId parent of entities
   @param chords   to compute all possible sequences of
   @return array of patternMap
   */
  private static Collection<SequenceChordProgression> computeChordProgressions(BigInteger parentId, Collection<SequenceChord> chords) {
    List<SequenceChordProgression> result = Lists.newArrayList();

    List<SequenceChord> allChords = Lists.newArrayList(chords);
    allChords.sort(Chord.byPositionAscending);

    int totalChords = allChords.size();
    for (int fromChord = 0; fromChord < totalChords; fromChord++) {
      int maxToChord = Math.min(totalChords, fromChord + Config.getIngestChordProgressionLengthMax());
      for (int toChord = fromChord; toChord < maxToChord; toChord++) {
        List<SequenceChord> subset = Lists.newArrayList();
        for (int i = fromChord; i <= toChord; i++) {
          subset.add(allChords.get(i));
        }
        result.add(new SequenceChordProgression(parentId, subset));
      }
    }

    return result;
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

  /*
  FUTURE: custom JSON serializer for DigestChordProgression

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

   */

  /**
   Digest entities from ingest
   */
  private void digest() {
    for (SequenceChordProgression chordProgression : computeAllChordProgressions())
      storeDigestedSequence(chordProgression);
  }

  /**
   Compute all possible chord progressions for contents in ingest
   */
  private Collection<SequenceChordProgression> computeAllChordProgressions() {
    Collection<SequenceChordProgression> result = Lists.newArrayList();

/*
  [#166746925] DEPRECATE SUPERSEQENCE/SUPERPATTERN FOR NOW

    ingest.getAllPatterns().forEach(pattern ->
      result.addAll(computeChordProgressions(pattern.getId(), pattern.getChords())));
*/

    return result;
  }

  /*

  [#166746925] DEPRECATE SUPERSEQENCE/SUPERPATTERN FOR NOW

   Express all the chord progressions for this descriptor as a JSON object for reporting

   @return evaluated library chord progression as JSON
   *
  private JSONObject toJSONObject(DigestChordProgressionItem evaluatedSequence) {
    JSONObject result = new JSONObject();
    JSONArray chordSequenceArr = new JSONArray();
    evaluatedSequence.getUsages().forEach(chordProgression -> chordSequenceArr.put(toJSONObject(chordProgression)));
    result.put(KEY_CHORD_PROGRESSIONS, chordSequenceArr);
    result.put(KEY_DESCRIPTOR, evaluatedSequence.getChordProgression().toString());
    return result;
  }

  /*
   Express all the chord progressions for this descriptor as a JSON object for reporting

   @return evaluated library chord progression as JSON
   *
  private JSONObject toJSONObject(SequenceChordProgression chordProgression) {
    JSONObject result = new JSONObject();

    Pattern pattern = getPattern(chordProgression.getParentId());
    result.put(KEY_PATTERN_ID, pattern.getId().toString());
    result.put(KEY_PATTERN_NAME, pattern.getName());
    result.put(KEY_PATTERN_TYPE, pattern.getType());

    Sequence sequence = getProgram(pattern.getSequenceId());
    result.put(KEY_SEQUENCE_ID, sequence.getId().toString());
    result.put(KEY_SEQUENCE_NAME, sequence.getName());
    result.put(KEY_PROGRAM_TYPE, sequence.getType());

    JSONArray chordArr = new JSONArray();
    chordProgression.getChords().forEach(chord -> chordArr.put(toJSONObject(chord)));
    result.put(KEY_CHORDS, chordArr);
    return result;
  }
   */

  /**
   Put a SequenceChord progression into the in-memory store

   @param chordProgression to store
   */
  private void storeDigestedSequence(SequenceChordProgression chordProgression) {
    ChordProgression descriptor = chordProgression.getChordProgression();
    String descriptorString = descriptor.toString();
    if (!evaluatedSequenceMap.containsKey(descriptorString))
      evaluatedSequenceMap.put(descriptorString, new DigestChordProgressionItem(descriptor));

    evaluatedSequenceMap.get(descriptorString).add(chordProgression);
  }

  /**
   Prune redundant subsets of all sequences
   Config property ingestChordProgressionPreserveLengthMin() specifies a threshold X, where during pruning of redundant subsets of chord progressions, a redundant subset with length greater than or equal to X will have its chord progressions preserved, meaning that they are moved into the ingest that is deprecating their original sequence descriptor.
   Also, we only preserve one redundant subset per unique parent id
   */
  private void prune() {
    Map<String, DigestChordProgressionItem> prunedSequenceMap = Maps.newHashMap();

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
      if (new ChordProgression(haystack).isRedundantSubset(new ChordProgression(needle), Config.getIngestChordProgressionRedundancyThreshold())) {
        redundantDescriptors.add(needle);
        //
        // preserve if length greater than or equal to threshold
        // BUT don't preserve if we already have one with this parent
        // AND don't preserve if we have already preserved these entities (by id)
        if (evaluatedNeedle.getDescriptorLength() >= Config.getIngestChordProgressionPreserveLengthMin())
          evaluatedNeedle.getUsages().forEach((SequenceChordProgression candidate) -> {
            if (candidate.getChords().size() >= evaluatedHaystack.getDescriptorLength() - Config.getIngestChordProgressionRedundancyThreshold())
              evaluatedHaystack.addIfUniqueParent(candidate);
          });
      }
    }));

    prunedDescriptors.removeAll(redundantDescriptors);
    evaluatedSequenceMap = prunedSequenceMap;
  }

  @Override
  public DigestChordProgression validate() {
    return this;
  }
}

