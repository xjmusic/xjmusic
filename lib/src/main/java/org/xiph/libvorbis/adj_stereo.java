// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package org.xiph.libvorbis;

import org.xiph.libvorbis.vorbis_constants.integer_constants;

public class adj_stereo {

  int[] pre;
  int[] post;
  float[] kHz;
  float[] lowpasskHz;


  public adj_stereo(int[] _pre, int[] _post, float[] _kHz, float[] _lowpasskHz) {

    pre = new int[integer_constants.PACKETBLOBS];
    System.arraycopy(_pre, 0, pre, 0, _pre.length);

    post = new int[integer_constants.PACKETBLOBS];
    System.arraycopy(_post, 0, post, 0, _post.length);

    kHz = new float[integer_constants.PACKETBLOBS];
    System.arraycopy(_kHz, 0, kHz, 0, _kHz.length);

    lowpasskHz = new float[integer_constants.PACKETBLOBS];
    System.arraycopy(_lowpasskHz, 0, lowpasskHz, 0, _lowpasskHz.length);
  }

  public adj_stereo(adj_stereo src) {

    this(src.pre, src.post, src.kHz, src.lowpasskHz);
  }
}
