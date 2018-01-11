// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.analysis.library_meme;

import io.xj.core.model.analysis.Analysis;

import com.google.common.collect.Maps;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;

/**
 In-memory cache of analysis of all memes in a library
 <p>
 [#154234716] Architect wants analysis of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 */
public class LibraryMemeAnalysis extends Analysis {
  private static final String KEY_INSTRUMENT_DESCRIPTION = "instrumentDescription";
  private static final String KEY_INSTRUMENT_ID = "instrumentId";
  private static final String KEY_INSTRUMENT_TYPE = "instrumentType";
  private static final String KEY_INSTRUMENTS = "instrumentsWithMeme";
  private static final String KEY_MEME_USAGE = "memeUsage";
  private static final String KEY_PATTERN_HAS_MEME = "patternHasMeme";
  private static final String KEY_PATTERN_ID = "patternId";
  private static final String KEY_PATTERN_NAME = "patternName";
  private static final String KEY_PATTERN_TYPE = "patternType";
  private static final String KEY_PATTERNS = "patternsWithMeme";
  private static final String KEY_PHASE_ID = "phaseId";
  private static final String KEY_PHASE_NAME = "phaseName";
  private static final String KEY_PHASE_OFFSET = "phaseOffset";
  private static final String KEY_PHASE_TYPE = "phaseType";
  private static final String KEY_PHASES_WITH_MEME = "phasesWithMeme";
  protected final Map<String, AnalyzedLibraryMeme> memes = Maps.newConcurrentMap();

  /**
   Get all analyzed memes

   @return map of meme name to analyzed meme
   */
  public Map<String, AnalyzedLibraryMeme> getMemes() {
    return Collections.unmodifiableMap(memes);
  }

  /**
   Add a pattern id, if it isn't already in the list

   @param patternId of pattern to add
   */
  public void addPatternId(String name, BigInteger patternId) {
    ensureMeme(name);
    memes.get(name).addPatternId(patternId);
  }

  /**
   Add a pattern id, if it isn't already in the list

   @param patternId parent of phase
   @param phaseId   of phase to add
   */
  public void addPatternPhaseId(String name, BigInteger patternId, BigInteger phaseId) {
    ensureMeme(name);
    memes.get(name).addPatternPhaseId(patternId, phaseId);
  }

  /**
   Add a instrument id, if it isn't already in the list

   @param instrumentId of instrument to add
   */
  public void addInstrumentId(String name, BigInteger instrumentId) {
    ensureMeme(name);
    memes.get(name).addInstrumentId(instrumentId);
  }

  /**
   Ensure that an analysis cache exists for a particular meme

   @param name of meme to ensure
   */
  private void ensureMeme(String name) {
    if (!memes.containsKey(name))
      memes.put(name, new AnalyzedLibraryMeme(name));
  }

  @Override
  public JSONObject toJSONObject() {
    JSONObject result = new JSONObject();
    JSONObject memeUsage = new JSONObject();
    memes.forEach((memeName, analyzedLibraryMeme) -> {
      JSONObject memeObj = new JSONObject();

      JSONArray memeInstrumentsArr = new JSONArray();
      analyzedLibraryMeme.getInstrumentIds().forEach(instrumentId -> {
        JSONObject instrumentObj = new JSONObject();
        instrumentObj.put(KEY_INSTRUMENT_ID, instrumentId);
        instrumentObj.put(KEY_INSTRUMENT_TYPE, getInstrument(instrumentId).getType());
        instrumentObj.put(KEY_INSTRUMENT_DESCRIPTION, getInstrument(instrumentId).getDescription());
        memeInstrumentsArr.put(instrumentObj);
      });
      memeObj.put(KEY_INSTRUMENTS, memeInstrumentsArr);

      JSONArray memePatternsArr = new JSONArray();
      analyzedLibraryMeme.getPhasePatternIds().forEach(patternId -> {
        JSONObject patternObj = new JSONObject();
        JSONArray patternPhasesArr = new JSONArray();
        analyzedLibraryMeme.getPhaseIds(patternId).forEach(phaseId -> {
          JSONObject phaseObj = new JSONObject();
          phaseObj.put(KEY_PHASE_ID, phaseId);
          phaseObj.put(KEY_PHASE_NAME, getPhase(phaseId).getName());
          phaseObj.put(KEY_PHASE_TYPE, getPhase(phaseId).getType());
          phaseObj.put(KEY_PHASE_OFFSET, getPhase(phaseId).getOffset());
          patternPhasesArr.put(phaseObj);
        });
        patternObj.put(KEY_PATTERN_ID, patternId);
        patternObj.put(KEY_PATTERN_TYPE, getPattern(patternId).getType());
        patternObj.put(KEY_PATTERN_NAME, getPattern(patternId).getName());
        patternObj.put(KEY_PATTERN_HAS_MEME, analyzedLibraryMeme.getPatternIds().contains(patternId));
        patternObj.put(KEY_PHASES_WITH_MEME, patternPhasesArr);
        memePatternsArr.put(patternObj);
      });
      analyzedLibraryMeme.getPatternIds().forEach(patternId -> {
        if (analyzedLibraryMeme.getPhasePatternIds().contains(patternId)) return;
        JSONObject patternObj = new JSONObject();
        patternObj.put(KEY_PATTERN_ID, patternId);
        patternObj.put(KEY_PATTERN_TYPE, getPattern(patternId).getType());
        patternObj.put(KEY_PATTERN_NAME, getPattern(patternId).getName());
        patternObj.put(KEY_PATTERN_HAS_MEME, true);
        memePatternsArr.put(patternObj);
      });
      memeObj.put(KEY_PATTERNS, memePatternsArr);

      memeUsage.put(memeName, memeObj);
    });

    result.put(KEY_MEME_USAGE, memeUsage);
    return result;
  }

  /**
   Reduce complexity before reporting
   */
  @Override
  public void prune() {
    // noop
  }


}
