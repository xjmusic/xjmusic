// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.library

class LibraryImplTest extends GroovyTestCase {
  Library library

  void setUp() {
    super.setUp()
    library = new LibraryImpl()
  }

  void tearDown() {
    library = null
  }

  void testIdeas() {
    assert library.Ideas().length == 0
  }

  void testInstruments() {
    assert library.Instruments().length == 0
  }
}
