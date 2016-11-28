// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.voice;

import io.outright.xj.core.model.event.Event;

public class VoiceImpl implements Voice {
  private Type type;
  private String description;
  private Event[] events = new Event[0];

  public VoiceImpl(
    Type _type,
    String _description
  ) {
    type = _type;
    description = _description;
  }

  @Override
  public Type Type() {
    return type;
  }

  @Override
  public String Description() {
    return description;
  }

  @Override
  public Event[] Events() {
    return events;
  }
}
