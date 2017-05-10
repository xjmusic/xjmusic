// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.pick;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class PickWrapper extends EntityWrapper {

  // Pick
  private Pick pick;

  public Pick getPick() {
    return pick;
  }

  public PickWrapper setPick(Pick pick) {
    this.pick = pick;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  @Override
  public Pick validate() throws BusinessException {
    if (this.pick == null) {
      throw new BusinessException("Pick is required.");
    }
    this.pick.validate();
    return this.pick;
  }

}
