// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package org.xiph.libvorbis;

import org.xiph.libvorbis.vorbis_constants.integer_constants;

public class compandblock {

  int[] data;    // data[NOISE_COMPAND_LEVELS]


  public compandblock(int[] _data) {

    data = new int[integer_constants.NOISE_COMPAND_LEVELS];
    System.arraycopy(_data, 0, data, 0, _data.length);
  }

  public compandblock(compandblock src) {

    this(src.data);
  }
}
