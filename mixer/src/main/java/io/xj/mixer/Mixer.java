// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.mixer;

import io.xj.mixer.impl.exception.FormatException;
import io.xj.mixer.impl.exception.MixerException;
import io.xj.mixer.impl.exception.PutException;
import io.xj.mixer.impl.exception.SourceException;

import javax.sound.sampled.AudioFormat;
import java.io.BufferedInputStream;
import java.io.IOException;

/**
 a Mixer sums audio sources over a finite length of time into an output audio stream.
 <p>
 a Mix instance requires one output audio file format and output stream.
 (NOTE: the audio file format specifies the final output length)
 <p>
 ALL time in the `mix` module is measured in microseconds as `long` primitive.
 Sources are set, each with a time, velocity and pitch ratio.
 <p>
 A source is loaded then used many times with different velocity and pitch.
 Each usage of a source is known as a Put.
 <p>
 Finally, the output audio is pulled out of the mixer. The final number of output
 "frames" of audio is known ahead of time (frame rate X length) and the mixer
 knows to provide all of those output audio frames in sequence from first to last.
 <p>
 Before frame zero, then again every N number of frames (the "mix cycle")
 some more costly operations are performed. For example, there are multiple
 collections of Puts stored in the mixer: readyPuts, livePuts, and donePuts.
 Puts are staged in readyPuts, then moved into livePuts only when within a small
 range of their playback time. This greatly reduced overhead of checking all
 the puts for live-ness during the course of a single mix.
 */
public interface Mixer {
  String READY = "ready";
  String MIXING = "mixing";

  /**
   Put a source audio into the mix at a specified time, velocity and pitch ratio.
   <p>
   the Put only has a reference to the source--
   so the Mixer has to use that reference source id along with other variables from the Put,
   in order to arrive at the final source output value at any given microsecond@param sourceId      under which the audio is stored
   @param startAtMicros duration from beginning of mix
   @param stopAtMicros  duration from beginning of mix
   @param attackMicros
   @param releaseMicros
   @param velocity      0 to 1
   @param pitchRatio    relative speed multiplier, e.g. original = 1.0
   @param pan           -1 (left) to +1 (right)


   */
  void put(String sourceId, long startAtMicros, long stopAtMicros, long attackMicros, long releaseMicros, double velocity, double pitchRatio, double pan) throws PutException;

  /**
   Load a source audio from input stream and cache it in memory under an alias.

   @param sourceId    under which to store this source
   @param inputStream of source audio
   */
  void loadSource(String sourceId, BufferedInputStream inputStream) throws FormatException, IOException, SourceException;

  /**
   Mix out to a file

   @throws MixerException if something goes wrong
   @param outputFilePath path
   @param quality of output (if lossy encoding)
   */
  void mixToFile(OutputEncoder outputEncoder, String outputFilePath, Float quality) throws Exception;

  /**
   Get the current count of all sources.

   @return count
   */
  int getSourceCount();

  /**
   Get the current count of all ready + live Puts.

   @return count
   */
  int getPutCount();

  /**
   Get the current count of all ready Puts.

   @return count
   */
  int getPutReadyCount();

  /**
   Get the current count of all live Puts.

   @return count
   */
  int getPutLiveCount();

  /**
   Get the current count of all done Puts.

   @return count
   */
  int getPutDoneCount();

  /**
   Get state

   @return state
   */
  String getState();

  /**
   Get frame rate

   @return frame rate
   */
  float getFrameRate();

  /**
   get output audio format

   @return format
   */
  AudioFormat getOutputFormat();

  /**
   Set the duration between "mix cycles", wherein garbage collection is performed.

   @param micros the duration of a mix cycle, in microseconds
   @throws MixerException if the mix frame rate is not yet known
   */
  void setCycleMicros(long micros) throws MixerException;

  /**
   is debugging?

   @return true if debugging
   */
  boolean isDebugging();

  /**
   Set debugging mode; this defaults to false to avoid some extra overhead

   @param debugging true to activate debugging
   */
  void setDebugging(boolean debugging);
}
