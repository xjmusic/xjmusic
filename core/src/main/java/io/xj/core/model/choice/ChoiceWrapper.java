// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.choice;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class ChoiceWrapper extends EntityWrapper {

  // Choice
  private Choice choice;

  public Choice getChoice() {
    return choice;
  }

  public ChoiceWrapper setChoice(Choice choice) {
    this.choice = choice;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  @Override
  public Choice validate() throws BusinessException {
    if (this.choice == null) {
      throw new BusinessException("Choice is required.");
    }
    this.choice.validate();
    return this.choice;
  }

}
