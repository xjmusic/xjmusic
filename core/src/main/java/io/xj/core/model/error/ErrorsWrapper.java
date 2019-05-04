//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.error;

import com.google.common.collect.Lists;

import java.util.Collection;

public class ErrorsWrapper {
  private final Collection<Error> errors = Lists.newArrayList();

  /**
   Get all errors

   @return errors
   */
  public Collection<Error> getErrors() {
    return errors;
  }

  /**
   Set all errors

   @param errors to set
   */
  public void setErrors(Collection<Error> errors) {
    this.errors.clear();
    this.errors.addAll(errors);
  }

  /**
   Add an error

   @param error to set
   @return this wrapper (for chaining adds)
   */
  public ErrorsWrapper add(Error error) {
    errors.add(error);
    return this;
  }

}
