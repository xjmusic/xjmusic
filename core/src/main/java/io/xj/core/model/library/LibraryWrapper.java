// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.library;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class LibraryWrapper {
  private Library library;

  public Library getLibrary() {
    return library;
  }

  public LibraryWrapper setLibrary(Library library) {
    this.library = library;
    return this;
  }
}
