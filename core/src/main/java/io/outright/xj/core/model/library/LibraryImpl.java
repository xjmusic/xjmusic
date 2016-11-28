// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.library;

import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.model.instrument.Instrument;

public class LibraryImpl implements Library {
  private Idea[] ideas = new Idea[0];
  private Instrument[] instruments = new Instrument[0];

  public LibraryImpl() {}

  public Idea[] Ideas() {
    return ideas;
  }

  public Instrument[] Instruments() {
    return instruments;
  }

}
