// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.library;

import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.model.instrument.Instrument;

public interface Library {
  /**
   * Library has many Ideas
   *
   * @return Idea[]
   */
  Idea[] Ideas();

  /**
   * Library has many Instruments
   *
   * @return Instrument[]
   */
  Instrument[] Instruments();
}
