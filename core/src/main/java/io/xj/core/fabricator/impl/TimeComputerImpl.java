//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.fabricator.impl;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.core.config.Config;
import io.xj.core.fabricator.TimeComputer;
import io.xj.music.BPM;

import java.util.Map;

/**
 A TimeComputer determines the position in time, given a position in beats within a Segment.
 <p>
 An instance of a TimeComputer is configured for one segment:
 Total # of beats int the segment
 Tempo (of) at the beginning of the segment
 Tempo (to) at the end of the segment
 <p>
 Computations are done internally using Velocity, which is seconds-per-beat (NOT beats per minute)
 <p>
 [#153542275] Segment wherein velocity changes expect perfectly smooth sound of previous segment through to following segment
 <p>
 FUTURE: instead of computing a fine grain map ahead of time, compute a coarser map and interpolate to requested position.
 */
public class TimeComputerImpl implements TimeComputer {
  double totalBeats;
  double toVelocity;
  double fromVelocity;
  double velocityDelta;
  Map<Integer, Double> timeAtPosition = Maps.newHashMap();
  double div = Config.getComputeTimeFramesPerBeat();
  double sub = Config.getComputeTimeResolutionHz();
  double inc = 1 / div;

  /**
   Configure a TimeComputer instance for a segment

   @param totalBeats of the segment
   @param fromTempo  at the beginning of the segment (in Beats Per Minute)
   @param toTempo    at the end of the segment  (in Beats Per Minute)
   */
  @Inject
  public TimeComputerImpl(
    @Assisted("totalBeats") double totalBeats,
    @Assisted("fromTempo") double fromTempo,
    @Assisted("toTempo") double toTempo
  ) {
    this.totalBeats = totalBeats;
    fromVelocity = BPM.velocity(fromTempo);
    toVelocity = BPM.velocity(toTempo);
    velocityDelta = toVelocity - fromVelocity;
    timeAtPosition.put(0, 0.0);

    // computed by integral, smoothly fading of previous segment velocity to current
    double time = 0.0d;
    double position = 0.0d;
    while (position <= totalBeats) {
      time += inc * velocityAtPosition(position);
      position += inc;
      timeAtPosition.put(normalizePosition(position), normalizeTime(time));
    }
  }

  /**
   Normalize a time value, given a resolution in Hz, such that the final floating point value has no extraneous digits.

   @param time to normalize
   @return normalized time
   */
  private double normalizeTime(double time) {
    return Math.floor(time * sub) / sub;
  }

  /**
   Normalize a position value, given a number of frames that each beat is supposed to be divided into,
   such that all possible positions form an unbroken sequence of integers beginning at 0.

   @param position to normalize
   @return normalized position
   */
  private int normalizePosition(double position) {
    return (int) Math.floor(position * div);
  }

  /**
   Get the velocity at any position in the segment

   @param pos position to get velocity of
   @return velocity at given position
   */
  private double velocityAtPosition(double pos) {
    return fromVelocity + velocityDelta * pos / totalBeats;
  }

  @Override
  public Double getSecondsAtPosition(double p) {
    if (0 > p) {
      // before beginning, at velocity of previous segment
      return p * fromVelocity;

    } else if (totalBeats < p) {
      // after end, at velocity of current segment
      // recursively use same function to get end of current segment; can't infinitely recurse because T < T impossible
      return getSecondsAtPosition(totalBeats) + ((p - totalBeats) * toVelocity);

    } else {
      return timeAtPosition.get(normalizePosition(p));
    }
  }

}
