// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package org.xiph.libvorbis;

import static org.xiph.libvorbis.vorbis_constants.integer_constants.PACKETBLOBS;

class vorbis_block_internal {

  float[][] pcmdelay;  // **pcmdelay // this is a pointer into local storage
  float ampmax;
  int blocktype;

  oggpack_buffer[] packetblob;


  public vorbis_block_internal() {

    packetblob = new oggpack_buffer[PACKETBLOBS];
  }
}
