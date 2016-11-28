// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.event;

public interface Event {
  /**
   * Event Velocity (ratio)
   *
   * @return float
   * TODO replace with Velocity (ratio) type
   */
  float Velocity();

  /**
   * Event Tonality (ratio)
   *
   * @return float
   * TODO replace with Tonality (ratio) type
   */
  float Tonality();

  /**
   * Event Tonality (ratio)
   *
   * @return String
   * TODO replace with Inflection (String) type
   */
  String Inflection();

  /**
   * Event Position (beats)
   *
   * @return float
   * TODO replace with Position (beats) type
   */
  float Position();

  /**
   * Event Duration (beats)
   *
   * @return float
   * TODO replace with Duration (beats) type
   */
  float Duration();

  /**
   * Event Note (music theory)
   *
   * @return String
   * TODO replace with Note (music theory) type
   */
  String Note();
}
