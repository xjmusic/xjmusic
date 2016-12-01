// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.event;

import io.outright.xj.core.primitive.beat.Beat;
import io.outright.xj.core.primitive.inflection.Inflection;
import io.outright.xj.core.primitive.note.Note;
import io.outright.xj.core.primitive.tonality.Tonality;
import io.outright.xj.core.primitive.velocity.Velocity;

public interface Event {
  /**
   * Event Velocity (ratio)
   *
   * @return Velocity
   */
  Velocity Velocity();

  /**
   * Event Tonality (ratio)
   *
   * @return Tonality
   */
  Tonality Tonality();

  /**
   * Event Tonality (ratio)
   *
   * @return Inflection
   */
  Inflection Inflection();

  /**
   * Event Position (beats)
   *
   * @return Beat
   */
  Beat Position();

  /**
   * Event Duration (beats)
   *
   * @return Beat
   */
  Beat Duration();

  /**
   * Event Note (music theory)
   *
   * @return Note
   */
  Note Note();
}
