// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.mixer.impl.audio;

import io.xj.mixer.impl.exception.FormatException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.BufferedInputStream;
import java.io.IOException;

/**
 Load audio data from input stream
 */
public class AudioStreamLoader {

  private static final int READ_BUFFER_BYTE_SIZE = 1024;
  public static final int MAX_INT_LENGTH_ARRAY_SIZE = 2147483647;

  private final AudioFormat audioFormat;
  private final AudioInputStream audioInputStream;
  private final double[][] frames;
  private final int channels;
  private final int frameSize;
  private final int sampleSize;
  private final int expectBytes;
  private final long expectFrames;
  private long actualFrames;

  /**
   Instantiate a stream loader,
   which immediately reads the audio data from the stream
   and caches it in-memory in the object itself.
   <p>
   retry n times to get audio input stream, sleeping between retries
   <p>
   See: [#150279565] During Dub, if a cached loaded source is not yet available it should be retried n times.

   @param inputStream to read audio data from
   @throws FormatException if unable to read the audio format
   */
  public AudioStreamLoader(BufferedInputStream inputStream) throws FormatException, InterruptedException {
    try {
      audioInputStream = AudioSystem.getAudioInputStream(inputStream);

      audioFormat = audioInputStream.getFormat();
      frameSize = audioInputStream.getFormat().getFrameSize();
      channels = audioInputStream.getFormat().getChannels();
      sampleSize = frameSize / channels;
      expectBytes = audioInputStream.available();

      if (expectBytes == audioInputStream.getFrameLength()) {
        // this is a bug where AudioInputStream returns bytes (instead of frames which it claims)
        expectFrames = expectBytes / frameSize;
      } else {
        expectFrames = audioInputStream.getFrameLength();
      }

      frames = new double[(int) expectFrames][channels];

    } catch (Exception e) {

      throw new FormatException("unable to read audio input stream (" + e.getClass().getName() + "): " + e.getMessage());
    }

    if (MAX_INT_LENGTH_ARRAY_SIZE <= expectBytes) { // max int-length array size
      throw new FormatException("loading audio steams longer than 2,147,483,647 frames (max. value of signed 32-bit integer) is not supported");
    }

    if (AudioSystem.NOT_SPECIFIED == frameSize || AudioSystem.NOT_SPECIFIED == expectFrames) {
      throw new FormatException("audio streams with unspecified frame size or length are unsupported");
    }

  }

  /**
   get frames final read from steam

   @return frames
   */
  public double[][] loadFrames() throws FormatException, IOException {
    String sampleType = AudioSample.typeOfInput(audioInputStream.getFormat());

    int numBytesReadToBuffer;
    int currentFrame = 0;
    byte[] sampleBuffer = new byte[sampleSize];
    byte[] readBuffer = new byte[READ_BUFFER_BYTE_SIZE];
    while ((numBytesReadToBuffer = audioInputStream.read(readBuffer)) != -1) {
      for (int b = 0; b < numBytesReadToBuffer; b += frameSize) {
        frames[currentFrame] = new double[channels];
        for (int c = 0; c < channels; c++) {
          System.arraycopy(readBuffer, b + c * sampleSize, sampleBuffer, 0, sampleSize);
          double value = AudioSample.fromBytes(sampleBuffer, sampleType);
          frames[currentFrame][c] = value;
        }
        currentFrame++;
      }
    }
    actualFrames = currentFrame;

    return frames;
  }

  /**
   get actual # pf frames read
   (should match expected)

   @return frames
   */
  public long getActualFrames() {
    return actualFrames;
  }

  /**
   get total frames available in audio stream

   @return frames
   */
  long getExpectFrames() {
    return expectFrames;
  }

  /**
   get total bytes available in audio stream

   @return size
   */
  int getExpectBytes() {
    return expectBytes;
  }

  /**
   get frame size

   @return size
   */
  int getFrameSize() {
    return frameSize;
  }

  /**
   Get Audio file format

   @return format
   */
  public AudioFormat getAudioFormat() {
    return audioFormat;
  }
}
