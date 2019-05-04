// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.meme;

import io.xj.core.exception.CoreException;

/**
 This represents common properties of all memes,
 although a Meme only actually exists as a Segment Meme, Sequence Meme, etc.
 */
public interface Meme<N> {
  String KEY_ONE = "meme";
  String KEY_MANY = "memes";

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

  /**
   Validate the meme

   @throws CoreException if invalid
   */
  void validate() throws CoreException;
}
