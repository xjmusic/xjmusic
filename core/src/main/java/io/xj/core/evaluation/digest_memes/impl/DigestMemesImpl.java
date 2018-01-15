// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.evaluation.digest_memes.impl;

import io.xj.core.evaluation.DigestType;
import io.xj.core.evaluation.Evaluation;
import io.xj.core.evaluation.digest_memes.DigestMemes;
import io.xj.core.evaluation.impl.DigestImpl;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.meme.Meme;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.phase.Phase;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 In-memory cache of evaluation of all memes in a library
 <p>
 [#154234716] Architect wants evaluation of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 */
public class DigestMemesImpl extends DigestImpl implements DigestMemes {
  protected final Map<String, DigestMemesItem> memes = Maps.newConcurrentMap();
  private final Logger log = LoggerFactory.getLogger(DigestMemesImpl.class);

  /**
   Instantiate a new digest with a collection of target entities

   @param evaluation to digest
   */
  @Inject
  public DigestMemesImpl(
    @Assisted("evaluation") Evaluation evaluation
  ) {
    super(evaluation, DigestType.DigestMemes);
    try {
      digest();
    } catch (Exception e) {
      log.error("Failed to digest memes of evaluation {}", evaluation, e);
    }
  }

  /**
   Digest entities from evaluation
   */
  private void digest() throws Exception {
    // in-memory caches of original objects
    Map<BigInteger, Collection<? extends Meme>> instrumentMemes = Maps.newConcurrentMap();
    Map<BigInteger, Collection<? extends Meme>> patternMemes = Maps.newConcurrentMap();
    Map<BigInteger, Map<BigInteger, Collection<? extends Meme>>> patternPhaseMemes = Maps.newConcurrentMap();

    // for each pattern, stash collection of pattern memes and prepare map of phases
    for (Pattern pattern : evaluation.getPatterns()) {
      patternMemes.put(pattern.getId(), evaluation.getPatternMemes());
      patternPhaseMemes.put(pattern.getId(), Maps.newConcurrentMap());

      // for each phase in pattern, stash collection of phase memes
      for (Phase phase : evaluation.getPhases()) {
        patternPhaseMemes.get(pattern.getId()).put(phase.getId(), evaluation.getPhaseMemes());
      }
    }

    // for each instrument, stash collection of instrument memes
    for (Instrument instrument : evaluation.getInstruments()) {
      instrumentMemes.put(instrument.getId(), evaluation.getInstrumentMemes());
    }

    // reverse-match everything and store it
    instrumentMemes.forEach((instrumentId, memesInInstrument) -> memesInInstrument.forEach(meme -> {
      addInstrumentId(meme.getName(), instrumentId);
    }));
    patternMemes.forEach((patternId, memesInPattern) -> memesInPattern.forEach(meme -> {
      addPatternId(meme.getName(), patternId);
    }));
    patternPhaseMemes.forEach((patternId, patternPhases) -> patternPhases.forEach((phaseId, phaseMemes) -> {
      phaseMemes.forEach(meme -> addPatternPhaseId(meme.getName(), patternId, phaseId));
    }));
  }

  /**
   Get all evaluated memes

   @return map of meme name to evaluated meme
   */
  public Map<String, DigestMemesItem> getMemes() {
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
   Ensure that an evaluation cache exists for a particular meme

   @param name of meme to ensure
   */
  private void ensureMeme(String name) {
    if (!memes.containsKey(name))
      memes.put(name, new DigestMemesItem(name));
  }

  @Override
  public JSONObject toJSONObject() {
    JSONObject result = new JSONObject();
    JSONObject memeUsage = new JSONObject();
    memes.forEach((memeName, memeDigestItem) -> {
      JSONObject memeObj = new JSONObject();

      JSONArray memeInstrumentsArr = new JSONArray();
      memeDigestItem.getInstrumentIds().forEach(instrumentId -> {
        JSONObject instrumentObj = new JSONObject();
        instrumentObj.put(KEY_INSTRUMENT_ID, instrumentId);
        instrumentObj.put(KEY_INSTRUMENT_TYPE, getInstrument(instrumentId).getType());
        instrumentObj.put(KEY_INSTRUMENT_DESCRIPTION, getInstrument(instrumentId).getDescription());
        memeInstrumentsArr.put(instrumentObj);
      });
      memeObj.put(KEY_INSTRUMENTS, memeInstrumentsArr);

      JSONArray memePatternsArr = new JSONArray();
      memeDigestItem.getPhasePatternIds().forEach(patternId -> {
        JSONObject patternObj = new JSONObject();
        JSONArray patternPhasesArr = new JSONArray();
        memeDigestItem.getPhaseIds(patternId).forEach(phaseId -> {
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
        patternObj.put(KEY_PATTERN_HAS_MEME, memeDigestItem.getPatternIds().contains(patternId));
        patternObj.put(KEY_PHASES_WITH_MEME, patternPhasesArr);
        memePatternsArr.put(patternObj);
      });
      memeDigestItem.getPatternIds().forEach(patternId -> {
        if (memeDigestItem.getPhasePatternIds().contains(patternId)) return;
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

}
