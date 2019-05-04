//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.event;

import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.Entity;

public interface Event extends Entity {
  String KEY_ONE = "event";
  String KEY_MANY = "events";

  Double getDuration();

  Event setDuration(Double duration);

  String getInflection();

  Event setInflection(String inflection);

  String getNote();

  Event setNote(String note);

  Double getPosition();

  Event setPosition(Double position);

  Double getTonality();

  Event setTonality(Double tonality);

  Double getVelocity();

  Event setVelocity(Double velocity);

  @Override
  void validate() throws CoreException;
}
