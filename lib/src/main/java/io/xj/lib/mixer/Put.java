// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.mixer;

import java.util.UUID;

/**
 Put to represent a single audio source playing at a specific time in the future.
 <p>
 Provides an attack/release envelope from 0.0 to 1.0 per dub using an Attack/Release envelope https://www.pivotaltracker.com/story/show/150279617
 */
public interface Put {
  String READY = "ready";
  String PLAY = "play";
  String DONE = "done";

  /**
   * Get the active id of this put
   * @return active id
   */
  UUID getId();

  /**
   * Get reference source id
   *
   * @return source id
   */
  UUID getAudioId();

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
   get Attack milliseconds

   @return Attack milliseconds
   */
  int getAttackMillis();

  /**
   get Release milliseconds

   @return Release milliseconds
   */
  int getReleaseMillis();

  /**
   Get the bus id to output

   @return output bus id
   */
  int getBus();
}
