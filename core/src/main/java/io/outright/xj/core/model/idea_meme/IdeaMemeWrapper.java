// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.idea_meme;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class IdeaMemeWrapper extends EntityWrapper {

  // Idea
  private IdeaMeme ideaMeme;

  public IdeaMeme getIdeaMeme() {
    return ideaMeme;
  }

  public IdeaMemeWrapper setIdeaMeme(IdeaMeme ideaMeme) {
    this.ideaMeme = ideaMeme;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  public IdeaMeme validate() throws BusinessException {
    if (this.ideaMeme == null) {
      throw new BusinessException("Idea is required.");
    }
    this.ideaMeme.validate();
    return this.ideaMeme;
  }

}
