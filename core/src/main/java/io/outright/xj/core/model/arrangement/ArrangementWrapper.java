// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.arrangement;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

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
   * Validate data.
   * @throws BusinessException if invalid.
   */
  @Override
  public Arrangement validate() throws BusinessException{
    if (this.arrangement == null) {
      throw new BusinessException("Arrangement is required.");
    }
    this.arrangement.validate();
    return this.arrangement;
  }

  @Override
  public String toString() {
    return "{" +
      Arrangement.KEY_ONE + ":" + this.arrangement +
      "}";
  }
}
