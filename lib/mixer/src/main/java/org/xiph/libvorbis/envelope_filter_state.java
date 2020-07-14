// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package org.xiph.libvorbis;

import static org.xiph.libvorbis.vorbis_constants.integer_constants.VE_AMP;
import static org.xiph.libvorbis.vorbis_constants.integer_constants.VE_NEARDC;

class envelope_filter_state {

  float[] ampbuf;
  int ampptr;

  float[] nearDC;
  float nearDC_acc;
  float nearDC_partialacc;
  int nearptr;


  public envelope_filter_state(float[] _ampbuf, int _ampptr, float[] _nearDC, float _nearDC_acc, float _nearDC_partialacc, int _nearptr) {

    ampbuf = new float[VE_AMP];
    System.arraycopy(_ampbuf, 0, ampbuf, 0, _ampbuf.length);

    ampptr = _ampptr;

    nearDC = new float[VE_NEARDC];
    System.arraycopy(_nearDC, 0, nearDC, 0, _nearDC.length);

    nearDC_acc = _nearDC_acc;
    nearDC_partialacc = _nearDC_partialacc;
    nearptr = _nearptr;
  }

  public envelope_filter_state(envelope_filter_state src) {

    this(src.ampbuf, src.ampptr, src.nearDC, src.nearDC_acc, src.nearDC_partialacc, src.nearptr);
  }

  public envelope_filter_state() {

    ampbuf = new float[VE_AMP];

    nearDC = new float[VE_NEARDC];
  }
}
