// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.morph;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

public class MorphWrapper extends EntityWrapper {

  // Morph
  private Morph morph;
  public Morph getMorph() {
    return morph;
  }
  public MorphWrapper setMorph(Morph morph) {
    this.morph = morph;
    return this;
  }

  /**
   * Validate data.
   * @throws BusinessException if invalid.
   */
  @Override
  public Morph validate() throws BusinessException{
    if (this.morph == null) {
      throw new BusinessException("Morph is required.");
    }
    this.morph.validate();
    return this.morph;
  }

}
