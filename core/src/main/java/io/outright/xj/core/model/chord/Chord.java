// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.chord;

import io.outright.xj.core.primitive.beat.Beat;
import io.outright.xj.core.primitive.name.Name;

public interface Chord {
  /**
   * Chord Name
   *
   * @return String
   */
  Name Name();

  /**
   * Chord Position (beats)
   *
   * @return float
   */
  Beat Position();
}
