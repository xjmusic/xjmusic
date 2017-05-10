// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.idea;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class IdeaWrapper extends EntityWrapper {

  // Idea
  private Idea idea;

  public Idea getIdea() {
    return idea;
  }

  public IdeaWrapper setIdea(Idea idea) {
    this.idea = idea;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  public Idea validate() throws BusinessException {
    if (this.idea == null) {
      throw new BusinessException("Idea is required.");
    }
    this.idea.validate();
    return this.idea;
  }

}
