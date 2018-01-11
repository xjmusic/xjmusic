// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.analysis;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.Entity;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.phase.Phase;

import com.google.common.collect.Maps;

import org.json.JSONObject;

import java.math.BigInteger;
import java.util.Map;

/**
 [#154234716] Architect wants analysis of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 */
public abstract class Analysis extends Entity {
  public static final String KEY_ONE = "analysis";
  public static final String KEY_MANY = "analyses";
  protected final Map<BigInteger, Pattern> patterns = Maps.newConcurrentMap();
  protected final Map<BigInteger, Instrument> instruments = Maps.newConcurrentMap();
  protected final Map<BigInteger, Audio> audios = Maps.newConcurrentMap();
  protected final Map<BigInteger, Phase> phases = Maps.newConcurrentMap();
  private BigInteger targetId;
  private AnalysisState state;
  private AnalysisType type;

  /**
   Cache a pattern, for reporting later based on its id alone

   @param pattern to add
   */
  public void putPattern(Pattern pattern) {
    patterns.put(pattern.getId(), pattern);
  }

  /**
   Cache a phase, for reporting later based on its id alone

   @param phase to add
   */
  public void putPhase(Phase phase) {
    phases.put(phase.getId(), phase);
  }

  /**
   Cache a instrument, for reporting later based on its id alone

   @param instrument to add
   */
  public void putInstrument(Instrument instrument) {
    instruments.put(instrument.getId(), instrument);
  }

  /**
   Cache a audio, for reporting later based on its id alone

   @param audio to add
   */
  public void putAudio(Audio audio) {
    audios.put(audio.getId(), audio);
  }

  /**
   Retrieved a cached audio

   @param id of audio
   @return audio
   */
  public Audio getAudio(BigInteger id) {
    return audios.get(id);
  }

  /**
   Retrieved a cached pattern

   @param id of pattern
   @return pattern
   */
  public Pattern getPattern(BigInteger id) {
    return patterns.get(id);
  }

  /**
   Retrieved a cached phase

   @param id of phase
   @return phase
   */
  public Phase getPhase(BigInteger id) {
    return phases.get(id);
  }

  /**
   Retrieved a cached instrument

   @param id of instrument
   @return instrument
   */
  public Instrument getInstrument(BigInteger id) {
    return instruments.get(id);
  }


  /**
   Set target id

   @param targetId to set
   */
  public void setTargetId(BigInteger targetId) {
    this.targetId = targetId;
  }

  /**
   Get state of analysis

   @return state of analysis
   */
  public AnalysisState getState() {
    return state;
  }

  /**
   Set state of analysis

   @param state to set
   @return analysis, for method chaining
   */
  public Analysis setState(AnalysisState state) {
    this.state = state;
    return this;
  }

  /**
   Get type of analysis

   @return type
   */
  public AnalysisType getType() {
    return type;
  }

  /**
   Set type of analysis

   @param type to set
   @return analysis, for method chaining
   */
  public Analysis setType(AnalysisType type) {
    this.type = type;
    return this;
  }

  @Override
  public void validate() throws BusinessException {
    if (null == targetId) {
      throw new BusinessException("Target ID is required.");
    }
    if (null == type || type.toString().isEmpty()) {
      throw new BusinessException("Type is required.");
    }
  }

  @Override
  public String toString() {
    return String.format("%s %s #%s", state, type, targetId);
  }

  /**
   An Analysis must override this method, for reporting

   @return JSON object of analysis report, probably for display in UI
   */
  public abstract JSONObject toJSONObject();

  /**
   Reduce complexity before reporting
   */
  public abstract void prune();

}
