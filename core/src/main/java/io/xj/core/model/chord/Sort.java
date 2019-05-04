//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.chord;

import java.util.Comparator;

public enum Sort {
  ;
  public static final Comparator<? super Chord> byPositionAscending = (Comparator<? super Chord>) (o1, o2) -> {
    if (o1.getPosition() > o2.getPosition()) return 1;
    if (o1.getPosition() < o2.getPosition()) return -1;
    return 0;
  };
  public static final Comparator<? super Chord> byPositionDescending = (Comparator<? super Chord>) (o1, o2) -> {
    if (o1.getPosition() > o2.getPosition()) return -1;
    if (o1.getPosition() < o2.getPosition()) return 1;
    return 0;
  };

}
