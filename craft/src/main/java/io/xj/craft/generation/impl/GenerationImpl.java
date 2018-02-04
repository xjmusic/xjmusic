// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.generation.impl;

import io.xj.craft.generation.Generation;
import io.xj.craft.generation.GenerationType;
import io.xj.craft.ingest.Ingest;
import io.xj.craft.ingest.IngestState;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.phase.Phase;

import java.math.BigInteger;

/**
 [#154234716] Architect wants ingest of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 [#154350346] Architect wants a universal Ingest Provider, to modularize graph mathematics used during craft to evaluate any combination of Library, Pattern, and Instrument for any purpose.
 */
public abstract class GenerationImpl implements Generation {
  protected final Ingest ingest;
  protected IngestState state;
  protected GenerationType type;

  /**
   Instantiate a new generation with a collection of target entities
   */
  protected GenerationImpl(Ingest ingest, GenerationType type) {
    this.ingest = ingest;
    this.type = type;
  }

  /**
   Retrieved a cached audio

   @param id of audio
   @return audio
   */
  public Audio getAudio(BigInteger id) {
    return ingest.audioMap().get(id);
  }

  /**
   Retrieved a cached pattern

   @param id of pattern
   @return pattern
   */
  public Pattern getPattern(BigInteger id) {
    return ingest.patternMap().get(id);
  }

  /**
   Retrieved a cached phase

   @param id of phase
   @return phase
   */
  public Phase getPhase(BigInteger id) {
    return ingest.phaseMap().get(id);
  }

  /**
   Retrieved a cached instrument

   @param id of instrument
   @return instrument
   */
  public Instrument getInstrument(BigInteger id) {
    return ingest.instrumentMap().get(id);
  }


  /**
   Get state of ingest

   @return state of ingest
   */
  public IngestState getState() {
    return state;
  }

  /**
   Set state of ingest

   @param state to set
   @return ingest, for method chaining
   */
  public GenerationImpl setState(IngestState state) {
    this.state = state;
    return this;
  }

  /**
   Get type of ingest

   @return type
   */
  public GenerationType getType() {
    return type;
  }


}
