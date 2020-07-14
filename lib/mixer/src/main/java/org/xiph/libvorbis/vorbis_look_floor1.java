// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package org.xiph.libvorbis;

import static org.xiph.libvorbis.vorbis_constants.integer_constants.VIF_POSIT;

class vorbis_look_floor1 {

  int[] sorted_index;  // int sorted_index[VIF_POSIT+2];
  int[] forward_index;  // int forward_index[VIF_POSIT+2];
  int[] reverse_index;  // int reverse_index[VIF_POSIT+2];

  int[] hineighbor;  // int hineighbor[VIF_POSIT];
  int[] loneighbor;  // int loneighbor[VIF_POSIT];
  int posts;

  int n;
  int quant_q;
  vorbis_info_floor1 vi;

  int phrasebits;
  int postbits;
  int frames;


  public vorbis_look_floor1() {

    sorted_index = new int[VIF_POSIT + 2];
    forward_index = new int[VIF_POSIT + 2];
    reverse_index = new int[VIF_POSIT + 2];

    hineighbor = new int[VIF_POSIT];
    loneighbor = new int[VIF_POSIT];
  }
}
