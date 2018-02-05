// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.craft.digest.pattern_style.impl;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.math.StatsAccumulator;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import io.xj.core.model.chord.Chord;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.phase.Phase;
import io.xj.core.model.phase_chord.PhaseChord;
import io.xj.craft.digest.DigestType;
import io.xj.craft.digest.impl.DigestImpl;
import io.xj.craft.digest.pattern_style.DigestPatternStyle;
import io.xj.craft.ingest.Ingest;
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
public class DigestPatternStyleImpl extends DigestImpl implements DigestPatternStyle {
  private final Logger log = LoggerFactory.getLogger(DigestPatternStyleImpl.class);
  private final Multiset<Integer> mainChordSpacingHistogram = ConcurrentHashMultiset.create();
  private final Multiset<Integer> mainPhasesPerPatternHistogram = ConcurrentHashMultiset.create();
  private final Multiset<Integer> mainPhaseTotalHistogram = ConcurrentHashMultiset.create();
  private final StatsAccumulator mainPhasesPerPatternStats = new StatsAccumulator();
  private final StatsAccumulator mainPhaseTotalStats = new StatsAccumulator();

  /**
   Instantiate a new digest with a collection of target entities

   @param ingest to digest
   */
  @Inject
  public DigestPatternStyleImpl(
    @Assisted("ingest") Ingest ingest
  ) {
    super(ingest, DigestType.DigestPatternStyle);
    try {
      digest();
    } catch (Exception e) {
      log.error("Failed to digest pattern style of ingest {}", ingest, e);
    }
  }

  /**
   Digest entities from ingest
   */
  private void digest() throws Exception {
    for (Pattern pattern : ingest.patterns(PatternType.Main)) {
      Collection<Phase> phases = ingest.phases(pattern.getId());
      mainPhasesPerPatternStats.add(phases.size());
      mainPhasesPerPatternHistogram.add(phases.size());
      for (Phase phase : phases) {
        Integer total = phase.getTotal();
        mainPhaseTotalStats.add(total);
        mainPhaseTotalHistogram.add(total);

        int cursor = 0;
        List<PhaseChord> chords = Lists.newArrayList(ingest.phaseChords(phase.getId()));
        chords.sort(Chord.byPositionAscending);
        for (Chord chord : chords)
          if (chord.getPosition() > cursor) {
            mainChordSpacingHistogram.add(chord.getPosition() - cursor);
            cursor = chord.getPosition();
          }
      }
    }
    log.debug("Digested style of {} main-type patterns containing {} phases.", mainPhasesPerPatternStats.count(), mainPhaseTotalStats.count());
  }

  /**
   Represent a stats accumulator as JSON

   @param stats     to represent
   @param histogram to represent within stats
   @return json object
   */
  private JSONObject toJSONObject(StatsAccumulator stats, Multiset<Integer> histogram) {
    JSONObject result = new JSONObject();
    result.put(KEY_STAT_MIN, stats.min());
    result.put(KEY_STAT_MAX, stats.max());
    result.put(KEY_STAT_COUNT, stats.count());
    result.put(KEY_STAT_MEAN, stats.mean());
    result.put(KEY_HISTOGRAM, toJSONArray(histogram));
    return result;
  }

  /**
   Represent a phase total count as a json array

   @return json array
   */
  private JSONArray toJSONArray(Multiset<Integer> histogram) {
    JSONArray result = new JSONArray();
    histogram.elementSet().forEach((total) -> {
      JSONObject obj = new JSONObject();
      obj.put(KEY_STAT_VALUE, total);
      obj.put(KEY_STAT_COUNT, histogram.count(total));
      result.put(obj);
    });
    return result;
  }

  @Override
  public JSONObject toJSONObject() {
    JSONObject result = new JSONObject();
    JSONObject patternStyle = new JSONObject();
    patternStyle.put(KEY_MAIN_PHASES_PER_PATTERN, toJSONObject(mainPhasesPerPatternStats, mainPhasesPerPatternHistogram));
    patternStyle.put(KEY_MAIN_PHASE_TOTAL, toJSONObject(mainPhaseTotalStats, mainPhaseTotalHistogram));
    patternStyle.put(KEY_MAIN_CHORD_SPACING_HISTOGRAM, toJSONArray(mainChordSpacingHistogram));
    result.put(KEY_PATTERN_STYLE, patternStyle);
    return result;
  }

  @Override
  public Multiset<Integer> getMainPhasesPerPatternHistogram() {
    return mainPhasesPerPatternHistogram;
  }

  @Override
  public StatsAccumulator getMainPhasesPerPatternStats() {
    return mainPhasesPerPatternStats;
  }

  @Override
  public StatsAccumulator getMainPhaseTotalStats() {
    return mainPhaseTotalStats;
  }

  @Override
  public Multiset<Integer> getMainPhaseTotalHistogram() {
    return mainPhaseTotalHistogram;
  }

  @Override
  public Multiset<Integer> getMainChordSpacingHistogram() {
    return mainChordSpacingHistogram;
  }
}
