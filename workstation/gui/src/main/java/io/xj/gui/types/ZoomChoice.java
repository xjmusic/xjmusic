package io.xj.gui.types;

import io.xj.model.util.StringUtils;

/**
 Choice of Zoom
 - values like 0.25, 0.5, 1, 2
 - displayed as 25%, 50%, 100%, 200%
 */
public record ZoomChoice(double value) {
  @Override
  public String toString() {
    return StringUtils.percentage((float) value);
  }
}
