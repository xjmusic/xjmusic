//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.fabricator;

@FunctionalInterface
public interface TimeComputer {
  /**
   Determine the position in time, given a position in beats within the Segment.

   @param p position in beats within the Segment
   @return position in time, seconds from start of Segment
   */
  Double getSecondsAtPosition(double p);
}
