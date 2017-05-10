// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.phase_meme;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class PhaseMemeWrapper extends EntityWrapper {

  // Phase
  private PhaseMeme phaseMeme;

  public PhaseMeme getPhaseMeme() {
    return phaseMeme;
  }

  public PhaseMemeWrapper setPhaseMeme(PhaseMeme phaseMeme) {
    this.phaseMeme = phaseMeme;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  public PhaseMeme validate() throws BusinessException {
    if (this.phaseMeme == null) {
      throw new BusinessException("phaseMeme is required.");
    }
    this.phaseMeme.validate();
    return this.phaseMeme;
  }

}
