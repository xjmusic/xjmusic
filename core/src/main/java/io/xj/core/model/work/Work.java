// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.work;

import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.impl.EntityImpl;

import java.math.BigInteger;

public class Work extends EntityImpl {
  public static final String KEY_TARGET_ID = "targetId";
  public static final String RESOURCE_TYPE = "works";
  private BigInteger targetId;
  private WorkState state;
  private WorkType type;

  @Override
  public String getResourceType() {
    return RESOURCE_TYPE;
  }

  public WorkState getState() {
    return state;
  }

  public BigInteger getTargetId() {
    return targetId;
  }

  public WorkType getType() {
    return type;
  }

  public Work setState(WorkState value) {
    state = value;
    return this;
  }

  public Work setTargetId(BigInteger value) {
    targetId = value;
    return this;
  }

  public Work setType(WorkType value) {
    type = value;
    return this;
  }

  @Override
  public BigInteger getParentId() {
    return targetId;
  }

  @Override
  public Work validate() throws CoreException {
    if (null == targetId)
      throw new CoreException("Target ID is required.");

    if (null == type || type.toString().isEmpty())
      throw new CoreException("Type is required.");

    return this;
  }

  @Override
  public String toString() {
    return String.format("%s %s #%s", state, type, targetId);
  }

}
