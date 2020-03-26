// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package org.xiph.libvorbis;

public class noiseguard {

	int lo;
	int hi;
	int fixed;


	public noiseguard( int _lo, int _hi, int _fixed ) {

		lo = _lo;
		hi = _hi;
		fixed = _fixed;
	}
}
