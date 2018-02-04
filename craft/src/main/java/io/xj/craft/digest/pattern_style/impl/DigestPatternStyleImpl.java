// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.craft.digest.pattern_style.impl;

import com.google.common.collect.Maps;
import com.google.common.math.StatsAccumulator;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.phase.Phase;
import io.xj.craft.digest.DigestType;
import io.xj.craft.digest.impl.DigestImpl;
import io.xj.craft.digest.pattern_style.DigestPatternStyle;
import io.xj.craft.ingest.Ingest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;

/**
 In-memory cache of ingest of all memes in a library
 <p>
 [#154234716] Architect wants ingest of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 */
public class DigestPatternStyleImpl extends DigestImpl implements DigestPatternStyle {
  private final Logger log = LoggerFactory.getLogger(DigestPatternStyleImpl.class);
  private final StatsAccumulator mainPhasesPerPattern = new StatsAccumulator();
  private final StatsAccumulator mainPhaseTotal = new StatsAccumulator();
  private final Map<Integer, Integer> mainPhaseTotalCount = Maps.newConcurrentMap();

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
      mainPhasesPerPattern.add(phases.size());
      for (Phase phase : phases) {
        Integer total = phase.getTotal();
        mainPhaseTotal.add(total);
        mainPhaseTotalCount.put(total,
          mainPhaseTotalCount.containsKey(total) ?
            mainPhaseTotalCount.get(total) + 1 :
            0);
      }
    }
    log.debug("Digested style of {} main-type patterns containing {} phases.", mainPhasesPerPattern.count(), mainPhaseTotal.count());
  }

  /**
   Represent a stats accumulator as JSON

   @param stats to represent
   @return json object
   */
  private JSONObject toJSONObject(StatsAccumulator stats) {
    JSONObject result = new JSONObject();
    result.put(KEY_STAT_MIN, stats.min());
    result.put(KEY_STAT_MAX, stats.max());
    result.put(KEY_STAT_COUNT, stats.count());
    result.put(KEY_STAT_MEAN, stats.mean());
    return result;
  }

  private JSONArray phaseTotalCountJSONArray() {
    JSONArray result = new JSONArray();
    mainPhaseTotalCount.forEach((total, count) -> {
      JSONObject obj = new JSONObject();
      obj.put(KEY_PHASE_TOTAL, total);
      obj.put(KEY_STAT_COUNT, count);
      result.put(obj);
    });
    return result;
  }

  @Override
  public JSONObject toJSONObject() {
    JSONObject result = new JSONObject();
    JSONObject patternStyle = new JSONObject();
    patternStyle.put(KEY_MAIN_PHASES_PER_PATTERN, toJSONObject(mainPhasesPerPattern));
    patternStyle.put(KEY_MAIN_PHASE_TOTAL, toJSONObject(mainPhaseTotal));
    patternStyle.put(KEY_MAIN_PHASE_TOTAL_COUNT, phaseTotalCountJSONArray());
    result.put(KEY_PATTERN_STYLE, patternStyle);
    return result;
  }

  @Override
  public StatsAccumulator getMainPhasesPerPattern() {
    return mainPhasesPerPattern;
  }

  @Override
  public StatsAccumulator getMainPhaseTotal() {
    return mainPhaseTotal;
  }

  @Override
  public Map<Integer, Integer> getMainPhaseTotalCount() {
    return mainPhaseTotalCount;
  }
}
