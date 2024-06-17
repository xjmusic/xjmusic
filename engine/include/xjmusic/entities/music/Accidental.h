// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_MUSIC_ACCIDENTAL_H
#define XJMUSIC_MUSIC_ACCIDENTAL_H

namespace XJ {

  /**
   * Expression of the "accidental notes" as either Sharps or Flats
   * Note has an adjustment symbol (Sharp or Flat) to render the "accidental notes for a given name (e.g. of a chord, scale or key)
   */
  enum Accidental {
    Natural,
    Sharp,
    Flat
  };

  /**
   * Accidental the adjustment symbol (Sharp or Flat) for a given name (e.g. of a chord, scale or key)
   * @param name to get adjustment symbol of
   * @return adjustment symbol
   */
  Accidental accidentalOf(const std::string &name);

  /**
   * the Accidental (Sharp or Flat) that begins a given name (e.g. the Root of a chord, scale or key)
   * @param name to get adjustment symbol from the beginning of
   * @return adjustment symbol
   */
  Accidental accidentalOfBeginning(const std::string &name);

  /**
   * Replace any accidentals with the explicit text "s" or "b"
   * @param name within which to replace text
   */
  std::string accidentalNormalized(const std::string &name);

} // namespace XJ

#endif //XJMUSIC_MUSIC_ACCIDENTAL_H
