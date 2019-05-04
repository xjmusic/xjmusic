// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.instrument;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class InstrumentWrapper {
  private Instrument instrument;

  public Instrument getInstrument() {
    return instrument;
  }

  public InstrumentWrapper setInstrument(Instrument instrument) {
    this.instrument = instrument;
    return this;
  }
}
