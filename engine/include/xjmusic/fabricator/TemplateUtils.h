// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <string>
#include <optional>

#include "xjmusic/content/Template.h"

namespace XJ {

  class TemplateUtils {
  public:
    static std::string getIdentifier(const Template *tmpl);
  };

}