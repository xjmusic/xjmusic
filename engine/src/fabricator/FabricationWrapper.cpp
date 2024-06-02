// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/fabricator/FabricationWrapper.h"

#include <exception>
#include <iostream>
#include <map>
#include <sstream>
#include <stdexcept>
#include <string>

class NexusException : public std::runtime_error {
public:
  NexusException(const std::string &message) : std::runtime_error(message) {}
};

class Fabricator {
public:
  virtual std::string getSegmentId() = 0;
  virtual void addWarningMessage(const std::string &message) = 0;
};

class FabricationWrapperImpl {
protected:
  Fabricator *fabricator;

public:
  FabricationWrapperImpl(Fabricator *fabricator) : fabricator(fabricator) {}

  NexusException exception(const std::string &message) {
    return NexusException(formatLog(message));
  }

  NexusException exception(const std::string &message, const std::exception &e) {
    return NexusException(formatLog(message) + ", " + e.what());
  }

  std::string formatLog(const std::string &message) {
    std::stringstream ss;
    ss << "[segId=" << fabricator->getSegmentId() << "] " << message;
    return ss.str();
  }

  void reportMissing(std::map<std::string, std::string> traces) {
    try {
      std::stringstream ss;
      ss << "InstrumentAudio not found! ";
      for (const auto &trace: traces) {
        ss << trace.first << "=" << trace.second << ", ";
      }
      fabricator->addWarningMessage(ss.str());
    } catch (const std::exception &e) {
      std::cerr << "Failed to create SegmentMessage: " << e.what() << std::endl;
    }
  }
};

/*
TODO remove Java imports
  package io.xj.nexus.fabricator;
  import io.xj.hub.pojos.InstrumentAudio;
  import io.xj.hub.util.CsvUtils;
  import io.xj.nexus.NexusException;
  import org.slf4j.Logger;
  import org.slf4j.LoggerFactory;
  import java.util.Map;
*/

/**
 Fabrication wrapper is a common foundation for all craft
 */
public
abstract class FabricationWrapperImpl {
  final Logger LOG = LoggerFactory.getLogger(FabricationWrapperImpl.class);
protected
  Fabricator fabricator;

  /**
   Must extend this class and inject

   @param fabricator internal
   */
public
  FabricationWrapperImpl(Fabricator fabricator) {
    this.fabricator = fabricator;
  }

  /**
   Create a new NexusException prefixed with a segment id

   @param message to include in exception
   @return NexusException to throw
   */
public
  NexusException exception(String message) {
    return new NexusException(formatLog(message));
  }

  /**
   Create a new NexusException prefixed with a segment id, including sub-exception

   @param message to include in exception
   @param e       Exception to include in exception
   @return NexusException to throw
   */
public
  NexusException exception(String message, Exception e) {
    return new NexusException(formatLog(message), e);
  }

  /**
   Format a message with the segmentId as prefix

   @param message to format
   @return formatted message with segmentId as prefix
   */
public
  String formatLog(String message) {
    return String.format("[segId=%s] %s", fabricator.getSegment().getId(), message);
  }

  /**
   Report a missing entity as a segment message@param traces of how missing entity was searched for
   */
protected
  void reportMissing(Map<String, String> traces) {
    try {
      fabricator.addWarningMessage(String.format("%s not found! %s", InstrumentAudio.class.getSimpleName(), CsvUtils.from(traces)));

    } catch (Exception e) {
      LOG.warn("Failed to create SegmentMessage", e);
    }
  }
}
