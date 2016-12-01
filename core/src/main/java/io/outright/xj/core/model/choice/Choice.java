// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.choice;

import io.outright.xj.core.primitive.offset.Offset;
import io.outright.xj.core.primitive.transpose.Transpose;

import io.outright.xj.core.model.arrangement.Arrangement;
import io.outright.xj.core.model.idea.Idea;

public interface Choice {
  /**
   * Choice Type
   *
   * @return Type
   */
  Type Type();

  /**
   * Choice Idea
   *
   * @return Idea
   */
  Idea Idea();

  /**
   * Choice Phase Offset
   *
   * @return int
   */
  Offset PhaseOffset();

  /**
   * Choice Transposition (relative from instrument)
   *
   * @return int
   */
  Transpose Transpose();

  /**
   * Choice Arrangement
   *
   * @return Arrangement
   */
  Arrangement Arrangement();
}
