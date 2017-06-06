// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model;

import io.xj.core.app.exception.BusinessException;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public abstract class EntityWrapper {
  public abstract Entity validate() throws BusinessException;
}
