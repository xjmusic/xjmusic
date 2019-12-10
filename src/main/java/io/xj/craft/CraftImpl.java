// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.craft;

import io.xj.core.fabricator.Fabricator;
import io.xj.craft.exception.CraftException;

public class CraftImpl {
  protected Fabricator fabricator;

  /**
   Create a new CraftException prefixed with a segment id

   @param message to include in exception
   @return CraftException to throw
   */
  public CraftException exception(String message) {
    return new CraftException(formatLog(message));
  }

  /**
   Create a new CraftException prefixed with a segment id, including sub-exception

   @param message to include in exception
   @param e       Exception to include in exception
   @return CraftException to throw
   */
  public CraftException exception(String message, Exception e) {
    return new CraftException(formatLog(message), e);
  }

  /**
   Format a message with the segmentId as prefix

   @param message to format
   @return formatted message with segmentId as prefix
   */
  public String formatLog(String message) {
    return String.format("[segId=%s] %s", fabricator.getSegment().getId(), message);
  }

}
