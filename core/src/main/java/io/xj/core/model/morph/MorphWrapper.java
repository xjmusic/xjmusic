// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.morph;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.EntityWrapper;

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
