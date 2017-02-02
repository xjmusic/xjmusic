package io.outright.xj.core.model;

import io.outright.xj.core.app.exception.BusinessException;

public abstract class EntityWrapper {
  public abstract void validate() throws BusinessException;
}
