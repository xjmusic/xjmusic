// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.idea;

import io.outright.xj.core.model.credit.Credit;
import io.outright.xj.core.model.meme.Meme;
import io.outright.xj.core.model.phase.Phase;

public interface Idea {
  /**
   * Idea Name
   *
   * @return String
   */
  String Name();

  /**
   * Idea Credit
   *
   * @return String
   */
  Credit Credit();

  /**
   * Idea Type
   *
   * @return String
   */
  Type Type();

  /**
   * Idea Density
   */
  float Density();

  /**
   * Idea Key
   *
   * @return String
   * TODO replace with Key (music theory) type
   */
  String Key();

  /**
   * Idea Tempo
   *
   * @return float
   */
  float Tempo();

  /**
   * Idea Memes (ordered)
   *
   * @return Meme[]
   */
  Meme[] Memes();

  /**
   * Idea Phases (ordered)
   *
   * @return Phase[]
   */
  Phase[] Phases();
}
