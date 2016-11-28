// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.link;

import io.outright.xj.core.model.choice.Choice;
import io.outright.xj.core.model.chord.Chord;
import io.outright.xj.core.model.meme.Meme;

public interface Link {
  /**
   * Link Offset
   *
   * @return int
   * TODO replace with Offset (int) type
   */
  int Offset();

  /**
   * Link State
   *
   * @return State
   */
  State State();

  /**
   * Link Start (time in seconds from start of chain)
   *
   * @return float
   * TODO replace with Time (seconds, start, stop, float) type
   */
  float Start();

  /**
   * Link Finish (time in seconds from start of chain)
   *
   * @return float
   * TODO replace with Time (seconds, start, stop, float) type
   */
  float Finish();

  /**
   * Link Total (Beats)
   *
   * @return float
   * TODO replace with Total (float) type
   */
  float Total();

  /**
   * Link Density
   *
   * @return float
   * TODO replace with Density (float) type
   */
  float Density();

  /**
   * Link Key
   *
   * @return String
   * TODO replace with Key (String) type
   */
  String Key();

  /**
   * Link Tempo
   *
   * @return float
   * TODO replace with Tempo (float) type
   */
  float Tempo();

  /**
   * Link Memes
   *
   * @return Meme[]
   */
  Meme[] Memes();

  /**
   * Link Choices
   *
   * @return Choice[]
   */
  Choice[] Choices();

  /**
   * Link Chords
   *
   * @return Chord[]
   */
  Chord[] Chords();
}
