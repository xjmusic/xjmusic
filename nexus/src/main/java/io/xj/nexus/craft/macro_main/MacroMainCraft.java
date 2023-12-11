// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.craft.macro_main;

import io.xj.hub.tables.pojos.Program;
import io.xj.nexus.NexusException;
import jakarta.annotation.Nullable;

/**
 [#138] Foundation craft for Initial Segment of a Chain
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
@FunctionalInterface
public interface MacroMainCraft {

  /**
   perform macro craft for the current segment
   */
  void doWork(@Nullable Program forceMacroProgram) throws NexusException;

}
