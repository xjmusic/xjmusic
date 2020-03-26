// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.model;

import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.entity.Entity;

import java.util.UUID;

public class Work extends Entity {


  public static final String KEY_TARGET_ID = "targetId";
  private UUID targetId;
  private WorkState state;
  private WorkType type;

  /**
   Create a new Work

   @return new Work
   */
  public static Work create() {
    return (Work) new Work().setId(UUID.randomUUID());
  }

  public WorkState getState() {
    return state;
  }

  public UUID getTargetId() {
    return targetId;
  }

  public WorkType getType() {
    return type;
  }

  public Work setState(WorkState value) {
    state = value;
    return this;
  }

  public Work setTargetId(UUID value) {
    targetId = value;
    return this;
  }

  public Work setType(WorkType value) {
    type = value;
    return this;
  }

  @Override
  public UUID getParentId() {
    return targetId;
  }

  @Override
  public void validate() throws ValueException {
    Value.require(targetId, "Target ID");
    Value.require(type, "Type");
  }

  @Override
  public String toString() {
    return String.format("%s %s #%s", state, type, targetId);
  }

  /**
   Get a key based on the work type and target id

   @return target key
   */
  public String getTargetKey() {
    return String.format("%s-%s", type, targetId);
  }
}
