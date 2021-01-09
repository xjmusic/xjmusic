// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.nexus.craft;

import io.xj.SegmentMessage;
import io.xj.service.nexus.craft.exception.CraftException;
import io.xj.service.nexus.fabricator.Fabricator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class CraftImpl {
  private final Logger log = LoggerFactory.getLogger(CraftImpl.class);
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


  /**
   Report a missing entity as a segment message

   @param type   of class that is missing
   @param detail of how missing entity was searched for
   */
  protected void reportMissing(Class<?> type, String detail) {
    try {
      fabricator.add(SegmentMessage.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setSegmentId(fabricator.getSegment().getId())
        .setType(SegmentMessage.Type.Warning)
        .setBody(String.format("%s not found %s", type.getSimpleName(), detail))
        .build());

    } catch (Exception e) {
      log.warn("Failed to create SegmentMessage", e);
    }
  }
}
