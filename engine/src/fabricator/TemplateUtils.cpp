// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/fabricator/TemplateUtils.h"

using namespace XJ;

std::string TemplateUtils::getIdentifier(const Template *tmpl) {
  return tmpl->shipKey.empty() ? tmpl->id : tmpl->shipKey;
}
