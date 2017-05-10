// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.mixer.impl;

import io.outright.xj.mixer.Source;
import io.outright.xj.mixer.impl.audio.AudioStreamLoader;
import io.outright.xj.mixer.impl.exception.FormatException;
import io.outright.xj.mixer.impl.exception.SourceException;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import java.io.BufferedInputStream;
import java.io.IOException;

/**
 models a single audio source
 Source stores a series of Samples in Channels across Time, for audio playback.
 */
public class SourceImpl implements Source {
  private final static Logger log = LoggerFactory.getLogger(SourceImpl.class);
  private final static float microsInASecond = 1000000;
  private final static float nanosInASecond = 1000 * microsInASecond;

  private final float frameRate;
  private final String sourceId;
  private double[][] frames;
  private final AudioFormat inputFormat;
  private long inputLengthMicros;
  private int inputChannels;
  private double microsPerFrame;
  private String state;

  @Inject
  public SourceImpl(
    @Assisted("sourceId") String sourceId,
    @Assisted("inputStream") BufferedInputStream inputStream
  ) throws SourceException, FormatException, IOException {
    this.sourceId = sourceId;

    state = STAGED;
    AudioStreamLoader stream;
    stream = new AudioStreamLoader(inputStream);
    inputFormat = stream.getAudioFormat();
    frameRate = inputFormat.getFrameRate();
    inputChannels = inputFormat.getChannels();
    enforceMax(2, "input audio channels", inputChannels);
    microsPerFrame = microsInASecond / frameRate;

    state = LOADING;
    frames = stream.loadFrames();
    inputLengthMicros = (long) (microsInASecond * stream.getActualFrames() / frameRate);

    state = READY;
    log.info("Did load source {}", this);
  }

  @Override
  public double[] frameAt(long atMicros, double volume, double pan, int outChannels) {
    int atFrame = frameAtMicros(atMicros);
    if (atMicros < inputLengthMicros && atFrame < inputLengthMicros) {
      switch (outChannels) {
        case 1:
          return monoFrameAt(atFrame, volume);
        case 2:
          return stereoFrameAt(atFrame, volume, pan);
      }
    }
    return new double[outChannels];
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
  public String toString() {
    return String.format("id[%s] frames[%d]", sourceId, frames.length);
  }


  /**
   Get a 1-channel frame a specific Tz, volume (0 to 1), and pan (-1 to +1)

   @param atFrame since beginning of source
   @param volume  to mix output to
   @return array of samples
   */
  private double[] monoFrameAt(int atFrame, double volume) {
    switch (inputChannels) {
      case 1:
        return new double[]{frames[atFrame][0] * volume};
      case 2:
        return new double[]{volume * (frames[atFrame][0] + frames[atFrame][1]) / 2};
      default:
        return new double[1];
    }
  }

  /**
   Get a 2-channel frame a specific Tz, volume (0 to 1), and pan (-1 to +1)

   @param atFrame since beginning of source
   @param volume  to mix output to
   @param pan     to mix output to
   @return array of samples
   */
  private double[] stereoFrameAt(int atFrame, double volume, double pan) {
    switch (inputChannels) {
      case 1:
        return new double[]{
          frames[atFrame][0] * volume * left(pan),
          frames[atFrame][0] * volume * right(pan)
        };
      case 2:
        return new double[]{
          frames[atFrame][0] * volume * left(pan),
          frames[atFrame][1] * volume * right(pan)
        };
      default:
        return new double[2];
    }
  }

  /**
   Volume ratio for right channel for a given pan.

   @param pan -1 to +1
   @return ratio
   */
  private double right(double pan) {
    if (pan >= 0) {
      // 0 to +1 (right) = full volume
      return 1;
    } else {
      // <0 to -1 = decay to zero;
      return 1 - Math.abs(pan);
    }
  }

  /**
   Volume ratio for left channel for a given pan.

   @param pan -1 to +1
   @return ratio
   */
  private double left(double pan) {
    if (pan <= 0) {
      // 0 to -1 (left) = full volume
      return 1;
    } else {
      // >0 to +1 = decay to zero;
      return 1 - pan;
    }
  }

  /**
   quick get frame at a particular duration in microseconds from beginning

   @param atMicros at which to get frame number
   @return frame number of micros
   */
  private int frameAtMicros(long atMicros) {
    return (int) Math.floor(atMicros / microsPerFrame);
  }

  /**
   Enforce a maximum

   @param valueMax   maximum allowable value
   @param entityName name of entity, for error message
   @param value      actual
   @throws SourceException if value greater than allowable
   */
  private void enforceMax(int valueMax, String entityName, int value) throws SourceException {
    if (value > valueMax) {
      throw new SourceException("more than " + valueMax + " " + entityName + " not allowed");
    }
  }

}

