// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package org.xiph.libvorbis;

import org.xiph.libvorbis.vorbis_constants.integer_constants;

public class att3 {

  int[] att;    // att[P_NOISECURVES]
  float boost;
  float decay;


  public att3(int[] _att, float _boost, float _decay) {

    att = new int[integer_constants.P_NOISECURVES];
    System.arraycopy(_att, 0, att, 0, _att.length);

    boost = _boost;
    decay = _decay;
  }

  public att3(att3 src) {

    this(src.att, src.boost, src.decay);
  }
}
