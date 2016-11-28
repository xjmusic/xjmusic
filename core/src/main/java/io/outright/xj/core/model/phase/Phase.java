// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.phase;

import io.outright.xj.core.model.chord.Chord;
import io.outright.xj.core.model.meme.Meme;
import io.outright.xj.core.model.voice.Voice;

public interface Phase {
  /**
   * Phase Name
   *
   * @return String
   */
  String Name();

  /**
   * Phase Offset
   *
   * @return int
   * TODO replace with Offset (int) type
   */
  int Offset();

  /**
   * Phase Total (Beats)
   *
   * @return float
   * TODO replace with Total (float) type
   */
  float Total();

  /**
   * Phase Density
   *
   * @return float
   * TODO replace with Density (float) type
   */
  float Density();

  /**
   * Phase Key
   *
   * @return String
   * TODO replace with Key (String) type
   */
  String Key();

  /**
   * Phase Tempo
   *
   * @return float
   * TODO replace with Tempo (float) type
   */
  float Tempo();

  /**
   * Phase has many Memes
   *
   * @return Meme[]
   */
  Meme[] Memes();

  /**
   * Phase has many Chords
   *
   * @return Chord[]
   */
  Chord[] Chords();

  /**
   * Phase has many Voices
   *
   * @return Voice[]
   */
  Voice[] Voices();
}
