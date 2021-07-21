// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.fabricator;

import com.google.inject.Inject;
import io.xj.SegmentMessage;
import io.xj.lib.util.CSV;
import io.xj.nexus.NexusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;

/**
 Fabrication wrapper is a common foundation for all craft
 */
public abstract class FabricationWrapperImpl {
  private final Logger log = LoggerFactory.getLogger(FabricationWrapperImpl.class);
  private static final SecureRandom random = new SecureRandom();
  protected Fabricator fabricator;

  /**
   Put N marbles in the bag, with one marble set to true, and the others set to false

   @param odds against one, or the total number of marbles, of which one is true
   @return a marble from the bag
   */
  protected boolean beatOddsAgainstOne(int odds) {
    return random.nextInt(odds) == 0;
  }

  /**
   Must extend this class and inject

   @param fabricator internal
   */
  @Inject
  public FabricationWrapperImpl(Fabricator fabricator) {
    this.fabricator = fabricator;
  }

  /**
   Create a new NexusException prefixed with a segment id

   @param message to include in exception
   @return NexusException to throw
   */
  public NexusException exception(String message) {
    return new NexusException(formatLog(message));
  }

  /**
   Create a new NexusException prefixed with a segment id, including sub-exception

   @param message to include in exception
   @param e       Exception to include in exception
   @return NexusException to throw
   */
  public NexusException exception(String message, Exception e) {
    return new NexusException(formatLog(message), e);
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

  /**
   Report a missing entity as a segment message@param traces of how missing entity was searched for
   */
  protected void reportMissing(Map<String, String> traces) {
    try {
      fabricator.add(SegmentMessage.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setSegmentId(fabricator.getSegment().getId())
        .setType(SegmentMessage.Type.Warning)
        .setBody(String.format("%s not found! %s", io.xj.InstrumentAudio.class.getSimpleName(), CSV.from(traces)))
        .build());

    } catch (Exception e) {
      log.warn("Failed to create SegmentMessage", e);
    }
  }
}
