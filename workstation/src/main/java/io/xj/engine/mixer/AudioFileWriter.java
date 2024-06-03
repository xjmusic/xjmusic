// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.engine.mixer;

import java.io.IOException;

/**
 Ship service can be used to write N seconds to local .WAV file https://github.com/xjmusic/workstation/issues/272
 */
public interface AudioFileWriter {
  /**
   Open a file for audio writing

   @param targetPath to write to
   @throws IOException if the file cannot be opened
   */
  void open(String targetPath) throws IOException;

  /**
   Append bytes to the currently open file

   @param samples of audio to append
   */
  void append(byte[] samples) throws IOException;

  /**
   Close the writer and release resources

   @return true if the output file is non-empty
   */
  boolean finish() throws IOException;

  /**
   @return true if the writer is currently writing
   */
  boolean isWriting();
}
