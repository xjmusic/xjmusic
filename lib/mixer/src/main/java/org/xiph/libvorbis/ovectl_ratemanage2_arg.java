// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package org.xiph.libvorbis;

public class ovectl_ratemanage2_arg {

  int management_active;

  int bitrate_limit_min_kbps;    // long
  int bitrate_limit_max_kbps;    // long
  int bitrate_limit_reservoir_bits;  // long
  float bitrate_limit_reservoir_bias;  // float

  int bitrate_average_kbps;    // long
  float bitrate_average_damping;  // float
}
