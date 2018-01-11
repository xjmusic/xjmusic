// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.instrument;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
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
   Validate data.

   @throws BusinessException if invalid.
   */
  public Instrument validate() throws BusinessException {
    if (this.instrument == null) {
      throw new BusinessException("Instrument is required.");
    }
    this.instrument.validate();
    return this.instrument;
  }

}
