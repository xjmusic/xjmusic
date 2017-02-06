// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.instrument;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

public class InstrumentWrapper extends EntityWrapper {

  // Instrument
  private Instrument instrument;
  public Instrument getInstrument() {
    return instrument;
  }
  public InstrumentWrapper setInstrument(Instrument instrument) {
    this.instrument = instrument;
    return this;
  }

  /**
   * Validate data.
   * @throws BusinessException if invalid.
   */
  public void validate() throws BusinessException{
    if (this.instrument == null) {
      throw new BusinessException("Instrument is required.");
    }
    this.instrument.validate();
  }

  @Override
  public String toString() {
    return "{" +
      Instrument.KEY_ONE + ":" + this.instrument +
      "}";
  }
}
