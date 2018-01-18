// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.meme;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.Entity;

import java.math.BigInteger;

/**
 This represents common properties of all memes,
 although a Meme only actually exists as a Link Meme, Pattern Meme, etc.
 */
public abstract class Meme extends Entity {
  public static final String KEY_ONE = "meme";
  public static final String KEY_MANY = "memes";

  public String getName() {
    return name;
  }

  protected String name;

  public Meme setName(String name) {
    this.name = name;
    return this;
  }

  public void validate() throws BusinessException {
    if (null == name) {
      throw new BusinessException("Name is required.");
    }
  }

}
