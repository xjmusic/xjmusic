// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.mixer;

import io.xj.hub.enums.InstrumentType;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.util.List;

/**
 a Mixer sums audio sources over a finite length of time into an output audio stream.
 <p>
 a Mix instance requires one output audio file format and output stream.
 (NOTE: the audio file format specifies the final output length)
 <p>
 ALL time in the `mix` module is measured in microseconds as `long` primitive.
 Sources are set, each with a time and velocity
 <p>
 A source is loaded then used many times with different velocity.
 Each usage of a source is known as a Put.
 <p>
 Dub mixes audio from disk (not memory) to avoid heap overflow https://github.com/xjmusic/workstation/issues/273
 */
public interface Mixer {
  /**
   Set the level for a given bus name

   @param busId to set level for
   @param level to set
   */
  void setBusLevel(int busId, float level);

  /**
   THE BIG SHOW
   <p>
   Mix out to a file

   @return total # of seconds mixed (float floating-point)
   @throws MixerException if something goes wrong
   */
  float mix(List<ActiveAudio> activeAudios, double intensity) throws MixerException, IOException, FormatException, InterruptedException;

  /**
   Get state

   @return state
   */
  MixerState getState();

  /**
   Get the shared audio buffer to read the mix output

   @return the shared audio buffer
   */
  BytePipeline getBuffer();

  /**
   Get the audio format of the mix output

   @return the audio format
   */
  AudioFormat getAudioFormat();

  /**
   Get the bus number for the given instrument type
   Assign a bus number to an instrument type, in no particular order

   @param instrumentType for which to get the bus number
   @return bus number
   */
  int getBusNumber(InstrumentType instrumentType);

}
