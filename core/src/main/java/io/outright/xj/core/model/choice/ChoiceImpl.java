// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.choice;

import io.outright.xj.core.model.arrangement.Arrangement;
import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.primitive.offset.Offset;
import io.outright.xj.core.primitive.transpose.Transpose;

public class ChoiceImpl implements Choice {
  private Type type;
  private Idea idea;
  private Offset phaseOffset;
  private Transpose transpose;
  private Arrangement arrangement;

  public ChoiceImpl(
    Type type,
    Idea idea,
    Arrangement arrangement,
    Offset phaseOffset,
    Transpose transpose
  ) {
    this.type = type;
    this.idea = idea;
    this.arrangement = arrangement;
    this.phaseOffset = phaseOffset;
    this.transpose = transpose;
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
  public Offset PhaseOffset() {
    return phaseOffset;
  }

  @Override
  public Transpose Transpose() {
    return transpose;
  }
}
