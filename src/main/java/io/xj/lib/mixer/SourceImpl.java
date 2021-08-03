// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.mixer;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import java.io.BufferedInputStream;

/**
 models a single audio source
 Source stores a series of Samples in Channels across Time, for audio playback.
 */
class SourceImpl implements Source {
  private static final Logger LOG = LoggerFactory.getLogger(SourceImpl.class);
  private static final float microsInASecond = 1000000;

  private final float frameRate;
  private final String sourceId;
  private final AudioFormat inputFormat;
  private final double[][] data;
  private final long inputLengthMicros;
  private final int inputChannels;
  private final double microsPerFrame;
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
    inputChannels = inputFormat.getChannels();
    enforceMax(2, "input audio channels", inputChannels);
    microsPerFrame = microsInASecond / frameRate;

    state = LOADING;
    data = stream.loadFrames();
    inputLengthMicros = (long) (microsInASecond * stream.getActualFrames() / frameRate);

    state = READY;
    LOG.debug("Did load source {}", sourceId);
  }

  /**
   Enforce a maximum

   @param valueMax   maximum allowable value
   @param entityName name of entity, for error message
   @param value      actual
   @throws SourceException if value greater than allowable
   */
  private static void enforceMax(int valueMax, String entityName, int value) throws SourceException {
    if (value > valueMax) {
      throw new SourceException("more than " + valueMax + " " + entityName + " not allowed");
    }
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

