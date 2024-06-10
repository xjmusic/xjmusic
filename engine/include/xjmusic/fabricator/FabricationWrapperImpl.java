// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.engine.fabricator;

import io.xj.model.pojos.InstrumentAudio;
import io.xj.model.util.CsvUtils;
import io.xj.engine.FabricationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 Fabrication wrapper is a common foundation for all craft
 */
public abstract class FabricationWrapperImpl {
  final Logger LOG = LoggerFactory.getLogger(FabricationWrapperImpl.class);
  protected Fabricator fabricator;

  /**
   Must extend this class and inject

   @param fabricator internal
   */
  public FabricationWrapperImpl(Fabricator fabricator) {
    this.fabricator = fabricator;
  }

  /**
   Create a new FabricationException prefixed with a segment id

   @param message to include in exception
   @return FabricationException to throw
   */
  public FabricationException exception(std::string message) {
    return new FabricationException(formatLog(message));
  }

  /**
   Create a new FabricationException prefixed with a segment id, including sub-exception

   @param message to include in exception
   @param e       Exception to include in exception
   @return FabricationException to throw
   */
  public FabricationException exception(std::string message, Exception e) {
    return new FabricationException(formatLog(message), e);
  }

  /**
   Format a message with the segmentId as prefix

   @param message to format
   @return formatted message with segmentId as prefix
   */
  public std::string formatLog(std::string message) {
    return std::string.format("[segId=%s] %s", fabricator.getSegment().id, message);
  }

  /**
   Report a missing entity as a segment message@param traces of how missing entity was searched for
   */
  protected void reportMissing(Map<std::string, std::string> traces) {
    try {
      fabricator.addWarningMessage(std::string.format("%s not found! %s", InstrumentAudio.class.getSimpleName(), CsvUtils.from(traces)));

    } catch (Exception e) {
      LOG.warn("Failed to create SegmentMessage", e);
    }
  }
}
