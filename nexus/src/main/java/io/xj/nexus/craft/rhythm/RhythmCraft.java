// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.rhythm;

import io.xj.nexus.NexusException;

/**
 Structure craft for the current segment rhythm
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
public interface RhythmCraft {

  /**
   perform craft for the current segment
   [#153976073] Artist wants Pattern to have type Macro or Main (for Macro- or Main-type sequences), or Intro, Loop, or Outro (for Rhythm or Detail-type Sequence) in order to of a composition that is dynamic when chosen to fill a Segment.
   [#161466708] Artist wants dynamic randomness over the selection of various audio events to fulfill particular pattern events, in order to establish repetition within any given segment.
   */
  void doWork() throws NexusException;

}
