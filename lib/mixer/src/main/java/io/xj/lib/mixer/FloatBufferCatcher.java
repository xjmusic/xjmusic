// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.mixer;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;

/**
 take the output of a TarsosDSP operation and writes in to a buffer.
 */
public class FloatBufferCatcher implements AudioProcessor {
  private final float[] output;
  private int cursor;

  /**
   Initialize the writer.@param format The format of the received bytes.@param output buffer to write to
   */
  public FloatBufferCatcher(int outputLength) {
    output = new float[outputLength];
  }

  /**
   @param audioEvent to process
   @return true if
   */
  @Override
  public boolean process(AudioEvent audioEvent) {
    int iFrom = (int) audioEvent.getSamplesProcessed();
    int length = Math.min(audioEvent.getBufferSize(), output.length - cursor);
    float[] floatBuffer = audioEvent.getFloatBuffer();
    System.arraycopy(floatBuffer, 0, output, iFrom, length);
    cursor = iFrom + length;
    return true;
  }

  @Override
  public void processingFinished() {
    // no op
  }

  /**
   @return final output
   */
  public float[] getFloatBuffer() {
    return output;
  }

  /**
   @return current cursor position
   */
  public int getCursor() {
    return cursor;
  }

}
