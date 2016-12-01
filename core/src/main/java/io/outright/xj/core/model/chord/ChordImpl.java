// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.chord;

import io.outright.xj.core.primitive.beat.Beat;
import io.outright.xj.core.primitive.name.Name;

public class ChordImpl implements Chord {
  private Name name;
  private Beat position;

  public ChordImpl(
    Name name,
    Beat position
  ) {
    this.name = name;
    this.position = position;
  }

  @Override
  public Name Name() {
    return name;
  }

  @Override
  public Beat Position() {
    return position;
  }
}
