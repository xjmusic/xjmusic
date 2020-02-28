// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.model;

import com.google.common.collect.ImmutableList;
import io.xj.lib.core.entity.Entity;
import io.xj.lib.core.exception.CoreException;

import java.util.UUID;

public class Work extends Entity {
  public static final ImmutableList<String> RESOURCE_ATTRIBUTE_NAMES = ImmutableList.<String>builder()
    .addAll(Entity.RESOURCE_ATTRIBUTE_NAMES)
    .build();
  public static final ImmutableList<Class> RESOURCE_BELONGS_TO = ImmutableList.<Class>builder()
    .build();
  public static final ImmutableList<Class> RESOURCE_HAS_MANY = ImmutableList.<Class>builder()
    .build();
  public static final String KEY_TARGET_ID = "targetId";
  private UUID targetId;
  private WorkState state;
  private WorkType type;

  /**
   * Create a new Work
   * @return new Work
   */
  public static Work create() {
    return (Work) new Work().setId(UUID.randomUUID());
  }

  @Override
  public ImmutableList<String> getResourceAttributeNames() {
    return RESOURCE_ATTRIBUTE_NAMES;
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return RESOURCE_BELONGS_TO;
  }

  @Override
  public ImmutableList<Class> getResourceHasMany() {
    return RESOURCE_HAS_MANY;
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
  public void validate() throws CoreException {
    require(targetId, "Target ID");
    require(type, "Type");
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
