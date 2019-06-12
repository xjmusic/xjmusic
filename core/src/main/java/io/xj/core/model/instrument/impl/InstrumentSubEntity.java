//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.instrument.impl;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.impl.SubEntityImpl;
import io.xj.core.model.instrument.Instrument;

import java.math.BigInteger;

/**
 [#166708597] Instrument model handles all of its own entities
 <p>
 POJO for persisting data in memory AS json
 <p>
 NOTE: There can only be ONE of any getter/setter (with the same # of input params)
 */
public abstract class InstrumentSubEntity extends SubEntityImpl {
  private BigInteger instrumentId;

  /**
   Get id of Instrument to which this entity belongs

   @return instrument id
   */
  public BigInteger getInstrumentId() {
    return instrumentId;
  }

  @Override
  public BigInteger getParentId() {
    return instrumentId;
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return ImmutableList.<Class>builder()
      .addAll(super.getResourceBelongsTo())
      .add(Instrument.class)
      .build();
  }

  /**
   Set id of Instrument to which this entity belongs

   @param instrumentId to which this entity belongs
   @return this Instrument Entity (for chaining setters)
   */
  public InstrumentSubEntity setInstrumentId(BigInteger instrumentId) {
    this.instrumentId = instrumentId;
    return this;
  }

  @Override
  public InstrumentSubEntity validate() throws CoreException {
    require(instrumentId, "Instrument ID");
    return this;
  }
}
