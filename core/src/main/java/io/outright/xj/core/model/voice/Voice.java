// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.voice;

import io.outright.xj.core.model.event.Event;

public interface Voice {
  /**
   * Voice Type
   *
   * @return Type
   */
  Type Type();

  /**
   * Voice Description
   *
   * @return String
   */
  String Description();

  /**
   * Voice has many Events
   *
   * @return Event[]
   */
  Event[] Events();
}
