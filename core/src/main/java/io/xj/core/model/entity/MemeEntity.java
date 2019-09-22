//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.entity;

/**
 This represents common properties of all memes,
 although a MemeEntity only actually exists as a Segment MemeEntity, Sequence MemeEntity, etc.
 */
public interface MemeEntity<N> {

  /**
   Get MemeEntity name

   @return name
   */
  String getName();

  /**
   Set meme name

   @param name to set
   @return this (for chaining setters)
   */
  N setName(String name);
}
