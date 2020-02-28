// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.entity;

import com.google.common.collect.ImmutableList;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.util.Text;

/**
 This represents common properties of all memes,
 although a MemeEntity only actually exists as a Segment MemeEntity, Sequence MemeEntity, etc.
 */
public abstract class MemeEntity extends Entity {
  public static final ImmutableList<String> RESOURCE_ATTRIBUTE_NAMES =
    ImmutableList.<String>builder()
      .addAll(Entity.RESOURCE_ATTRIBUTE_NAMES)
      .add("name")
      .build();
  private String name;

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

  public static void validate(MemeEntity meme) throws CoreException {
    require(meme.name, "Name");
    meme.name = Text.toUpperSlug(meme.name);
  }

}
