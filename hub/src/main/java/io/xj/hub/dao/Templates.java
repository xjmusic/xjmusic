// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.dao;

import com.google.common.base.Strings;
import io.xj.hub.tables.pojos.Template;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 Utilities for working with templates
 */
public enum Templates {
  ;

  /**
   Get the identifier or a Template: ship key if available, else ID

   @param template to get identifier of
   @return ship key if available, else ID
   */
  public static String getIdentifier(@Nullable Template template) {
    if (Objects.isNull(template)) return "N/A";
    return Strings.isNullOrEmpty(template.getShipKey()) ? template.getId().toString() : template.getShipKey();
  }
}
