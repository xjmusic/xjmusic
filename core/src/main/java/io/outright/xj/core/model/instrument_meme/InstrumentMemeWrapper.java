// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.instrument_meme;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

public class InstrumentMemeWrapper extends EntityWrapper {

  // Instrument
  private InstrumentMeme instrumentMeme;

  public InstrumentMeme getInstrumentMeme() {
    return instrumentMeme;
  }

  public InstrumentMemeWrapper setInstrumentMeme(InstrumentMeme instrumentMeme) {
    this.instrumentMeme = instrumentMeme;
    return this;
  }

  /**
   * Validate data.
   *
   * @throws BusinessException if invalid.
   */
  public InstrumentMeme validate() throws BusinessException {
    if (this.instrumentMeme == null) {
      throw new BusinessException("Instrument is required.");
    }
    this.instrumentMeme.validate();
    return this.instrumentMeme;
  }

  @Override
  public String toString() {
    return "{" +
      InstrumentMeme.KEY_ONE + ":" + this.instrumentMeme +
      "}";
  }
}
