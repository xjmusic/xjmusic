// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.meme;

public interface Meme {
  /**
   * Meme Name
   * @return String
   */
  String Name();

  /**
   * Meme Order, relative to other memes in a set.
   * @return int
   */
  int Order();
}
