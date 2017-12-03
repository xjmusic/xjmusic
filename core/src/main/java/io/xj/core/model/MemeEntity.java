// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model;

import io.xj.core.exception.BusinessException;

/**
 This represents common properties of all memes,
 although a Meme only actually exists as a Link Meme, Pattern Meme, etc.
 */
public abstract class MemeEntity extends Entity {
  public static final String KEY_ONE = "meme";
  public static final String KEY_MANY = "memes";

  public String getName() {
    return name;
  }

  protected String name;

  public abstract MemeEntity setName(String name);

  public void validate() throws BusinessException {
    if (this.name == null) {
      throw new BusinessException("Name is required.");
    }
  }

}
