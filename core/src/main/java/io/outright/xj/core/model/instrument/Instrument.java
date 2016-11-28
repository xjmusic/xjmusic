// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.instrument;

import io.outright.xj.core.model.Audio;
import io.outright.xj.core.model.credit.Credit;
import io.outright.xj.core.model.meme.Meme;

public interface Instrument {
  /**
   * Instrument Type
   *
   * @return Type
   */
  Type Type();

  /**
   * Instrument Description
   *
   * @return String
   * TODO replace with Description (String) type
   */
  String Description();

  /**
   * Instrument Credit
   *
   * @return Credit
   */
  Credit Credit();

  /**
   * Instrument Density (ratio)
   *
   * @return float
   * TODO replace with Density (ratio) type
   */
  float Density();

  /**
   * Instrument Memes
   *
   * @return Meme[]
   */
  Meme[] Memes();

  /**
   * Instrument Audios
   *
   * @return Audio[]
   */
  Audio[] Audios();
}
