// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.craft.macro_main;

import io.xj.service.nexus.NexusException;

/**
 [#138] Foundation craft for Initial Segment of a Chain
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
@FunctionalInterface
public interface MacroMainCraft {

  /**
   perform macro craft for the current segment
   */
  void doWork() throws NexusException;

}
