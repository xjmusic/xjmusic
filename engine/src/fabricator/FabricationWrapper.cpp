// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <utility>

#include "xjmusic/fabricator/FabricationWrapper.h"
#include "xjmusic/util/CsvUtils.h"

using namespace XJ;


FabricationWrapper::FabricationWrapper(Fabricator *fabricator) {
  this->fabricator = fabricator;
}


FabricationException FabricationWrapper::exception(const std::string &message) {
  return FabricationException(formatLog(message));
}


std::string FabricationWrapper::formatLog(const std::string &message) {
  return "[segId=" + std::to_string(fabricator->getSegment().id) + "] " + message;
}


void FabricationWrapper::reportMissing(const std::map<std::string, std::string> &traces) {
  try {
    fabricator->addWarningMessage("EntityUtils not found! " + CsvUtils::from(traces));

  } catch (const std::exception &e) {
    spdlog::warn("Failed to create SegmentMessage", e.what());
  }
}

