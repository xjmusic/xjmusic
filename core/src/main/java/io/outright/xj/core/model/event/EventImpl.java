// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.event;

import io.outright.xj.core.primitive.beat.Beat;
import io.outright.xj.core.primitive.inflection.Inflection;
import io.outright.xj.core.primitive.note.Note;
import io.outright.xj.core.primitive.tonality.Tonality;
import io.outright.xj.core.primitive.velocity.Velocity;

public class EventImpl implements Event {
  private Velocity velocity;
  private Tonality tonality;
  private Inflection inflection;
  private Beat position;
  private Beat duration;
  private Note note;

  public EventImpl(
    Velocity velocity,
    Tonality tonality,
    Inflection inflection,
    Beat position,
    Beat duration,
    Note note
  ) {
    this.velocity = velocity;
    this.tonality = tonality;
    this.inflection = inflection;
    this.position = position;
    this.duration = duration;
    this.note = note;
  }

  @Override
  public Velocity Velocity() {
    return velocity;
  }

  @Override
  public Tonality Tonality() {
    return tonality;
  }

  @Override
  public Inflection Inflection() {
    return inflection;
  }

  @Override
  public Beat Position() {
    return position;
  }

  @Override
  public Beat Duration() {
    return duration;
  }

  @Override
  public Note Note() {
    return note;
  }
}
