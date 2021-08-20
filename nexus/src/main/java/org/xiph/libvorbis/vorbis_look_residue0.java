// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package org.xiph.libvorbis;

class vorbis_look_residue0 {

  vorbis_info_residue0 info;

  int parts;
  int stages;

  codebook[] fullbooks;    // *fullbooks;
  codebook phrasebook;    // *phrasebook;
  codebook[][] partbooks;    // ***partbooks;

  int partvals;
  int[][] decodemap;    // **decodemap;

  int postbits;
  int phrasebits;
  int frames;

  // #ifdef TRAIN_RES
  // int        train_seq;
  // long      *training_data[8][64];
  // float      training_max[8][64];
  // float      training_min[8][64];
  // float     tmin;
  // float     tmax;
  // #endif


  public vorbis_look_residue0() {

  }
}
