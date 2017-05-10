// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.meme;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.Entity;

public abstract class Meme extends Entity {
  public static final String KEY_ONE = "meme";
  public static final String KEY_MANY = "memes";

  public String getName() {
    return name;
  }

  protected String name;

  public abstract Meme setName(String name);

  public void validate() throws BusinessException {
    if (this.name == null) {
      throw new BusinessException("Name is required.");
    }
  }

}
