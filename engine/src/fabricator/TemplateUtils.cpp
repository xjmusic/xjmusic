// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/fabricator/TemplateUtils.h"

using namespace XJ;

std::string TemplateUtils::getIdentifier(const std::optional<Template> &templateObj) {
  if (!templateObj.has_value()) return "N/A";
  return templateObj->shipKey.empty() ? templateObj->id : templateObj->shipKey;
}
