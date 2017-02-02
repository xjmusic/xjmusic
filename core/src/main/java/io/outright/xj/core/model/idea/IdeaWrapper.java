// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.idea;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

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
   * Validate data.
   * @throws BusinessException if invalid.
   */
  public void validate() throws BusinessException{
    if (this.idea == null) {
      throw new BusinessException("Idea is required.");
    }
    this.idea.validate();
  }

  @Override
  public String toString() {
    return "{" +
      Idea.KEY_ONE + ":" + this.idea +
      "}";
  }
}
