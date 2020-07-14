// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.entity;

import io.xj.lib.util.Text;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;

/**
 This represents common properties of all memes,
 although a MemeEntity only actually exists as a Segment MemeEntity, Sequence MemeEntity, etc.
 */
public abstract class MemeEntity extends Entity {
  private String name;

  /**
   Validate a Meme entity

   @param meme to validate
   @throws ValueException on invalid meme
   */
  public static void validate(MemeEntity meme) throws ValueException {
    Value.require(meme.name, "Name");
    meme.name = Text.toUpperSlug(meme.name);
  }

  /**
   Get MemeEntity name

   @return name
   */
  public String getName() {
    return name;
  }

  /**
   Set meme name

   @param name to set
   @return this (for chaining setters)
   */
  public MemeEntity setName(String name) {
    this.name = name;
    return this;
  }

}
