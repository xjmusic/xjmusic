// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.dub;

import io.xj.service.nexus.craft.CraftException;

/**
 [#141] Dub process Segment mix final output of instrument-audio-arrangements
 */
public interface DubMaster {

  /**
   perform master dub for the current segment
   <p>
   [#165799913] Dubbed audio can begin before segment start
   - Segment has `waveform_preroll` field in order to offset the start of the audio
   - During dub work, the waveform preroll required for the current segment is determined by finding the earliest positioned audio sample. **This process must factor in the transient of each audio sample.**
   - During dub work, output audio includes the head start, and `waveform_preroll` value is persisted to segment
   */
  void doWork() throws DubException, CraftException, DubException;

}
