// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.craft.beat;

import io.xj.engine.FabricationException;

/**
 Structure craft for the current segment beat
 If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
public interface BeatCraft {

  /**
   perform craft for the current segment
   Artist wants Pattern to have type Macro or Main (for Macro- or Main-type sequences), or Intro, Loop, or Outro (for Beat or Detail-type Sequence) in order to of a composition that is dynamic when chosen to fill a Segment. https://github.com/xjmusic/workstation/issues/257
   Artist wants dynamic randomness over the selection of various audio events to fulfill particular pattern events, in order to establish repetition within any given segment. https://github.com/xjmusic/workstation/issues/258
   */
  void doWork() throws FabricationException;
}
