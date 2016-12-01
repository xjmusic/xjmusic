// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.idea;

import io.outright.xj.core.model.credit.Credit;
import io.outright.xj.core.model.meme.Meme;
import io.outright.xj.core.model.phase.Phase;
import io.outright.xj.core.primitive.density.Density;
import io.outright.xj.core.primitive.key.Key;
import io.outright.xj.core.primitive.tempo.Tempo;

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
  Density Density();

  /**
   * Idea Key
   *
   * @return String
   */
  Key Key();

  /**
   * Idea Tempo
   *
   * @return float
   */
  Tempo Tempo();

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
