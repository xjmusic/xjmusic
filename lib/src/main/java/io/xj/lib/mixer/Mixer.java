// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.lib.mixer;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.util.UUID;

/**
 * a Mixer sums audio sources over a finite length of time into an output audio stream.
 * <p>
 * a Mix instance requires one output audio file format and output stream.
 * (NOTE: the audio file format specifies the final output length)
 * <p>
 * ALL time in the `mix` module is measured in microseconds as `long` primitive.
 * Sources are set, each with a time and velocity
 * <p>
 * A source is loaded then used many times with different velocity.
 * Each usage of a source is known as a Put.
 * <p>
 * Dub mixes audio from disk (not memory) to avoid heap overflow https://www.pivotaltracker.com/story/show/180206211
 */
public interface Mixer {
  /**
   * Put a source audio into the mix at a specified time and velocity.
   * <p>
   * the Put only has a reference to the source--
   * so the Mixer has to use that reference source id along with other variables from the Put,
   * in order to arrive at the final source output value at any given microsecond
   * <p>
   * One-shot fadeout mode https://www.pivotaltracker.com/story/show/183385397
   * <p>
   * If this is an update if position of an existing pick (mix frame moves forward in time covering the same pick) then update, don't create another put
   * Mixer is kept alive and used as a rolling mix frame, part of Nexus DAW mixing architecture https://www.pivotaltracker.com/story/show/185456028
   *
   * @param id
   * @param audioId       under which to store the audio
   * @param busId         on which to put source
   * @param startAtMicros duration from beginning of mix
   * @param stopAtMicros  duration from beginning of mix
   * @param velocity      0 to 1
   * @param attackMillis  attack
   * @param releaseMillis release
   */
  void put(UUID id, UUID audioId, int busId, long startAtMicros, long stopAtMicros, double velocity, int attackMillis, int releaseMillis) throws PutException;

  /**
   * Remove a source from the mix
   * <p>
   * Mixer is kept alive and used as a rolling mix frame, part of Nexus DAW mixing architecture https://www.pivotaltracker.com/story/show/185456028
   *
   * @param activeId under which to store the audio
   */
  void del(UUID activeId);

  /**
   * Set the level for a given bus name
   *
   * @param busId to set level for
   * @param level to set
   */
  void setBusLevel(int busId, double level);

  /**
   * Load a source audio from input stream and cache it in memory under an alias.
   * <p>
   * Fabrication should not completely fail because of one bad source audio https://www.pivotaltracker.com/story/show/182575665
   *
   * @param audioId     under which to store this source
   * @param pathToFile  of source audio
   * @param description in case of failure logs
   */
  void loadSource(UUID audioId, String pathToFile, String description) throws FormatException, IOException, SourceException;

  /**
   * THE BIG SHOW
   * <p>
   * Mix out to a file
   *
   * @return total # of seconds mixed (double floating-point)
   * @throws MixerException if something goes wrong
   */
  double mix() throws MixerException, IOException, FormatException, InterruptedException;

  /**
   * Get the current count of all sources.
   *
   * @return count
   */
  int getSourceCount();

  /**
   * Get state
   *
   * @return state
   */
  MixerState getState();

  /**
   * Whether the mixer has loaded a specified source
   *
   * @param audioId to check for
   * @return true if source has been loaded
   */
  boolean hasLoadedSource(UUID audioId);

  /**
   * Get a source by id
   *
   * @param audioId to get
   * @return source
   */
  Source getSource(UUID audioId);

  /**
   * Get the shared audio buffer to read the mix output
   *
   * @return the shared audio buffer
   */
  BytePipeline getBuffer();

  /**
   * Get the audio format of the mix output
   *
   * @return the audio format
   */
  AudioFormat getAudioFormat();
}
