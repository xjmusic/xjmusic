// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJ_MUSIC_FABRICATOR_FABRICATION_WRAPPER_H
#define XJ_MUSIC_FABRICATOR_FABRICATION_WRAPPER_H

#include <spdlog/spdlog.h>

#include "Fabricator.h"
#include "FabricationException.h"

namespace XJ {

/**
 Fabrication wrapper is a common foundation for all craft
 */
  class FabricationWrapper {
  public:

    /**
     Must extend this class and inject

     @param fabricator internal
     */
    FabricationWrapper(Fabricator *fabricator);

    /**
     Create a new FabricationException prefixed with a segment id

     @param message to include in exception
     @return FabricationException to throw
     */
    FabricationException exception(const std::string& message);

    /**
     Format a message with the segmentId as prefix

     @param message to format
     @return formatted message with segmentId as prefix
     */
    std::string formatLog(const std::string& message);

    /**
     Report a missing entity as a segment message
     @param traces of how missing entity was searched for
     */
    void reportMissing(const std::map<std::string, std::string>& traces);

  protected:
    Fabricator *fabricator;

  };

}// namespace XJ

#endif// XJ_MUSIC_FABRICATOR_FABRICATION_WRAPPER_H