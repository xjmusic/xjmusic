// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.engine.fabricator;

import io.xj.hub.pojos.Template;
import io.xj.hub.util.StringUtils;
import jakarta.annotation.Nullable;

import java.util.Objects;

public class TemplateUtils {
  /**
   Get the identifier or a Template: ship key if available, else ID

   @param template to get identifier of
   @return ship key if available, else ID
   */
  public static String getIdentifier(@Nullable Template template) {
    if (Objects.isNull(template)) return "N/A";
    return StringUtils.isNullOrEmpty(template.getShipKey()) ? template.getId().toString() : template.getShipKey();
  }
}
