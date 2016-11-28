// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.choice;

import io.outright.xj.core.model.arrangement.Arrangement;
import io.outright.xj.core.model.idea.Idea;

public class ChoiceImpl implements Choice {
  private Type type;
  private Idea idea;
  private int phaseOffset;
  private int transpose;
  private Arrangement arrangement;

  public ChoiceImpl(
    Type _type,
    Idea _idea,
    Arrangement _arrangement,
    int _phaseOffset,
    int _transpose
  ) {
    type = _type;
    idea = _idea;
    arrangement = _arrangement;
    phaseOffset = _phaseOffset;
    transpose = _transpose;
  }

  @Override
  public Type Type() {
    return type;
  }

  @Override
  public Idea Idea() {
    return idea;
  }

  @Override
  public Arrangement Arrangement() {
    return arrangement;
  }

  @Override
  public int PhaseOffset() {
    return phaseOffset;
  }

  @Override
  public int Transpose() {
    return transpose;
  }
}
