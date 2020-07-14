// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package org.xiph.libvorbis;

import static org.xiph.libvorbis.vorbis_constants.integer_constants.NOISE_COMPAND_LEVELS;

public class compandblock {

  int[] data;    // data[NOISE_COMPAND_LEVELS]


  public compandblock(int[] _data) {

    data = new int[NOISE_COMPAND_LEVELS];
    System.arraycopy(_data, 0, data, 0, _data.length);
  }

  public compandblock(compandblock src) {

    this(src.data);
  }
}
