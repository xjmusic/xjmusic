// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.mixer;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class BytePipeline {
  final PipedInputStream inputStream;
  final PipedOutputStream outputStream;

  public BytePipeline(int pipeSize) throws IOException {
    this.outputStream = new PipedOutputStream();
    this.inputStream = new PipedInputStream(outputStream, pipeSize);
  }

  public void produce(byte[] data) throws IOException {
    outputStream.write(data);
    outputStream.flush();
  }

  public byte[] consume(int length) throws IOException {
    byte[] buffer = new byte[length];
    int readLength = inputStream.read(buffer);

    // Handle partial read by creating a new array with just the data that was read.
    if (readLength != length) {
      byte[] actualData = new byte[readLength];
      System.arraycopy(buffer, 0, actualData, 0, readLength);
      buffer = actualData;
    }

    return buffer;
  }

  public int getAvailableByteCount() throws IOException {
    return inputStream.available();
  }
}
