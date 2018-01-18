// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.entity;

import io.xj.core.exception.BusinessException;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public abstract class EntityWrapper {
  public abstract Entity validate() throws BusinessException;
}
