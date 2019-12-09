// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.harmonic;

import io.xj.core.exception.CoreException;
import io.xj.craft.exception.CraftException;

/**
 Structure craft for the current segment includes rhythm and harmonicDetail
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
@FunctionalInterface
public interface HarmonicDetailCraft {

  /**
   perform craft for the current segment
   */
  void doWork() throws CoreException, CraftException;

}
