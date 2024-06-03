// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.engine.mixer;


/**
 One-shot fadeout mode https://github.com/xjmusic/workstation/issues/226
 */
public interface EnvelopeProvider {
  /**
   Compute an envelope of the given length, caching each unique envelope length.
   <p></p>
   One-shot fadeout mode https://github.com/xjmusic/workstation/issues/226

   @param frames length of envelope
   @return envelope
   */
  Envelope length(
    Integer frames
  );
}
