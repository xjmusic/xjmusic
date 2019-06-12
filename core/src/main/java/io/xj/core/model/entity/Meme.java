//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.entity;

/**
 This represents common properties of all memes,
 although a Meme only actually exists as a Segment Meme, Sequence Meme, etc.
 */
public interface Meme<N> {

  /**
   Get Meme name

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
