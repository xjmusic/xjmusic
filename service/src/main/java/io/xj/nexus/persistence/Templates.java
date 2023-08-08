package io.xj.nexus.persistence;

import io.xj.hub.tables.pojos.Template;
import io.xj.hub.util.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class Templates {
  /**
   * Get the identifier or a Template: ship key if available, else ID
   *
   * @param template to get identifier of
   * @return ship key if available, else ID
   */
  public static String getIdentifier(@Nullable Template template) {
    if (Objects.isNull(template)) return "N/A";
    return StringUtils.isNullOrEmpty(template.getShipKey()) ? template.getId().toString() : template.getShipKey();
  }
}
