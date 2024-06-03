// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.model.music;

import io.xj.model.util.ValueUtils;

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

  public int computeSubsectionBeats(int beats) {
    var subDiv = ValueUtils.subDiv(beats, this.beats);
    var factors = ValueUtils.factors(beats, FACTORS_TO_TEST);
    var minFactor = Arrays.stream(factors).min().orElse(1);
    return Math.min(beats, Math.max(this.beats * minFactor, this.beats * subDiv));
  }

  public int getBeats() {
    return beats;
  }

  public Bar setBeats(int beats) {
    this.beats = beats;
    return this;
  }
}
