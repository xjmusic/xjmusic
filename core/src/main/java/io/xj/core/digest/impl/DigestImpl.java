// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.digest.impl;

import io.xj.core.digest.Digest;
import io.xj.core.digest.DigestType;
import io.xj.core.evaluation.Evaluation;
import io.xj.core.evaluation.EvaluationState;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.phase.Phase;

import java.math.BigInteger;

/**
 [#154234716] Architect wants evaluation of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 [#154350346] Architect wants a universal Evaluation Provider, to modularize graph mathematics used during craft to evaluate any combination of Library, Pattern, and Instrument for any purpose.
 */
public abstract class DigestImpl implements Digest {
  protected final Evaluation evaluation;
  protected EvaluationState state;
  protected DigestType type;

  /**
   Instantiate a new digest with a collection of target entities
   */
  protected DigestImpl(Evaluation evaluation, DigestType type) {
    this.evaluation = evaluation;
    this.type = type;
  }

  /**
   Retrieved a cached audio

   @param id of audio
   @return audio
   */
  public Audio getAudio(BigInteger id) {
    return evaluation.audioMap().get(id);
  }

  /**
   Retrieved a cached pattern

   @param id of pattern
   @return pattern
   */
  public Pattern getPattern(BigInteger id) {
    return evaluation.patternMap().get(id);
  }

  /**
   Retrieved a cached phase

   @param id of phase
   @return phase
   */
  public Phase getPhase(BigInteger id) {
    return evaluation.phaseMap().get(id);
  }

  /**
   Retrieved a cached instrument

   @param id of instrument
   @return instrument
   */
  public Instrument getInstrument(BigInteger id) {
    return evaluation.instrumentMap().get(id);
  }


  /**
   Get state of evaluation

   @return state of evaluation
   */
  public EvaluationState getState() {
    return state;
  }

  /**
   Set state of evaluation

   @param state to set
   @return evaluation, for method chaining
   */
  public DigestImpl setState(EvaluationState state) {
    this.state = state;
    return this;
  }

  /**
   Get type of evaluation

   @return type
   */
  public DigestType getType() {
    return type;
  }


}
