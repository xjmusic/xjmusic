// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.mixer;

/**
 Put to represent a single audio source playing at a specific time in the future.
 */
public interface Put {
  String READY = "ready";
  String PLAY = "play";
  String DONE = "done";

  /**
   For a specific moment in the final mix, get the corresponding moment in the source audio.

   @param atMixOffsetMicros a moment in the final mix
   @return corresponding moment in the source audio
   */
  long sourceOffsetMicros(long atMixOffsetMicros);

  /**
   Is the put alive?

   @return true if not done
   */
  boolean isAlive();

  /**
   Is the put playing?

   @return true if playing
   */
  boolean isPlaying();

  /**
   Get reference source id

   @return source id
   */
  String getSourceId();

  /**
   Get State

   @return state
   */
  String getState();

  /**
   get Start-At

   @return Start-At
   */
  long getStartAtMicros();

  /**
   get Stop-At

   @return Stop-At
   */
  long getStopAtMicros();

  /**
   get Velocity

   @return Velocity
   */
  double getVelocity();

  /**
   get Pitch Ratio

   @return Pitch Ratio
   */
  double getPitchRatio();

  /**
   get Pan

   @return Pan
   */
  double getPan();
}
