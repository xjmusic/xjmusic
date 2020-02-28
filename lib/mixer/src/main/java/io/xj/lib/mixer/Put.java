// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.mixer;

/**
 Put to represent a single audio source playing at a specific time in the future.

 Provides an attack/release envelope from 0.0 to 1.0 per [#150279617] dub using an Attack/Release envelope
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
   [#150279617] Engineer wants XJ to dub using an Attack/Release envelope, in order to enhance quality.
   // FUTURE: smooth (non-linear) envelope

   @param atMixOffsetMicros a moment in the final mix
   @return corresponding envelope (ratio from 0 to 1) to multiply this source audio by at that instant
   */
  Double envelope(long atMixOffsetMicros);

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

  /**
   Get the length of the attack envelope


   @return microseconds
   */
  long getAttackMicros();

  /**
   Get the length of the release envelope

   @return microseconds
   */
  long getReleaseMicros();
}
