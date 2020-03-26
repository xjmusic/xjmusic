// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package org.xiph.libvorbis;

import static org.xiph.libvorbis.vorbis_constants.integer_constants.PACKETBLOBS;

class bitrate_manager_state {

	int managed;

	int avg_reservoir;		// long
	int minmax_reservoir;	// long
	int avg_bitsper;		// long
	int min_bitsper;		// long
	int max_bitsper;		// long

	int short_per_long;		// long
	float avgfloat;			// double

	vorbis_block vb;
	int choice;


	public bitrate_manager_state( vorbis_info vi ) {

		codec_setup_info ci = vi.codec_setup;
		bitrate_manager_info bi = ci.bi;

		if ( bi.reservoir_bits > 0 ) {

			int ratesamples = vi.rate;
			int halfsamples = ci.blocksizes[0]>>1;

			short_per_long = ci.blocksizes[1]/ci.blocksizes[0];
			managed=1;

			avg_bitsper = new Double( Math.rint(1.*bi.avg_rate*halfsamples/ratesamples) ).intValue();
			min_bitsper = new Double( Math.rint(1.*bi.min_rate*halfsamples/ratesamples) ).intValue();
			max_bitsper = new Double( Math.rint(1.*bi.max_rate*halfsamples/ratesamples) ).intValue();

			avgfloat = PACKETBLOBS/2;

			// not a necessary fix, but one that leads to a more balanced typical initialization
			{
				int desired_fill = new Float(bi.reservoir_bits*bi.reservoir_bias).intValue();	// long
				minmax_reservoir = desired_fill;
				avg_reservoir = desired_fill;
			}
		}
	}
}
