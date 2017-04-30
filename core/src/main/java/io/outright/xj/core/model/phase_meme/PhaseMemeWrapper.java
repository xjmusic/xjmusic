// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.phase_meme;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

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
