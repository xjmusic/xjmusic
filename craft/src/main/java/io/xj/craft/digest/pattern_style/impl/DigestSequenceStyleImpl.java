// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest.pattern_style.impl;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.math.StatsAccumulator;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.core.model.chord.Chord;
import io.xj.core.model.chord.Sort;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern_chord.PatternChord;
import io.xj.core.model.sequence.Sequence;
import io.xj.core.model.sequence.SequenceType;
import io.xj.craft.digest.DigestType;
import io.xj.craft.digest.impl.DigestImpl;
import io.xj.craft.digest.pattern_style.DigestSequenceStyle;
import io.xj.core.ingest.Ingest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

/**
 In-memory cache of ingest of all memes in a library
 <p>
 [#154234716] Architect wants ingest of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 */
@SuppressWarnings("UnstableApiUsage")
public class DigestSequenceStyleImpl extends DigestImpl implements DigestSequenceStyle {
  private final Logger log = LoggerFactory.getLogger(DigestSequenceStyleImpl.class);
  private final Multiset<Double> mainChordSpacingHistogram = ConcurrentHashMultiset.create();
  private final Multiset<Integer> mainPatternsPerSequenceHistogram = ConcurrentHashMultiset.create();
  private final Multiset<Integer> mainPatternTotalHistogram = ConcurrentHashMultiset.create();
  private final StatsAccumulator mainPatternsPerSequenceStats = new StatsAccumulator();
  private final StatsAccumulator mainPatternTotalStats = new StatsAccumulator();

  /**
   Instantiate a new digest with a collection of target entities

   @param ingest to digest
   */
  @Inject
  public DigestSequenceStyleImpl(
    @Assisted("ingest") Ingest ingest
  ) {
    super(ingest, DigestType.DigestSequenceStyle);
    try {
      digest();
    } catch (Exception e) {
      log.error("Failed to digest sequence style of ingest {}", ingest, e);
    }
  }

  /**
   Digest entities from ingest
   */
  private void digest() {
    for (Sequence sequence : ingest.getSequencesOfType(SequenceType.Main)) {
      Collection<Pattern> patterns = ingest.getPatternsOfSequence(sequence.getId());
      mainPatternsPerSequenceStats.add(patterns.size());
      mainPatternsPerSequenceHistogram.add(patterns.size());
      for (Pattern pattern : patterns) {
        Integer total = pattern.getTotal();
        mainPatternTotalStats.add(total);
        mainPatternTotalHistogram.add(total);

        List<PatternChord> chords = Lists.newArrayList(ingest.getChordsOfPattern(pattern.getId()));
        chords.sort(Sort.byPositionAscending);
        double cursor = 0;
        for (Chord chord : chords)
          if (chord.getPosition() > cursor) {
            mainChordSpacingHistogram.add(chord.getPosition() - cursor);
            cursor = chord.getPosition();
          }
      }
    }
    log.debug("Digested style of {} main-type sequences containing {} patterns.", mainPatternsPerSequenceStats.count(), mainPatternTotalStats.count());
  }

  /**
   Represent a stats accumulator as JSON

   @param stats     to represent
   @param histogram to represent within stats
   @return json object
   */
  private static JSONObject toJSONObject(StatsAccumulator stats, Multiset<Integer> histogram) {
    JSONObject result = new JSONObject();
    try {
      result.put(KEY_STAT_MIN, stats.min());
    } catch (Exception ignored) {
    }
    try {
      result.put(KEY_STAT_MAX, stats.max());
    } catch (Exception ignored) {
    }
    try {
      result.put(KEY_STAT_COUNT, stats.count());
    } catch (Exception ignored) {
    }
    try {
      result.put(KEY_STAT_MEAN, stats.mean());
    } catch (Exception ignored) {
    }
    result.put(KEY_HISTOGRAM, toJSONArray(histogram));
    return result;
  }

  /**
   Represent a number histogram as a json array

   @return json array
   */
  private static <N extends Number> JSONArray toJSONArray(Multiset<N> histogram) {
    JSONArray result = new JSONArray();
    histogram.elementSet().forEach((total) -> {
      JSONObject obj = new JSONObject();
      obj.put(KEY_STAT_VALUE, total);
      obj.put(KEY_STAT_COUNT, histogram.count(total));
      result.put(obj);
    });
    return result;
  }

  /*
  TODO: custom JSON serializer for DigestChordProgression

  public JSONObject toJSONObject() {
    JSONObject result = new JSONObject();
    JSONObject sequenceStyle = new JSONObject();
    sequenceStyle.put(KEY_MAIN_PATTERNS_PER_SEQUENCE, toJSONObject(mainPatternsPerSequenceStats, mainPatternsPerSequenceHistogram));
    sequenceStyle.put(KEY_MAIN_PATTERN_TOTAL, toJSONObject(mainPatternTotalStats, mainPatternTotalHistogram));
    sequenceStyle.put(KEY_MAIN_CHORD_SPACING_HISTOGRAM, toJSONArray(mainChordSpacingHistogram));
    result.put(KEY_SEQUENCE_STYLE, sequenceStyle);
    return result;
  }

   */

  @Override
  public Multiset<Integer> getMainPatternsPerSequenceHistogram() {
    return mainPatternsPerSequenceHistogram;
  }

  @Override
  public StatsAccumulator getMainPatternsPerSequenceStats() {
    return mainPatternsPerSequenceStats;
  }

  @Override
  public StatsAccumulator getMainPatternTotalStats() {
    return mainPatternTotalStats;
  }

  @Override
  public Multiset<Integer> getMainPatternTotalHistogram() {
    return mainPatternTotalHistogram;
  }

  @Override
  public Multiset<Double> getMainChordSpacingHistogram() {
    return mainChordSpacingHistogram;
  }
}
