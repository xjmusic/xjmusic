// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.library;

import io.xj.core.app.exception.BusinessException;
import io.xj.core.model.EntityWrapper;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class LibraryWrapper extends EntityWrapper {

  // Library
  private Library library;

  public Library getLibrary() {
    return library;
  }

  public LibraryWrapper setLibrary(Library library) {
    this.library = library;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  public Library validate() throws BusinessException {
    if (this.library == null) {
      throw new BusinessException("Library is required.");
    }
    this.library.validate();
    return this.library;
  }

}
