// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.fabricator;

import com.google.inject.assistedinject.Assisted;

/**
 [#153542275] Segment wherein velocity changes expect perfectly smooth sound of previous segment through to following segment
 */
@FunctionalInterface
public interface TimeComputerFactory {
  /**
   Configure a TimeComputer instance for a segment

   @param totalBeats of the segment
   @param fromTempo  at the beginning of the segment (in Beats Per Minute)
   @param toTempo    at the end of the segment  (in Beats Per Minute)
   @return TimeComputer instance
   */
  TimeComputer create(
    @Assisted("totalBeats") double totalBeats,
    @Assisted("fromTempo") double fromTempo,
    @Assisted("toTempo") double toTempo
  );
}
