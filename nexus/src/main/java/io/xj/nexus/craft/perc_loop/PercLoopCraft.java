// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.perc_loop;

import io.xj.nexus.NexusException;

/**
 * Structure craft for the current segment percLoop
 * [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
public interface PercLoopCraft {

  /**
   * perform craft for the current segment
   * Artist wants Pattern to have type Macro or Main (for Macro- or Main-type sequences), or Intro, Loop, or Outro (for Percussion-type Loop-mode or Detail-type Sequence) in order to of a composition that is dynamic when chosen to fill a Segment. https://www.pivotaltracker.com/story/show/153976073
   * Artist wants dynamic randomness over the selection of various audio events to fulfill particular pattern events, in order to establish repetition within any given segment. https://www.pivotaltracker.com/story/show/161466708
   */
  void doWork() throws NexusException;

}
