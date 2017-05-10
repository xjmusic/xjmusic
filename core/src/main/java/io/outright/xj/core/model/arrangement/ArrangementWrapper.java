// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.arrangement;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class ArrangementWrapper extends EntityWrapper {

  // Arrangement
  private Arrangement arrangement;

  public Arrangement getArrangement() {
    return arrangement;
  }

  public ArrangementWrapper setArrangement(Arrangement arrangement) {
    this.arrangement = arrangement;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  @Override
  public Arrangement validate() throws BusinessException {
    if (this.arrangement == null) {
      throw new BusinessException("Arrangement is required.");
    }
    this.arrangement.validate();
    return this.arrangement;
  }

}
