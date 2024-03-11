package io.xj.gui.modes;

/**
 Choice of Grid
 - values like 0.5, 0.25, 0.125
 - displayed as 1/2, 1/4, 1/8
 */
public record GridChoice(double value) {
  @Override
  public String toString() {
    return String.format("1/%d", (int) (1 / value));
  }
}
