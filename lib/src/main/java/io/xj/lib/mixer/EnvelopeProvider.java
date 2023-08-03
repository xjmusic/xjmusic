// Copyright (c) 1999-2022, XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.lib.mixer;


/**
 * One-shot fadeout mode https://www.pivotaltracker.com/story/show/183385397
 */
public interface EnvelopeProvider {
  /**
   * Compute an envelope of the given length, caching each unique envelope length.
   * <p></p>
   * One-shot fadeout mode https://www.pivotaltracker.com/story/show/183385397
   *
   * @param frames length of envelope
   * @return envelope
   */
  Envelope length(
    Integer frames
  );
}
