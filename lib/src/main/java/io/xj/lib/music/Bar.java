// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.lib.music;

import io.xj.lib.util.ValueUtils;

import java.util.Arrays;
import java.util.Objects;

public class Bar {
  static final int[] FACTORS_TO_TEST = new int[]{3, 4, 5, 7};
  int beats;

  Bar(Integer beats) {
    if (Objects.isNull(beats)) throw new MusicalException("Bar must have beats!");
    if (0 >= beats) throw new MusicalException("Bar must beats greater than zero!");
    this.beats = beats;
  }

  public static Bar of(Integer beats) {
    return new Bar(beats);
  }

  public int computeSubsectionBeats(int totalBeats) {
    var subDiv = ValueUtils.subDiv(totalBeats, beats);
    var factors = ValueUtils.factors(totalBeats, FACTORS_TO_TEST);
    var minFactor = Arrays.stream(factors).min().orElse(1);
    return Math.min(totalBeats, Math.max(beats * minFactor, beats * subDiv));
  }

  public int getBeats() {
    return beats;
  }

  public Bar setBeats(int beats) {
    this.beats = beats;
    return this;
  }
}
