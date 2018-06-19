// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.macro;

/**
 [#138] Foundation craft for Initial Segment of a Chain
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
@FunctionalInterface
public interface MacroMainCraft {

  /**
   perform macro craft for the current segment
   */
  void doWork() throws Exception;

}
