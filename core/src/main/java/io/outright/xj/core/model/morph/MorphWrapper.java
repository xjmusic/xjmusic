// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.morph;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
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
   Validate data.

   @throws BusinessException if invalid.
   */
  @Override
  public Morph validate() throws BusinessException {
    if (this.morph == null) {
      throw new BusinessException("Morph is required.");
    }
    this.morph.validate();
    return this.morph;
  }

}
