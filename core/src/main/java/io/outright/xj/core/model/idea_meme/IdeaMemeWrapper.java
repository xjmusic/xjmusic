// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.idea_meme;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

import org.jooq.Record;

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
   * Validate data.
   *
   * @throws BusinessException if invalid.
   */
  public void validate() throws BusinessException {
    if (this.ideaMeme == null) {
      throw new BusinessException("Idea is required.");
    }
    this.ideaMeme.validate();
  }

  @Override
  public String toString() {
    return "{" +
      IdeaMeme.KEY_ONE + ":" + this.ideaMeme +
      "}";
  }

  public Record intoRecord() {
    return null;
  }
}
