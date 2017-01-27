// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.library;

import io.outright.xj.core.app.exception.BusinessException;

import org.jooq.Record;

public class LibraryWrapper {

  // Library
  private Library library;
  public Library getLibrary() {
    return library;
  }
  public void setLibrary(Library library) {
    this.library = library;
  }

  /**
   * Validate data.
   * @throws BusinessException if invalid.
   */
  public void validate() throws BusinessException{
    if (this.library == null) {
      throw new BusinessException("Library is required.");
    }
    this.library.validate();
  }

  @Override
  public String toString() {
    return "{" +
      Library.KEY_ONE + ":" + this.library +
      "}";
  }

  public Record intoRecord() {
    return null;
  }
}
