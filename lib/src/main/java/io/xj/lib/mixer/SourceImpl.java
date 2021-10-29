// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.mixer;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import java.io.BufferedInputStream;

import static io.xj.lib.util.Values.MICROS_PER_SECOND;

/**
 models a single audio source
 stores a series of Samples in Channels across Time, for audio playback.
 */
class SourceImpl implements Source {
  private static final Logger LOG = LoggerFactory.getLogger(SourceImpl.class);

  private final float frameRate;
  private final String sourceId;
  private final AudioFormat inputFormat;
  private final double[][] data;
  private final long inputLengthMicros;
  private final double microsPerFrame;
  private final double lengthSeconds;
  private String state;

  @Inject
  public SourceImpl(
    @Assisted("sourceId") String sourceId,
    @Assisted("inputStream") BufferedInputStream inputStream
  ) throws Exception {
    this.sourceId = sourceId;

    state = STAGED;
    AudioStreamLoader stream = new AudioStreamLoader(inputStream);
    inputFormat = stream.getAudioFormat();
    frameRate = inputFormat.getFrameRate();
    int inputChannels = inputFormat.getChannels();
    enforceMaxStereo(inputChannels);
    microsPerFrame = MICROS_PER_SECOND / frameRate;

    state = LOADING;
    data = stream.loadFrames();
    lengthSeconds = stream.getActualFrames() / frameRate;
    inputLengthMicros = (long) (MICROS_PER_SECOND * lengthSeconds);

    state = READY;
    LOG.debug("Did load source {}", sourceId);
  }

  /**
   Enforce a maximum

   @param value actual
   @throws SourceException if value greater than allowable
   */
  static void enforceMaxStereo(int value) throws SourceException {
    if (value > 2)
      throw new SourceException("more than 2 input audio channels not allowed");
  }

  @Override
  public long lengthMicros() {
    return inputLengthMicros;
  }

  public AudioFormat getInputFormat() {
    return inputFormat;
  }

  @Override
  public String getState() {
    return state;
  }

  @Override
  public String getSourceId() {
    return sourceId;
  }

  @Override
  public float getFrameRate() {
    return frameRate;
  }

  @Override
  public double[][] getData() {
    return data;
  }

  @Override
  public double getValue(long atMicros, int c) {
    int f = frameAtMicros(atMicros);
    return f < data.length ? data[f][(c < data[f].length ? c : 0)] : 0;
  }

  @Override
  public double getLengthSeconds() {
    return lengthSeconds;
  }

  @Override
  public String toString() {
    return String.format("id[%s] frames[%d]", sourceId, data.length);
  }

  /**
   quick get frame at a particular duration in microseconds from beginning

   @param atMicros at which to get frame number
   @return frame number of micros
   */
  private int frameAtMicros(long atMicros) {
    return (int) Math.floor(atMicros / microsPerFrame);
  }

}

