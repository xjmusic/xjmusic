// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.event;

public class EventImpl implements Event {
  private float velocity;
  private float tonality;
  private String inflection;
  private float position;
  private float duration;
  private String note;

  public EventImpl(
    float _velocity,
    float _tonality,
    String _inflection,
    float _position,
    float _duration,
    String _note
  ) {
    velocity = _velocity;
    tonality = _tonality;
    inflection = _inflection;
    position = _position;
    duration = _duration;
    note = _note;
  }

  @Override
  public float Velocity() {
    return velocity;
  }

  @Override
  public float Tonality() {
    return tonality;
  }

  @Override
  public String Inflection() {
    return inflection;
  }

  @Override
  public float Position() {
    return position;
  }

  @Override
  public float Duration() {
    return duration;
  }

  @Override
  public String Note() {
    return note;
  }
}
